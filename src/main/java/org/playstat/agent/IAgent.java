package org.playstat.agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;

public interface IAgent {

    InputStream go(Transaction t) throws IOException;

    void setProxy(Proxy proxy);

    void addCookie(String host, String name, String value);

    String getCookie(String host, String name);

    InputStream post(Transaction t) throws IOException;

    void setCharset(String charset);
}
