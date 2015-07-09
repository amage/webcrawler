package org.playstat.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

public interface IAgent {

    InputStream go(URL url) throws IOException;

    void setProxy(Proxy proxy);

    void addCookie(String host, String name, String value);

    void addHeader(String key, String value);

    void removeHeader(String key);

    String getCookie(String host, String name);

    InputStream post(URL url, Map<String, String> params) throws IOException;

    void setCharset(String charset);
}
