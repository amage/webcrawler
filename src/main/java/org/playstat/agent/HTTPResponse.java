package org.playstat.agent;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
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
        return Charset.forName(fix(newCharset));
    }

    private static Map<String, String> cpFixes = new HashMap<>();

    static {
        cpFixes.put("cp1251", "Windows-1251");
        cpFixes.put("windows-1251", "Windows-1251");
    }

    private static String fix(String newCharset) {
        if (cpFixes.containsKey(newCharset)) {
            return cpFixes.get(newCharset);
        }
        return newCharset;
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
