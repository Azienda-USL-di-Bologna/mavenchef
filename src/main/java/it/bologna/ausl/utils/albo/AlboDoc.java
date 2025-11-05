package it.bologna.ausl.utils.albo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class AlboDoc implements AlboPubblicabile {

    private String oggettoPub, registro, tipoDocumento;
    private String numero, anno, validita, idDoc, strutturaPubblicante, dataDoc, dataAdozione, numRegistro, articolazione;
    //TRASPARENZA
    private String tipoProvvedimento, contenuto, eventualeSpesaPrevista, estremiDocumentiRelativi;
    private String dataEsecutivita, dal, al, utente, password;
    private ArrayList<AlboAllegato> allegati = null;
    private ArrayList<AlboMod> mods = null;

    public AlboDoc() {

    }

    @Override
    public List<NameValuePair> getFormValues() throws UnsupportedEncodingException {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        /*	if (allegati != null){
         for (AlboAllegato a : allegati){
         formparams.addAll(a.getFormValues());
         }
         }*/
        if (mods != null) {
            for (AlboMod m : mods) {
                formparams.addAll(m.getFormValues());
            }
        }
        formparams.add(new BasicNameValuePair("oggetto_pub", this.oggettoPub));
        formparams.add(new BasicNameValuePair("registro", this.registro));
        formparams.add(new BasicNameValuePair("num_registro", this.numRegistro));
        formparams.add(new BasicNameValuePair("tipo_documento", this.tipoDocumento));
        formparams.add(new BasicNameValuePair("numero", this.numero));
        formparams.add(new BasicNameValuePair("anno", this.anno));
        formparams.add(new BasicNameValuePair("validita", this.validita));
        formparams.add(new BasicNameValuePair("id_doc", this.idDoc));
        formparams.add(new BasicNameValuePair("struttura_pubblicante", this.strutturaPubblicante));
        formparams.add(new BasicNameValuePair("articolazione", this.articolazione));
        formparams.add(new BasicNameValuePair("data_doc", this.dataDoc));
        formparams.add(new BasicNameValuePair("data_adozione", this.dataAdozione));
        formparams.add(new BasicNameValuePair("data_esecutivita", this.dataEsecutivita));
        formparams.add(new BasicNameValuePair("dal", this.dal));
        formparams.add(new BasicNameValuePair("al", this.al));
        formparams.add(new BasicNameValuePair("__ac_name", this.utente));
        formparams.add(new BasicNameValuePair("__ac_password", this.password));

        //TRASPARENZA
        formparams.add(new BasicNameValuePair("tipo_provvedimento", this.tipoProvvedimento));
        formparams.add(new BasicNameValuePair("contenuto", this.contenuto));
        formparams.add(new BasicNameValuePair("eventuale_spesa_prevista", this.eventualeSpesaPrevista));
        formparams.add(new BasicNameValuePair("estremi_documenti_relativi", this.estremiDocumentiRelativi));
        return formparams;
    }

    public String getOggettoPub() {
        return oggettoPub;
    }

    public void setOggettoPub(String oggettoPub) {
        this.oggettoPub = oggettoPub;
    }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getAnno() {
        return anno;
    }

    public void setAnno(String anno) {
        this.anno = anno;
    }

    public String getValidita() {
        return validita;
    }

    public void setValidita(String validita) {
        this.validita = validita;
    }

    public String getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(String idDoc) {
        this.idDoc = idDoc;
    }

    public String getStrutturaPubblicante() {
        return strutturaPubblicante;
    }

    public void setStrutturaPubblicante(String strutturaPubblicante) {
        this.strutturaPubblicante = strutturaPubblicante;
    }

    public String getDataDoc() {
        return dataDoc;
    }

    public void setDataDoc(String dataDoc) {
        this.dataDoc = dataDoc;
    }

    public String getDataAdozione() {
        return dataAdozione;
    }

    public void setDataAdozione(String dataAdozione) {
        this.dataAdozione = dataAdozione;
    }

    public String getDataEsecutivita() {
        return dataEsecutivita;
    }

    public void setDataEsecutivita(String dataEsecutivita) {
        this.dataEsecutivita = dataEsecutivita;
    }

    public String getDal() {
        return dal;
    }

    public void setDal(String dal) {
        this.dal = dal;
    }

    public String getAl() {
        return al;
    }

    public void setAl(String al) {
        this.al = al;
    }

    @Override
    public ArrayList<AlboAllegato> getAllegati() {
        return allegati;
    }

    public void setAllegati(ArrayList<AlboAllegato> allegati) {
        this.allegati = allegati;
    }

    public ArrayList<AlboMod> getMods() {
        return mods;
    }

    public void setMods(ArrayList<AlboMod> mods) {
        this.mods = mods;
    }

    public String getNumRegistro() {
        return numRegistro;
    }

    public void setNumRegistro(String numRegistro) {
        this.numRegistro = numRegistro;
    }

    public String getArticolazione() {
        return articolazione;
    }

    public void setArticolazione(String articolazione) {
        this.articolazione = articolazione;
    }

    public String getUtente() {
        return utente;
    }

    @Override
    public void setUtente(String utente) {
        this.utente = utente;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public String getTipoProvvedimento() {
        return tipoProvvedimento;
    }

    public void setTipoProvvedimento(String tipoProvvedimento) {
        this.tipoProvvedimento = tipoProvvedimento;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public String getEventualeSpesaPrevista() {
        return eventualeSpesaPrevista;
    }

    public void setEventualeSpesaPrevista(String eventualeSpesaPrevista) {
        this.eventualeSpesaPrevista = eventualeSpesaPrevista;
    }

    public String getEstremiDocumentiRelativi() {
        return estremiDocumentiRelativi;
    }

    public void setEstremiDocumentiRelativi(String estremiDocumentiRelativi) {
        this.estremiDocumentiRelativi = estremiDocumentiRelativi;
    }

}
