package org.playstat.agent;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/*
 * Created by Iurii Shchekochikhin <amagex@gmail.com> on 14.03.16.
 */
public class HTTPResponse {
    private final int responseCode;
    private final Map<String, List<String>> header;
    private final InputStream body;

    public HTTPResponse(int responseCode, Map<String, List<String>> header, InputStream body) {
        this.responseCode = responseCode;
        this.header = header;
        this.body = body;
    }


    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getHeader() {
        return header;
    }

    public InputStream getBody() {
        return body;
    }

    public Charset getCharset() {
        return getCharset(Charset.defaultCharset());
    }

    public Charset getCharset(Charset defaultCharset) {
        if (!header.containsKey("Content-Type")) {
            return defaultCharset;
        }
        String content = header.get("Content-Type").get(0);
        String str = "charset=";
        int indexOfCharset = content.indexOf(str);
        if (indexOfCharset < 0) {
            return defaultCharset;
        }
        String newCharset = content.substring((indexOfCharset + str.length()));
        int space = newCharset.indexOf(' ');
        if (space > 0) {
            newCharset = newCharset.substring(0, space);
        }
        return Charset.forName(newCharset);
    }

    @Override
    public String toString() {
        return "HTTPResponse{" +
                "status='" + responseCode + '\'' +
                ", header=" + header +
                ", body='" + body + '\'' +
                '}';
    }
}
