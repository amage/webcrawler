package org.playstat.agent.nullagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.playstat.agent.HTTPResponse;
import org.playstat.agent.IAgent;
import org.playstat.agent.ICookiesStorage;
import org.playstat.agent.RequestMethod;
import org.playstat.agent.Transaction;
import org.playstat.crawler.WebClientSettings;

public class NullAgent implements IAgent {
    private final static int MAX_REDIRECTS = 32;
    private int redirects = 0;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ICookiesStorage cookiesStorage;
    private WebClientSettings settings;

    private String language;
    private Proxy proxy;
    private String userAgent;
    private String charset = "UTF-8";

    private final static int HISTORY_SIZE = 512;
    private final LinkedList<Transaction> history = new LinkedList<>();

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            // no-op
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            // no-op
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }};

    private int timeout = 1000 * 60 * 5;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public NullAgent(WebClientSettings settings) {
        setUserAgent(UserAgentFactory.generateString());
        setLanguage(String.join(",", settings.getLanguages()));
        this.cookiesStorage = new MemoryCookiesStorage();
    }

    @Override
    // TODO: threadsafe me!
    public HTTPResponse go(Transaction t) throws IOException {
        if (!history.isEmpty()) {
            t.addRequestParam("Referer", history.getFirst().getUrl());
        }
        putToHistory(t);

        logger.debug("go to " + t.getUrl());

        final URL url = new URL(t.getUrl());
        HttpURLConnection con;
        if (proxy != null) {
            con = (HttpURLConnection) url.openConnection(proxy);
        } else {
            con = (HttpURLConnection) url.openConnection();
        }

        con.setConnectTimeout(timeout);
        if ("https".equals(url.getProtocol())) {
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, NullAgent.TRUST_ALL_CERTS, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                ((javax.net.ssl.HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
            } catch (final Exception e) {
                throw new IOException(e);
            }
        }
        HttpURLConnection.setFollowRedirects(false);
        if (t.getMethod().equals(RequestMethod.POST)) {
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
        }
        prepareHeader(con, t);

        logHeader(con);

        if (t.getMethod().equals(RequestMethod.POST)) {
            final DataOutputStream out = new DataOutputStream(con.getOutputStream());
            final StringBuilder sb = new StringBuilder();
            for (final Entry<String, List<String>> e : t.getRequestParams().entrySet()) {
                for (final String value : e.getValue()) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(e.getKey());
                    sb.append("=");
                    sb.append(URLEncoder.encode(value, charset));
                }
            }
            out.writeBytes(sb.toString());
            out.flush();
        }
        logResponce(con);
        updateEncoding(con);

        // TODO: Status and body

        final String setCookieFieldName = findSetCookieFieldName(con);
        if (setCookieFieldName != null) {
            for (final String newCookies : con.getHeaderFields().get(setCookieFieldName)) {
                logger.debug("get new cookies: " + newCookies);
                if (newCookies.length() != 0) {
                    // set-cookie = "Set-Cookie:" cookies
                    // cookies = 1#cookie
                    // cookie = NAME "=" VALUE *(";" cookie-av)
                    // NAME = attr
                    // VALUE = value
                    // cookie-av = "Comment" "=" value
                    // | "Domain" "=" value
                    // | "Max-Age" "=" value
                    // | "Path" "=" value
                    // | "Secure"
                    // | "Version" "=" 1*DIGIT
                    // TODO: full cookie support
                    final String pairs[] = newCookies.split(";");
                    for (final String pair : pairs) {
                        final int ePos = pair.indexOf("=");
                        if (ePos == -1) {
                            // skip 'secure' and 'HttpOnly'
                            continue;
                        }
                        final String name = pair.substring(0, pair.indexOf("=")).trim();
                        // skip them too
                        if (name.equalsIgnoreCase("expires") || name.equalsIgnoreCase("domain")
                                || name.equalsIgnoreCase("path") || name.equalsIgnoreCase("max-age")
                                || name.equalsIgnoreCase("comment") || name.equalsIgnoreCase("version")) {
                            continue;
                        }
                        final String value = pair.substring(pair.indexOf("=") + 1);
                        addCookie(url.getHost(), name, value);
                    }
                }
            }
        }
        final HTTPResponse response = new HTTPResponse(con.getResponseCode(), con.getHeaderFields(),
                con.getInputStream());
        t.setResponse(response);
        if (con.getResponseCode() / 100 == 3) {
            con.disconnect();
            String dst = con.getHeaderField("Location");
            if (dst.startsWith("/")) {
                final String port = url.getPort() == 80 ? "" : ":" + url.getPort();
                dst = url.getProtocol() + "://" + url.getHost() + port + dst;
            }
            if (redirects < MAX_REDIRECTS) {
                redirects++;
            } else {
                redirects = 0;
                throw new IOException("Too many redirects");
            }
            return go(Transaction.create(dst));
        }
        redirects = 0;
        // TODO check gzip header;
        return t.getResponse();
    }

    private void updateEncoding(HttpURLConnection con) {
        String content = con.getHeaderField("Content-Type");
        if(content == null) {
            return;
        }
        String str = "charset=";
        String newCharset = content.substring((content.indexOf(str) + str.length()));
        int space = newCharset.indexOf(' ');
        if (space > 0) {
            newCharset = newCharset.substring(0, space);
        }
        setCharset(newCharset);
    }

    private void logHeader(HttpURLConnection con) {
        for (final Entry<String, List<String>> p : con.getRequestProperties().entrySet()) {
            logger.debug("RequestProperties: " + p.getKey() + " -> " + p.getValue());
        }
    }

    private void logResponce(HttpURLConnection con) {
        for (final Entry<String, List<String>> p : con.getHeaderFields().entrySet()) {
            logger.debug("HeaderFields: " + p.getKey() + " -> " + p.getValue());
        }
    }

    // TODO: move to request generator and cleanup.
    private void prepareHeader(HttpURLConnection con, Transaction t) {
        con.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        if (charset != null) {
            con.addRequestProperty("Accept-Charset", charset + ",*;q=0.5");
        } else {
            con.addRequestProperty("Accept-Charset", "UTF-8,*;q=0.5");
        }
        // con.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        // con.addRequestProperty("Accept-Encoding", "deflate");
        con.addRequestProperty("Accept-Language", getLanguage() + ";q=0.8");
        con.addRequestProperty("Cache-Control", "no-cache");
        con.addRequestProperty("Connection", "keep-alive");
        final String cookie = makeCookieString(con.getURL().getHost());
        if (!cookie.isEmpty()) {
            con.addRequestProperty("Cookie", cookie);
        }

        // TODO con.addRequestProperty("Host", "www.yandex.ru");
        con.addRequestProperty("Pragma", "no-cache");
        con.addRequestProperty("User-Agent", getUserAgent());

        for (final Entry<String, List<String>> e : t.getRequestParams().entrySet()) {
            for (final String value : e.getValue()) {
                con.addRequestProperty(e.getKey(), value);
            }
        }

    }

    private String makeCookieString(String host) {
        final Map<String, String> cookiesForHost = cookiesStorage.get(host);
        final StringBuilder sb = new StringBuilder();
        for (final Entry<String, String> cookie : cookiesForHost.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(cookie.getKey());
            sb.append("=");
            sb.append(cookie.getValue());
        }
        return sb.toString();
    }

    @Override
    public void addCookie(String host, String name, String value) {
        cookiesStorage.addCookie(host, name, value);
    }

    @Override
    public String getCookie(String host, String name) {
        return cookiesStorage.get(host).get(name);
    }

    private String findSetCookieFieldName(HttpURLConnection con) {
        for (final String headerName : con.getHeaderFields().keySet()) {
            if (headerName == null) {
                continue;
            }
            if ("Set-Cookie".equalsIgnoreCase(headerName.toLowerCase())) {
                return headerName;
            }
        }
        return null;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void setCharset(String charset) {
        this.charset = charset;
    }

    public LinkedList<Transaction> getHistory() {
        return history;
    }

    private void putToHistory(Transaction transaction) {
        if (history.size() > HISTORY_SIZE) {
            history.removeLast();
        }
        history.addFirst(transaction);
    }

}
