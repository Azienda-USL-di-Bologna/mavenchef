package it.bologna.ausl.utils.albo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author andrea
 */
public class DatiTrasparenza implements AlboPubblicabile {

    /*
     L'url dello script per la pubblicazione è 
     http://vm-zope-dev2:8380/eauslbo/albo_pubblica_PROT.
     L'url  dove finiscono i documenti creati è 
     http://vm-zope-dev2:8380/eauslbo/amministrazione-trasparente/provvedimenti-1

     */
    /*
    
     oggetto_pub
     registro
     num_registro
     anno
     articolazione
     validita
     data_reg
     data_adozione
     data_esecutivita
     tipo_provvedimento (stringa)
     contenuto (stringa 600 caratteri)
     eventuale_spesa_prevista (numerico)
     estremi_documenti_relativi (stringa)

     */
    private String oggettoPub, registro;
    private String numRegistro, numero, anno, articolazione, dataAdozione, validita, idDoc, dataDoc;
    //TRASPARENZA
    private String tipoProvvedimento, contenuto, eventualeSpesaPrevista, estremiDocumentiRelativi;
    private String dataEsecutivita, utente, password;

    public DatiTrasparenza() {

    }

    @Override
    public List<NameValuePair> getFormValues() throws UnsupportedEncodingException {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();

        formparams.add(new BasicNameValuePair("oggetto_pub", this.oggettoPub));
        formparams.add(new BasicNameValuePair("registro", this.registro));
        formparams.add(new BasicNameValuePair("num_registro", this.numRegistro));
        formparams.add(new BasicNameValuePair("numero", this.numero));
        formparams.add(new BasicNameValuePair("anno", this.anno));
        formparams.add(new BasicNameValuePair("validita", this.validita));
        formparams.add(new BasicNameValuePair("id_doc", this.idDoc));
        formparams.add(new BasicNameValuePair("articolazione", this.articolazione));
        formparams.add(new BasicNameValuePair("data_doc", this.dataDoc));
        formparams.add(new BasicNameValuePair("data_adozione", this.dataAdozione));
        formparams.add(new BasicNameValuePair("data_esecutivita", this.dataEsecutivita));
        formparams.add(new BasicNameValuePair("__ac_name", this.utente));
        formparams.add(new BasicNameValuePair("__ac_password", this.password));

        //TRASPARENZA
        formparams.add(new BasicNameValuePair("tipo_provvedimento", this.tipoProvvedimento));
        formparams.add(new BasicNameValuePair("contenuto", this.contenuto));
        formparams.add(new BasicNameValuePair("eventuale_spesa_prevista", this.eventualeSpesaPrevista));
        formparams.add(new BasicNameValuePair("estremi_documenti_relativi", this.estremiDocumentiRelativi));
        return formparams;
    }

    @Override
    public List<AlboAllegato> getAllegati() {
        return null;
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

    public String getNumRegistro() {
        return numRegistro;
    }

    public void setNumRegistro(String numRegistro) {
        this.numRegistro = numRegistro;
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

    public String getArticolazione() {
        return articolazione;
    }

    public void setArticolazione(String articolazione) {
        this.articolazione = articolazione;
    }

    public String getDataAdozione() {
        return dataAdozione;
    }

    public void setDataAdozione(String dataAdozione) {
        this.dataAdozione = dataAdozione;
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

    public String getDataDoc() {
        return dataDoc;
    }

    public void setDataDoc(String dataDoc) {
        this.dataDoc = dataDoc;
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

    public String getDataEsecutivita() {
        return dataEsecutivita;
    }

    public void setDataEsecutivita(String dataEsecutivita) {
        this.dataEsecutivita = dataEsecutivita;
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

}
