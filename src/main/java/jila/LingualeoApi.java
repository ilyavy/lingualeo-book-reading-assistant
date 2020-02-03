package jila;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

/**
 * Provide the way to work with lingualeo api.
 */
public class LingualeoApi {
    private static final String LLDOMAIN = "https://api.lingualeo.com/";
    private Connector conn;
    private String cookie;

    /**
     * Create LingualeoApi object,
     * using default connector (with no proxy).
     */
    public LingualeoApi() {
        this(createConnector());
    }

    /**
     * Create LingualeoApi object with specified connector.
     * Connector provides the way to control network connection.
     *
     * @param connector
     */
    public LingualeoApi(final Connector connector) {
        conn = connector;
    }

    /**
     * Create LingualeoApi object, using cookie.
     * Invoke of login method is not necessary after that.
     *
     * @param connector - specified connector.
     * @param cookie    - cookie string.
     */
    public LingualeoApi(final Connector connector, final String cookie) {
        this(connector);
        this.cookie = cookie;
    }

    /**
     * If credentials were saved before in cookie, login can be done,
     * using that cookie.
     *
     * @param cookie - String, containing user's credentials. The main key is
     *               remember. Any other keys are not necessary.
     * @return JsonObject, containing user's profile data.
     */
    public JsonObject login(final String cookie) {
        this.cookie = cookie;
        String link = LLDOMAIN + "api/login";
        // TODO: implement
        String res = "{\"error_msg\":\"\",\"user\":" +
                "{\"user_id\":3488533,\"nickname\":\"ilvolos\",\"xp_level\":13,\"hungry_pct\":0," +
                "\"words_cnt\":1057,\"words_known\":6765}}";
        JsonReader jreader = Json.createReader(new StringReader(res));
        JsonObject jobj = jreader.readObject();
        return jobj;
    }

    /**
     * Authenticate user, using his email and password.
     *
     * @param email    - user's email
     * @param password - user's password. Should not be encrypted.
     * @return JsonObject, containing user's profile data.
     */
    public JsonObject login(final String email, final String password) {
        String link = LLDOMAIN + "login?email=" + email + "&password=" + password; // TODO: use WebFlux
        System.out.println(link);
        HttpURLConnection urlConn = conn.getResponse(link, null);
        cookie = conn.extractCookie(urlConn);
        return conn.extractData(urlConn);
    }

    /**
     * Returns a translation of the specified word.
     *
     * @param word word to translate
     * @return
     */
    public String getTranslate(String word) {
        String link = LLDOMAIN + "gettranslates?word=" + word;
        JsonObject jobj = conn.extractData(
                conn.getResponse(link, cookie));

        JsonObject tran = jobj.getJsonArray("translate").getJsonObject(0);
        return tran.getString("value");
    }

    /**
     * Adds the specified word with a translation and a context into user's dictionary.
     *
     * @param word      word to add to a dictionary
     * @param translate it's translation
     * @param context   it's context
     */
    public void addWord(String word, String translate, String context) {
        try {
            translate = URLEncoder.encode(translate, "UTF-8");
            context = URLEncoder.encode(context, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String link = LLDOMAIN + "addword?word=" + word +
                "&translate=" + translate + "&context=" + context;
        System.out.println("add word: " + link);
        JsonObject jobj = conn.extractData(
                conn.getResponse(link, cookie));
        System.out.println("added word: " + jobj);
    }


    /**
     * Create default connector.
     * Usual HttpURLConnection will be used, without proxy.
     *
     * @return connector object.
     */
    public static Connector createConnector() {
        return new ConcreteConnector();
    }

    /**
     * Create connector with proxy without proxy
     * authentication needed.
     *
     * @param proxyType - HTTP/HTTPS/SOCKS
     * @param proxyHost - proxy server's ip-address
     * @param proxyPort - proxy server's port
     * @return connector object.
     */
    public static Connector createConnector(final String proxyType,
                                            final String proxyHost, final int proxyPort) {
        return new ConcreteConnector(proxyType, proxyHost, proxyPort);
    }

    /**
     * Interface for connector.
     */
    public interface Connector {
        HttpURLConnection getResponse(String link, String cookie);

        String extractCookie(HttpURLConnection urlConn);

        JsonObject extractData(HttpURLConnection urlConn);
    }

    /**
     * Connector is a wrapper for HttpURLConnection,
     * that provides the way for LingualeoApi object to work with network.
     * It can use specified proxy or work directly, without it.
     */
    private static class ConcreteConnector implements Connector {
        private static final int TIMEOUT = 1000;
        private String proxyType = null;
        private String proxyHost;
        private int proxyPort;

        ConcreteConnector() {
        }

        /**
         * Create connector with specified proxy settings.
         *
         * @param proxyType - HTTP/HTTPS/SOCKS
         * @param proxyHost - proxy server's ip-address
         * @param proxyPort - proxy server's port
         */
        ConcreteConnector(final String proxyType, final String proxyHost, final int proxyPort) {
            this();
            this.proxyType = proxyType;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }

        /**
         * Get response from the specified url.
         *
         * @param link - string with url.
         * @return - JsonObject, that contains the server's answer.
         */
        public HttpURLConnection getResponse(final String link, final String cookie) {
            HttpsURLConnection urlConn = null;
            URL url;

            try {
                url = new URL(link);
                if (proxyType != null) {
                    SocketAddress socketAddress = new InetSocketAddress(proxyHost, proxyPort);
                    Proxy httpProxy = new Proxy(
                            proxyType.equals("HTTP") ? Proxy.Type.HTTP : Proxy.Type.SOCKS,
                            socketAddress);
                    urlConn = (HttpsURLConnection) url.openConnection(httpProxy);
                } else {
                    urlConn = (HttpsURLConnection) url.openConnection();
                }

                StringBuilder sb = new StringBuilder();
                // necessary
                sb.append("remember=153b3500f876710c9ba2fbea030d95d975b3f528f2e299e10f7e35ded9347bcd8925398f6f61fb87;");

                //Cookie
                if (cookie != null && !cookie.isEmpty()) {
                    urlConn.setRequestProperty("Cookie", cookie);
                    urlConn.setConnectTimeout(TIMEOUT);
                    urlConn.connect();
                }

                System.out.println("connection is established");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return urlConn;
        }


        public String extractCookie(HttpURLConnection urlConn) {
            String result = null;

            StringBuilder sb;
            sb = new StringBuilder();
            String remember = "";
            String headerName = null;
            for (int i = 1; (headerName = urlConn.getHeaderFieldKey(i)) != null; i++) {
                String headerField = urlConn.getHeaderField(i);
                /**
                 if (headerField.length() > 30 && headerField.startsWith("remember")) {
                 String[] tokens = headerField.substring(9).split(";");
                 remember = tokens[0];
                 System.out.println("REMEMBER: " + remember);
                 }
                 */
                if (headerName.equals("Set-Cookie")) {
                    sb.append(";").append(urlConn.getHeaderField(i));
                }
            }

            result = sb.toString();
            System.out.println(result);
            return result;
        }


        public JsonObject extractData(HttpURLConnection urlConn) {
            JsonObject jobj = null;

            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder sb = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
                in.close();

                System.out.println("data extracted: " + sb.toString());

                JsonReader jreader = Json.createReader(
                        new StringReader(sb.toString()));
                jobj = jreader.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(jobj);
            return jobj;
        }
    }
}
