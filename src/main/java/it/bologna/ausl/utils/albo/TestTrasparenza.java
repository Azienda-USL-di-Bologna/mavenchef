package it.bologna.ausl.utils.albo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class TestTrasparenza {

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
//        BasicConfigurator.configure();
//        Logger.getLogger("org.apache.http").setLevel(Level.WARNING);

        String url = "http://vm-zope-dev2:8380/eauslbo/albo_pubblica_PROT";
        DatiTrasparenza adoc = new DatiTrasparenza();

        adoc.setAnno("2014");
        adoc.setDataAdozione("2014-10-30");
        adoc.setDataDoc("2014-10-30");
        adoc.setDataEsecutivita("2014-10-30");
        adoc.setIdDoc("1234");
        

        adoc.setNumero("0000001");
        adoc.setOggettoPub("Test Pubblicazione SOLO trasparenza");
        adoc.setRegistro("Protocollo Generale");

        adoc.setArticolazione("Base delle scienze Art");
        adoc.setNumRegistro("0000002");
        // adoc.setUtente("doc_am_ut");
        // adoc.setPassword("la password");
        adoc.setValidita("esecutiva");
        adoc.setContenuto("Contenuto trasparente");
        adoc.setEventualeSpesaPrevista("1000");
        adoc.setEstremiDocumentiRelativi("Id doc relativi");
        adoc.setTipoProvvedimento("Autorizzazione");
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
