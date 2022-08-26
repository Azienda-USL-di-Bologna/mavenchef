package it.bologna.ausl.utils.albo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.json.simple.JSONObject;

public class Test {

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
        // TODO Auto-generated method stub
        String url = "http://vm-zope-dev2.internal.ausl.bologna.it:8380/eauslbo/pubblicaDoc";
        url = "http://vm-zope-dev2:8380/eauslbo/albo_pubblica_doc";
        //url = "https://albo-extranet.internal.ausl.bologna.it/albo_pubblica_doc";
        //doc_am_ut
        //Nu5eiTho
        JSONObject jsontest = new JSONObject();
        ArrayList<String> docs = new ArrayList<String>();
        docs.add("1234");
        docs.add("456");
        //	AlboMod amod=new AlboMod(AlboMod.Operation.ANNULLA,docs);
        AlboDoc adoc = new AlboDoc();
        //	adoc.setAl("2013-09-04");
////		jsontest.put("al","2013-03-01");
//		adoc.setDal("2013-08-20");
////		jsontest.put("dal","2013-02-15");
//		adoc.setAnno("2013");
////		jsontest.put("anno","2013");
//		adoc.setDataAdozione("2013-08-20");
////		jsontest.put("dataAdozione","2013-02-18");
//		adoc.setDataDoc("2013-08-20");
////		jsontest.put("dataDoc","2013-02-14");
//	//	adoc.setDataEsecutivita("2013-06-23");
////		jsontest.put("dataEsecutivita","2013-02-20");
//		adoc.setIdDoc("1234");
////		jsontest.put("idDoc","1234");
//		adoc.setNumero("0088574");
////		jsontest.put("numero","0000344");
//		adoc.setOggettoPub("Esposizione all'Albo aziendale dell'istanza di trasferimento della Farmacia delle Terme, da piazza Libertà n. 55, a Porretta, ai locali posti in piazza Libertà n. 48 del Comune di Porretta.");
////		jsontest.put("oggettoPub","TEST pubblicazione");
//		adoc.setRegistro("Protocollo Generale");
////		jsontest.put("registro","PROTOCOLLO GENERALE");
//		adoc.setStrutturaPubblicante("Uoc Ass Farmaceutica Territ E Vigilanza");
////		jsontest.put("strutturaPubblicante","Struttura di pubblicazione");
//		adoc.setTipoDocumento("PROTOCOLLO");
////		jsontest.put("tipoDocumento","DELIBERA");
//		adoc.setArticolazione("Uoc Ass Farmaceutica Territ E Vigilanza");
////		jsontest.put("articolazione","ginocchio");
//		adoc.setNumRegistro("0088574");
////		jsontest.put("numRegistro","2");
//		adoc.setUtente("doc_am_ut");
//		adoc.setPassword("la password");
//		//adoc.setUuid("1234-1231-12313");
//		//jsontest.put("uuid","1234-1231-12313");
//		adoc.setValidita("esecutiva");
////		jsontest.put("validita","esecutiva");
//		JSONArray jmods=new JSONArray();
//		JSONObject jmod=new JSONObject();
//		/*jmod.put("op","annulla");
//		jmod.put("ldoc",docs);
//		jmods.add(jmod);
//		jsontest.put("ops",jmods);*/
//		//System.out.println(jsontest);
//		//if (1==1) return;
//	//	ArrayList<AlboMod> mods=new ArrayList<AlboMod>();
//		//mods.add(amod);
//		//adoc.setMods(mods);

        adoc.setAl("2014-11-01");
        adoc.setDal("2014-10-31");
        adoc.setAnno("2014");
        adoc.setDataAdozione("2014-10-31");
        adoc.setDataDoc("2014-10-31");

        adoc.setIdDoc("1234");

        adoc.setNumero("0000001");
        adoc.setOggettoPub("Test Pubblicazione albo con trasparenza");
        adoc.setRegistro("Protocollo Generale");
        adoc.setStrutturaPubblicante("Base delle Scienze");
        adoc.setTipoDocumento("PROTOCOLLO");
        adoc.setArticolazione("Base delle scienze Art");
        adoc.setNumRegistro("0000001");
        // adoc.setUtente("doc_am_ut");
        // adoc.setPassword("la password");
        adoc.setValidita("esecutiva");

        adoc.setContenuto("Contenuto trasparente");
        adoc.setEventualeSpesaPrevista("1000");
        adoc.setEstremiDocumentiRelativi("Id doc relativi");
        adoc.setTipoProvvedimento("Autorizzazione");

        ArrayList<AlboAllegato> aa = new ArrayList<AlboAllegato>();
        aa.add(new AlboAllegato("Time Functions and Operators.pdf", new File("/tmp/Time Functions and Operators.pdf")));
        //aa.add(new AlboAllegato("test2.pdf","2","test2.pdf",new File("/tmp/test2.pdf")));
        adoc.setAllegati(aa);

        AlboManager am = new AlboManager(url, "utente", "utente");//, "doc_am_ut", "Nu5eiTho");
        try {
            String res = am.pubDocMulipart(adoc);
            System.out.println(res);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
