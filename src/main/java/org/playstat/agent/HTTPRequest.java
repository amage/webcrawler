package org.playstat.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPRequest {
    private final String method;
    private final String  url;
    private final Map<String,List<String>> header;
    private final String body;

    public HTTPRequest(String method, String url, Map<String, List<String>> header, String body) {
        this.method = method;
        this.url = url;
        this.header = header;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<String>> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HTTPRequest{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", header=" + header +
                ", body='" + body + '\'' +
                '}';
    }
}
