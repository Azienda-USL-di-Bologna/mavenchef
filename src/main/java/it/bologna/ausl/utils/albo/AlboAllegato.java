package it.bologna.ausl.utils.albo;

import java.io.File;
import java.io.InputStream;

public class AlboAllegato {
private String nome;
private File file;
private InputStream is;

    public AlboAllegato(String nome,InputStream is) {
        this.nome=nome;		
        this.is=is;
    }

    public AlboAllegato(String nome,File file) {
        this.nome=nome;
        this.file=file;
    }

    public String getNome() {
        return nome;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /*public List<NameValuePair> getFormValues() throws UnsupportedEncodingException{
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("allegati.nome:records", this.nome));
        formparams.add(new BasicNameValuePair("allegati.id_doc:records", this.idDoc));
        formparams.add(new BasicNameValuePair("allegati.nome_file:records", this.nomeFile));
        return formparams;
    }*/
}
