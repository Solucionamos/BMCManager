package com.solucionamos.bmcmanager.connection;

import android.annotation.SuppressLint;

import com.solucionamos.bmcmanager.model.Sensor;

import org.apache.http.auth.AuthenticationException;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class IvbHttpsConnection implements BmcConnectionInterface {

    private static final int TIMEOUT = 1000;
    /*
     * Implementation for monitoring IVB servers (RD540 and 640)
     */
    private final String hostname;
    private final String username;
    private final String password;
    private final String protocol;

    private List<HttpCookie> cookies;

    /*
     * Constructor
     */
    public IvbHttpsConnection(String protocol, String hostname,
                              String username, String password) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.cookies = null;
    }

    /*
     * Configure the environment to accept self-signed certificates and
     * certificates with wrong hostname
     */
    @SuppressLint("TrulyRandom")
    private void trustAllCertificates() throws Exception {

        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) {
            }

            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) {
            }
        }};

        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
                .getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostValid = new HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostValid);
    }

    /*
     * Create a HTTPS URL object from the hostname and the given path
     */
    private URL createHttpsUrl(String protocol, String path)
            throws MalformedURLException {
        return new URL(protocol + "://" + hostname + path);
    }

    @Override
    public void connect() throws Exception {

		/* --- ACCEPT ANY CERTIFICATE --- */
        trustAllCertificates();

		/* --- PREPARE TO RECEIVE COOKIES --- */
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

		/* --- PREPARE THE REQUEST --- */
        URL url = createHttpsUrl(protocol, "/data/login");
        String urlParameters = "user=" + username + "&password=" + password
                + "&press=btnOK";

		/* --- SEND REQUEST --- */
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(
                conn.getOutputStream());
        writer.write(urlParameters);
        writer.flush();
        writer.close();

        conn.getContent();

		/* --- READ THE RECEIVED COOKIE --- */
        CookieStore cookieJar = cookieManager.getCookieStore();
        cookies = cookieJar.getCookies();

		/* --- GET RESPONSE --- */
        InputStream in = conn.getInputStream();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        HttpsConnectionSaxHandler handler = new HttpsConnectionSaxHandler();
        SAXParser parser = factory.newSAXParser();

        parser.parse(in, handler);
        if (handler.getAuthResult() != HttpsConnectionSaxHandler.AUTH_OK) {
            throw new AuthenticationException();
        }

        conn.disconnect();
    }

    @Override
    public void disconnect() throws Exception {
        URL url;
        HttpURLConnection conn;

        url = createHttpsUrl(protocol, "/data/logout");
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);

        // send the cookie received on connect()
        conn.setRequestProperty("Cookie", cookies.get(0).toString());
        conn.getContent();

        conn.disconnect();
    }

    public List<Sensor> getSensors(String type) throws Exception {
        List<Sensor> sensors;

        URL url = createHttpsUrl(protocol, "/data?get=" + type + "s");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.getContent();

		/* --- GET RESPONSE --- */
        InputStream in = conn.getInputStream();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SensorSaxHandler handler = new SensorSaxHandler();
        SAXParser parser = factory.newSAXParser();

        try {
            parser.parse(in, handler);
        } catch (SAXException sax) {
            Exception embed = sax.getException();
            embed.printStackTrace();
        }
        sensors = handler.getSensors();
        for (Sensor s : sensors) {
            s.setType(type);
        }

        conn.disconnect();

        return sensors;


    }

    @Override
    public int getPwState() throws Exception {
        int pwState;

        URL url = createHttpsUrl(protocol, "/data?get=pwState");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.getContent();

		/* --- GET RESPONSE --- */
        InputStream in = conn.getInputStream();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        PwStateSaxHandler handler = new PwStateSaxHandler();
        SAXParser parser = factory.newSAXParser();

        try {
            parser.parse(in, handler);
        } catch (SAXException sax) {
            Exception embed = sax.getException();
            embed.printStackTrace();
        }
        pwState = handler.getPwState();
        conn.disconnect();
        return pwState;
    }

    @Override
    public void setPwState(int state) throws Exception {
        URL url = createHttpsUrl(protocol,
                "/data?set=pwState:" + String.valueOf(state));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.getContent();
        conn.disconnect();
    }

}
