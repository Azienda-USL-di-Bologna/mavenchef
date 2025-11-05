package it.bologna.ausl.utils.albo;

import com.mongodb.MongoException;
import it.bologna.ausl.mongowrapper.MongoWrapper;
import it.bologna.ausl.mongowrapper.exceptions.MongoWrapperException;
import it.bologna.ausl.pdfconverterandsigner.PdfConvertClient;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.apache.http.client.HttpResponseException;
import org.json.simple.JSONObject;

public class Test {

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, KeyManagementException, NoSuchAlgorithmException, MalformedURLException, UnknownHostException, MongoException, MongoWrapperException, IOException {
        String mongoUri = "DONTUSEMONGO";
        String minIODBDriver = "org.postgresql.Driver";
        String minIODBUrl = "jdbc:postgresql://gdml.internal.ausl.bologna.it:5432/minirepo?stringtype=unspecified";
        String minIODBUsername = "minirepo";
        String minIODBPassword = "siamofreschi";
        String codiceAzienda = "105t";
        MongoWrapper m = MongoWrapper.getWrapper(true, mongoUri, minIODBDriver, minIODBUrl, minIODBUsername, minIODBPassword, codiceAzienda, null);
        
        PdfConvertClient pdfc = new PdfConvertClient("https://gdml.internal.ausl.bologna.it/firmasemplice/PdfConvert", new File("tempdir"), null);
//      PdfConvertClient pdfc = new PdfConvertClient("http://localhost:8082/firmasemplice/PdfConvert", new File("tempdir"), null);
        File file = new File("errore_pdf.png");
        
//        pdfc.convert("cafsaf", new File("c:/tmp/provaaaaaaa.pdf"), true, null);
        byte[] convert = null;
        try {
            convert = pdfc.convert(new FileInputStream(file), true, null);
            DataOutputStream dataos = new DataOutputStream(new FileOutputStream(file.getAbsoluteFile() + ".pdf"));
            dataos.write(convert);
            String put = m.put(file, file.getName(), "/temp", false);
            System.out.println("res: " + put);
        }
        catch (HttpResponseException ex) {
            System.out.println("Errore nel formato file:\n" + ex.getStatusCode() + "\n" + ex.getMessage());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
