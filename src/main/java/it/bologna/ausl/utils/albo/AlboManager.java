package it.bologna.ausl.utils.albo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

public class AlboManager {

    private String url, username, password;
    private HttpClient c;

    public AlboManager(String url) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
        this.url = url;
        trustAllHostNameAndCertificate(new URL(this.url).getHost());
        this.c = new DefaultHttpClient();
    }

    public AlboManager(String url, String username, String password) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
        this.url = url;
        trustAllHostNameAndCertificate(new URL(this.url).getHost());
        this.username = username;
        this.password = password;
        this.c = new DefaultHttpClient();
    }

    public void trustAllHostNameAndCertificate(final String hostNameToTrust) throws NoSuchAlgorithmException, KeyManagementException {

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                if (hostNameToTrust != null) {
                    if (hostname.equals("localhost") || hostname.equals(hostNameToTrust)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (hostname.equals("localhost")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        });

        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, java.lang.String str) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, java.lang.String str) {
                }
            }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        HttpClient client = new DefaultHttpClient();
        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme("https", socketFactory, 8443));
        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
        HttpClient http = new DefaultHttpClient(mgr, client.getParams());

//        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    public boolean pubDoc(AlboPubblicabile doc) throws ClientProtocolException, IOException {
        if (this.username != null && this.password != null) {
            doc.setUtente(username);
            doc.setPassword(password);
        }
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams = doc.getFormValues();
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        HttpPost httppost = new HttpPost(this.url);
        httppost.setEntity(entity);
        c.getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
        );
        HttpResponse response = c.execute(httppost);
        HttpEntity res = response.getEntity();
        return true;

    }

    public String pubDocMulipart(AlboPubblicabile doc) throws ClientProtocolException, IOException {
        if (this.username != null && this.password != null) {
            doc.setUtente(username);
            doc.setPassword(password);
        }
        MultipartEntity me = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams = doc.getFormValues();
        for (NameValuePair nvp : formparams) {
            if (nvp.getValue() != null) {
                me.addPart(nvp.getName(), new StringBody(nvp.getValue(), Charset.forName("UTF-8")));
            } else {
                System.out.println(nvp.getName() + " non valorizzato!");
            }
        }
        if (doc.getAllegati() != null) {
            List<AlboAllegato> aa = doc.getAllegati();
            for (AlboAllegato a : aa) {
//                me.addPart("allegatifile:list",new InputStreamBody(a.getIs(),"application/pdf",a.getNome()));
                me.addPart("allegatifile:list", new FileBody(a.getFile(), a.getNome(), "application/pdf", "UTF-8"));
            }

        }
        HttpPost httppost = new HttpPost(this.url);
        httppost.setEntity(me);
        c.getConnectionManager().getSchemeRegistry().register(
            new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
        );
        HttpResponse response = c.execute(httppost);
        HttpEntity res = response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.writeTo(baos);
        return baos.toString();
    }
}
