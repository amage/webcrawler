package org.playstat.agent.nullagent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.playstat.agent.HTTPResponse;
import org.playstat.agent.IAgent;
import org.playstat.agent.ICookiesStorage;
import org.playstat.agent.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullAgent implements IAgent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ICookiesStorage cookiesStorage;

    private String language;
    private Proxy proxy;
    private String userAgent;
    private String charset;

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { new X509TrustManager() {
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
    } };

    public NullAgent() {
        setUserAgent(UserAgentFactory.generateString());
        setLanguage("en,ru");
        this.cookiesStorage = new MemoryCookiesStorage();
    }

    @Override
    public InputStream go(Transaction t) throws IOException {
        logger.debug("go to " + t.getUrl());

        final URL url = new URL(t.getUrl());
        HttpURLConnection con;
        if (proxy != null) {
            con = (HttpURLConnection) url.openConnection(proxy);
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        con.setConnectTimeout(1000 * 60 * 5);
        if (url.getProtocol().equals("https")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, NullAgent.TRUST_ALL_CERTS, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                ((javax.net.ssl.HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        HttpURLConnection.setFollowRedirects(false);

        prepareHeader(con, t);

        logHeader(con);
        logResponce(con);

        // TODO: Status and body
        HTTPResponse response = new HTTPResponse("", con.getHeaderFields(), "");
        t.setResponse(response);

        final String setCookieFieldName = findSetCookieFieldName(con);
        if (setCookieFieldName != null) {
            for (String newCookies : con.getHeaderFields().get(setCookieFieldName)) {
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
                    for (String pair : pairs) {
                        final int ePos = pair.indexOf("=");
                        if (ePos == -1) {
                            // skip 'secure' and 'HttpOnly'
                            continue;
                        }
                        final String name = pair.substring(0, pair.indexOf("=")).trim();
                        // skip them too
                        if (name.equalsIgnoreCase("expires") || name.equalsIgnoreCase("domain") || name.equalsIgnoreCase("path")
                                || name.equalsIgnoreCase("max-age") || name.equalsIgnoreCase("comment")
                                || name.equalsIgnoreCase("version")) {
                            continue;
                        }
                        String value = pair.substring(pair.indexOf("=") + 1);
                        addCookie(url.getHost(), name, value);
                    }
                }
            }
        }
        if (con.getResponseCode() / 100 == 3) {
            con.disconnect();
            String dst = con.getHeaderField("Location");
            if (dst.startsWith("/")) {
                final String port = url.getPort() == 80 ? "" : ":" + url.getPort();
                dst = url.getProtocol() + "://" + url.getHost() + port + dst;
            }
            return go(Transaction.create(dst));
        }
        // TODO check gzip header;
        return con.getInputStream();
    }

    private void logHeader(HttpURLConnection con) {
        for (Entry<String, List<String>> p : con.getRequestProperties().entrySet()) {
            logger.debug("RequestProperties: " + p.getKey() + " -> " + p.getValue());
        }
    }

    private void logResponce(HttpURLConnection con) {
        for (Entry<String, List<String>> p : con.getHeaderFields().entrySet()) {
            logger.debug("HeaderFields: " + p.getKey() + " -> " + p.getValue());
        }
    }

    // TODO: move to request generator.
    private void prepareHeader(HttpURLConnection con, Transaction t) {
        con.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        if (charset != null) {
            con.addRequestProperty("Accept-Charset", charset + ",*;q=0.5");
        } else {
            con.addRequestProperty("Accept-Charset", "UTF-8,*;q=0.5");
        }
        // con.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        // con.addRequestProperty("Accept-Encoding", "deflate");
        con.addRequestProperty("Accept-Language", language + ";q=0.8");
        con.addRequestProperty("Cache-Control", "no-cache");
        con.addRequestProperty("Connection", "keep-alive");
        String cookie = makeCookieString(con.getURL().getHost());
        if (cookie != null && cookie.length() > 0) {
            con.addRequestProperty("Cookie", cookie);
        }
        con.addRequestProperty("Host", "www.yandex.ru");
        con.addRequestProperty("Pragma", "no-cache");
        con.addRequestProperty("User-Agent", getUserAgent());

        for (Entry<String, List<String>> e : t.getRequestParams().entrySet()) {
            for (String value : e.getValue()) {
                con.addRequestProperty(e.getKey(), value);
            }
        }

    }

    private String makeCookieString(String host) {
        Map<String, String> cookiesForHost = cookiesStorage.get(host);
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> cookie : cookiesForHost.entrySet()) {
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
    public InputStream post(Transaction t) throws IOException {
        logger.debug("go to " + t.getUrl());

        final URL url = new URL(t.getUrl());
        HttpURLConnection con;
        if (proxy != null) {
            con = (HttpURLConnection) url.openConnection(proxy);
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        if (url.getProtocol().equals("https")) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, NullAgent.TRUST_ALL_CERTS, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                ((javax.net.ssl.HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        HttpURLConnection.setFollowRedirects(false);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);

        prepareHeader(con, t);
        logHeader(con);

        final DataOutputStream out = new DataOutputStream(con.getOutputStream());
        final StringBuilder sb = new StringBuilder();
        for (Entry<String, List<String>> e : t.getRequestParams().entrySet()) {
            for (String value : e.getValue()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(e.getKey());
                sb.append("=");
                sb.append(URLEncoder.encode(value, "UTF-8"));
            }
        }
        out.writeBytes(sb.toString());
        out.flush();

        logResponce(con);

        String newCookies = null;

        if ((newCookies = con.getHeaderField("Set-cookie")) != null) {
            logger.debug("get new cookies: " + newCookies);
            if (newCookies.length() != 0) {
                String pairs[] = newCookies.split(";");
                if (pairs.length > 0) {
                    String name = pairs[0].substring(0, pairs[0].indexOf("="));
                    String value = pairs[0].substring(pairs[0].indexOf("=") + 1);
                    addCookie(url.getHost(), name, value);
                }
            }
        }
        if (con.getResponseCode() / 100 == 3) {
            con.disconnect();
            return go(Transaction.create(con.getHeaderField("Location")));
        }
        return con.getInputStream();
    }

    public void addCookie(String host, String name, String value) {
        cookiesStorage.addCookie(host, name, value);
    }

    @Override
    public String getCookie(String host, String name) {
        return cookiesStorage.get(host).get(name);
    }

    private String findSetCookieFieldName(HttpURLConnection con) {
        for (String headerName : con.getHeaderFields().keySet()) {
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

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void setCharset(String charset) {
        this.charset = charset;
    }
}
