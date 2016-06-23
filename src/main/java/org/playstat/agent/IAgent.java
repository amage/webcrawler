package org.playstat.agent;

import java.io.IOException;
import java.net.Proxy;

public interface IAgent {

    HTTPResponse go(Transaction t) throws IOException;

    // State
    void addCookie(String host, String name, String value);
    String getCookie(String host, String name);

    // Settings
    void setProxy(Proxy proxy);
    void setCharset(String charset);
}
