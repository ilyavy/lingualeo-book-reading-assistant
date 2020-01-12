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
    private final static String LLDOMAIN = "https://api.lingualeo.com/";
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
     * @param connector
     */
    public LingualeoApi(final Connector connector) {
        conn = connector;
    }
    
    /**
     * Create LingualeoApi object, using cookie.
     * Invoke of login method is not necessary after that.
     * @param connector - specified connector.
     * @param cookie - cookie string.
     */
    public LingualeoApi(final Connector connector, final String cookie) {
        this(connector);
        this.cookie = cookie;
    }
    
    /**
     * If credentials were saved before in cookie, login can be done,
     * using that cookie.
     * @param cookie - String, containing user's credentials. The main key is
     * remember. Any other keys are not necessary.
     * @return JsonObject, containing user's profile data.
     */
    public JsonObject login(final String cookie) {
        this.cookie = cookie;
        String link = LLDOMAIN + "api/login";
        String res = "{\"error_msg\":\"\",\"user\":{\"user_id\":3488533,\"nickname\":\"ilvolos\",\"xp_level\":13,\"hungry_pct\":0,\"words_cnt\":1057,\"words_known\":6765}}";
        JsonReader jreader = Json.createReader(new StringReader(res));
        JsonObject jobj = jreader.readObject();
        return jobj;
    }
    
    /**
     * Authenticate user, using his email and password. 
     * @param email - user's email
     * @param password - user's password. Should not be encrypted.
     * @return JsonObject, containing user's profile data.
     */
    public JsonObject login(final String email, final String password) {
        String link = LLDOMAIN + "login?email=" + 
                email + "&password=" + password;
        System.out.println(link);
        HttpURLConnection urlConn = conn.getResponse(link, null);
        cookie = conn.extractCookie(urlConn);
        return conn.extractData(urlConn);
    }
    
    
    public String getTranslate(String word) {
        String link = LLDOMAIN + "gettranslates?word=" + word;
        JsonObject jobj = conn.extractData(
        		conn.getResponse(link, cookie));  
        
        JsonObject tran = jobj.getJsonArray("translate").getJsonObject(0);
        return tran.getString("value");
    }
    
    
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
     * @return connector object.
     */
    public static Connector createConnector() {
        return new ConcreteConnector();
    }
    
    /**
     * Create connector with proxy without proxy
     * authentication needed.
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
        public HttpURLConnection getResponse(final String link, final String cookie);
        public String extractCookie(final HttpURLConnection urlConn);
        public JsonObject extractData(final HttpURLConnection urlConn);
    }
    
    /**
     * Connector is a wrapper for HttpURLConnection,
     * that provides the way for LingualeoApi object to work with network.
     * It can use specified proxy or work directly, without it.
     */
    private static class ConcreteConnector implements Connector {
        private final static int TIMEOUT = 1000;
        private String pType = null;
        private String pHost;
        private int pPort;
        
        public ConcreteConnector() {
        }
        
        /**
         * Create connector with specified proxy settings.
         * @param proxyType - HTTP/HTTPS/SOCKS
         * @param proxyHost - proxy server's ip-address
         * @param proxyPort - proxy server's port
         */
        public ConcreteConnector(final String proxyType, final String proxyHost, final int proxyPort) {
            this();
            pType = proxyType;
            pHost = proxyHost;
            pPort = proxyPort;
        }
        
        /**
         * Get response from the specified url.
         * @param link - string with url.
         * @return - JsonObject, that contains the server's answer.
         */
        public HttpURLConnection getResponse(final String link, final String cookie) {
            String result = "";
            HttpsURLConnection urlConn = null;
            URL url;
            
            try {
                url = new URL(link);
                if (pType != null) {
                    SocketAddress addr = new InetSocketAddress(pHost, pPort);
                    Proxy.Type _pType = (pType.equals("HTTP") ? Proxy.Type.HTTP : Proxy.Type.SOCKS);
                    Proxy httpProxy = new Proxy(_pType, addr);
                    urlConn = (HttpsURLConnection) url.openConnection(httpProxy);
                } else {
                    urlConn = (HttpsURLConnection) url.openConnection();
                }

                StringBuilder sb = new StringBuilder();
                sb.append("remember=153b3500f876710c9ba2fbea030d95d975b3f528f2e299e10f7e35ded9347bcd8925398f6f61fb87;"); //neccessary
                
                //Cookie
                if (cookie != null && !cookie.isEmpty()) {
                	urlConn.setRequestProperty("Cookie", cookie);
                    urlConn.setConnectTimeout(TIMEOUT);
                    urlConn.connect();
                }
                
                System.out.println("connection is established");
                
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            return urlConn;
        }
        
        
        public String extractCookie(HttpURLConnection urlConn) {
        	String result = null;
        	
        	StringBuilder sb;
        	sb = new StringBuilder();
            String remember = "";
            String headerName=null;
            for (int i=1; (headerName = urlConn.getHeaderFieldKey(i)) != null; i++) {
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
    
    
    
    public static void main(String[] args) {
        System.out.println("started");
        //!!! Get request should be encoded
        //Connector cp = new Connector("HTTP", "192.168.5.12", 3128);
        //CheckProxy cp = new CheckProxy("https://api.lingualeo.com/api/login?email=iltero@rambler.ru&password=00ForLearn99");
        //System.out.println(cp.getResponse("http://api.lingualeo.com/addword?word=face&tword=%D0%BB%D0%B8%D1%86%D0%BE"));
        //System.out.println("finished");
        LingualeoApi leo = new LingualeoApi();
        JsonObject res;
        res = leo.login("ilvolos@rambler.ru", "nothingness");
        System.out.print("LOGIN");
        System.out.println(res);
        
        System.out.println("TRANSLATE");
        System.out.println(leo.getTranslate("this"));
        
        System.out.println("ADDWORD");
        leo.addWord("this", "���", "test");
    }
}
