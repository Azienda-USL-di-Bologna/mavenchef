package it.bologna.ausl.utils.albo;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.apache.http.NameValuePair;

/**
 *
 * @author andrea
 */
public interface AlboPubblicabile {

    public List<NameValuePair> getFormValues() throws UnsupportedEncodingException;

    public List<AlboAllegato> getAllegati();

    public void setUtente(String utente);

    public void setPassword(String password);

}
