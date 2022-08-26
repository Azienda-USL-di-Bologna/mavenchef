package it.bologna.ausl.utils.albo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class AlboMod {

    public enum Operation {

        ANNULLA, INTEGRA, AGGIUNGI_TRASPARENZA
    }
    private Operation operation;
    private List<String> doclist;
    private String stringop;

    public AlboMod(Operation op, List<String> doclist) {
        this.operation = op;
        this.doclist = doclist;
        switch (op) {
            case ANNULLA:
                this.stringop = "annulla";
                break;
            case INTEGRA:
                this.stringop = "integra";
                break;
        }

    }

    public AlboMod() {
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
        switch (this.operation) {
            case ANNULLA:
                this.stringop = "annulla";
                break;
            case INTEGRA:
                this.stringop = "integra";
                break;
        }
    }

    public void setDoclist(List<String> doclist) {
        this.doclist = doclist;
    }

    public void setStringop(String stringop) {
        this.stringop = stringop;
    }

    public Operation getOperation() {
        return operation;
    }

    public List<String> getDoclist() {
        return doclist;
    }

    public String getStringop() {
        return stringop;
    }

    public List<NameValuePair> getFormValues() throws UnsupportedEncodingException {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("doc_list.op:records", this.stringop));
        for (String s : doclist) {
            formparams.add(new BasicNameValuePair("doc_list.ldoc:list:records", s));
        }
        return formparams;
    }
}
