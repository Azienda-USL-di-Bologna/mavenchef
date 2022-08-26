package it.bologna.ausl.masterchef.workers;

import com.itextpdf.text.pdf.PdfAConformanceException;
import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.masterchef.utils.UtilityFunctions;
import static it.bologna.ausl.masterchef.utils.UtilityFunctions.mergePdf;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import it.bologna.ausl.mongowrapper.exceptions.MongoWrapperException;
import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 INPUT
 {
 reporterList:[
        {
            reportName:"nome del report"
            filePath:"path completo su mongo"
            reporterParams:{
                templatePath:"/path/su/mongo"
                'data': {
                     'nomeCampo': valoreCampo
                }
                'qrCodeData': "Azienda USL di Bologna Codice AOO:ASL_BO:protocollo generale - [numero] [data] [ora]"
            }
        }
],
allegati:[uuid,uuid, ....],
savePath:"path completo su mongo"
}

OUTPUT
{
    UUIDStampaUnica:String,
    pdf:String (uuid in mongodownload),
    size:bytes,
    reporterList:[
        {
            reportName:String
            filePath:String,
            uuid:String,
            size:bytes
        }
    ]
}
 */
public class StampaUnicaReporterWorker implements Chef {
//	private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");

    private MongoWrapper m, md;
    private static final Logger log = LogManager.getLogger(StampaUnicaReporterWorker.class);
    private final ReporterWorker rw;

    public StampaUnicaReporterWorker() throws WorkerException {
//            Properties config=Launcher.loadConfig(configFile);
        String mongoUri = config.getProperty("mongo.uri", "mongodb://argo:siamofreschi@procton4,procton3/doc?safe=true");
        String mongoDownloadUri = config.getProperty("mongodownload.uri", "mongodb://argo:siamofreschi@procton3/downloadgdml");
        try {
//            config.keySet().stream().forEach(k -> {
//                log.info(k.toString() + ":" + config.getProperty(k.toString()));
//            });
            Boolean useMinIO = Boolean.parseBoolean(config.getProperty("minio.enabled"));
            JSONObject minIOConfig = (JSONObject) JSONValue.parse(config.getProperty("minio.config"));
            String codiceAzienda = config.getProperty("masterchef.codice-azienda");

            // TOrna un Long e va convertito in Integer
            Integer maxPoolSize = Math.toIntExact((Long) minIOConfig.get("maxPoolSize"));
            log.info("useMinIO -> " + useMinIO);
            log.info("minIOConfig -> " + minIOConfig);
            log.info("codiceAzienda -> " + codiceAzienda);
            log.info("maxPoolSize -> " + maxPoolSize);
            m = MongoWrapper.getWrapper(
                    useMinIO, mongoUri,
                    minIOConfig.get("DBDriver").toString(),
                    minIOConfig.get("DBUrl").toString(),
                    minIOConfig.get("DBUsername").toString(),
                    minIOConfig.get("DBPassword").toString(),
                    codiceAzienda,
                    maxPoolSize,
                    null);
            md = MongoWrapper.getWrapper(
                    useMinIO, mongoDownloadUri,
                    minIOConfig.get("DBDriver").toString(),
                    minIOConfig.get("DBUrl").toString(),
                    minIOConfig.get("DBUsername").toString(),
                    minIOConfig.get("DBPassword").toString(),
                    codiceAzienda + "t",
                    maxPoolSize,
                    null);
        } catch (Exception e) {
            throw new WorkerException(getJobType(), e);
        }
        rw = new ReporterWorker();
    }

    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        //log.debug(in);
        //log.debug(in.get("savePath"));
        log.info("entro nella doWork");
        JSONArray reporterList = (JSONArray) in.get("reporterList");
        JSONArray reporterOut = new JSONArray();
        ArrayList<String> reporterPdf = null;
        if (reporterList != null) {
            reporterPdf = new ArrayList<>();
            for (Object reporterObj : reporterList) {
                String reportName = (String) ((JSONObject) reporterObj).get("reportName");
                String filePath = (String) ((JSONObject) reporterObj).get("filePath");
                JSONObject reporterParams = (JSONObject) ((JSONObject) reporterObj).get("reporterParams");
                String pdfUuidTemp = generatePdf(reporterParams, workDir);
                reporterPdf.add(pdfUuidTemp);

                if (filePath != null) {
                    try {
                        // se esiste già un file con il nome che vogliamo mettere verrà rinominato
                        UtilityFunctions.renameExistentFileMongo(m, filePath);
                        String uuid = m.put(md.get(pdfUuidTemp), MongoWrapper.getFname(filePath), MongoWrapper.getDname(filePath).replace("\\", "/"), "system", reportName, true);
                        log.info("UUID DI MONGO CHE PRENDI DAL REPO DI MINIO " + uuid);
                        JSONObject reporter = new JSONObject();
                        reporter.put("reportName", reportName);
                        reporter.put("filePath", filePath);
                        reporter.put("uuid", uuid);
                        reporter.put("size", m.getSizeByUuid(uuid));
                        reporterOut.add(reporter);
                    } catch (IOException | MongoWrapperException e) {
                        log.warn("errore nel creare la stampa unica pdf/a, provo a crearla non pdf/a...",e);
                        throw new WorkerException(getJobType(), e);
                    }
                }
            }
        }
        List<Map.Entry<String, MongoWrapper>> toMerge = null;

        JSONArray allegati = (JSONArray) in.get("allegati");
        if (allegati != null) {
            try {
                toMerge = getPdfStreams(allegati);
            } catch (MongoWrapperException ex) {
            }
        }

        if (toMerge == null) {
            toMerge = new ArrayList();
        }

        if (reporterPdf != null) {
            for (int i = reporterPdf.size() - 1; i >= 0; i--) {
//                try {
                toMerge.add(0, new AbstractMap.SimpleEntry(reporterPdf.get(i), md));
//                    isList.add(0, md.get(reporterPdf.get(i)));
//                } catch (Exception ex) {
//                    log.warn(String.format("errore nel reperimento dell'uuid: %s dal repository temporaneo, il file non verrà aggiunto alla stampa unica", reporterPdf.get(i)), ex);
//                }
            }
        }
        String outPdfUuid = null;
        String outPdfUuidTemp = null;

        // buffer all data in a circular buffer of infinite size
        //        CircularByteBuffer cbb = new CircularByteBuffer(CircularByteBuffer.INFINITE_SIZE);
        //        mergePdf1(isList, cbb.getOutputStream());
        byte[] outPdf;
        try {
            InputStream iccProfileStream = getClass().getResourceAsStream("/it/bologna/ausl/mavenchef/AdobeRGB1998.icc");
            if(iccProfileStream==null){
            log.info("ICCPROFILESTREAM é NULL CAZZO");
            }
            try {
                outPdf = UtilityFunctions.mergePdf(toMerge, iccProfileStream, true);
            } catch (PdfAConformanceException ex) {
               
                log.warn("errore nel creare la stampa unica pdf/a, provo a crearla non pdf/a...",ex);
                if (allegati != null) {
                    try {
                        toMerge = getPdfStreams(allegati);
                    } catch (MongoWrapperException subEx) {
                    }
                }
                if (toMerge == null) {
                    toMerge = new ArrayList();
                }
                if (reporterPdf != null) {
                    for (int i = reporterPdf.size() - 1; i >= 0; i--) {
                        toMerge.add(0, new AbstractMap.SimpleEntry(reporterPdf.get(i), md));
//                        isList.add(0, reporterPdf.get(i));
                    }
                }
                outPdf = mergePdf(toMerge, null, false);
            }
            String tempFileName = String.format("stampa_unica_reporter_%s.pdf", UUID.randomUUID().toString());
            outPdfUuidTemp = md.put(new ByteArrayInputStream(outPdf), tempFileName, "/tmp", false);
            String savePath = (String) in.get("savePath");
            if (savePath != null) {
                // se esiste già un file con il nome che vogliamo mettere verrà rinominato
                UtilityFunctions.renameExistentFileMongo(m, savePath);
                outPdfUuid = m.put(new ByteArrayInputStream(outPdf), MongoWrapper.getFname(savePath), MongoWrapper.getDname(savePath).replace("\\", "/"), "system", "stampa_unica", true);
//                outPdfUuid=m.put(cbb.getInputStream(), MongoWrapper.getFname(savePath), MongoWrapper.getDname(savePath).replace("\\", "/"));
            }
        } catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        }
        JSONObject res = new JSONObject();
        res.put("UUIDStampaUnica", outPdfUuid);
        //res.put("pdf",Base64Coder.encodeLines(outPdf));
        res.put("pdf", outPdfUuidTemp);
        res.put("size", outPdf.length);
        res.put("reporterList", reporterOut);
        return res;
    }

    private String generatePdf(JSONObject params, File workDir) throws WorkerException {
        try {
            JSONObject res = rw.doWork(params, 0, null, workDir, null);
            String mongoDownloadUuid = (String) res.get("pdf");
            return mongoDownloadUuid;
        } catch (WorkerException ex) {
            ex.setJobType(getJobType());
            throw ex;
        } catch (Exception ex) {
            throw new WorkerException(getJobType(), ex);
        }

        //return m.put(new ByteArrayInputStream(pdf),MongoWrapper.getFname(fileName),MongoWrapper.getDname(fileName));
    }

    private List<Map.Entry<String, MongoWrapper>> getPdfStreams(ArrayList<String> inputUuids) throws WorkerException, MongoWrapperException {
        if (inputUuids != null && !inputUuids.isEmpty()) {
            return inputUuids.stream().map(uuid -> new AbstractMap.SimpleEntry<String, MongoWrapper>(uuid, m)).collect(Collectors.toList());
        } else {
            return null;
        }
//        ArrayList<Map.Entry<String, MongoWrapper>> res = new ArrayList();
//        for (String inputUuid : inputUuids) {
//            InputStream in = m.get(inputUuid);
//            if (in == null) {
//                log.info("allegato uuid: " + inputUuid + "non trovato in Mongo. Non lo aggiungo alla stampa unica");
//                //throw new WorkerException(getJobType(), "Allegato uuid " + inputUuids.get(i) + " non trovato in Mongo" );
//            } else {
//                res.add(in);
//            }
//        }
//        return res;
    }

    @Override
    public final String getJobType() {
        return "StampaUnicaReporter";
    }
}
