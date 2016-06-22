package org.playstat.agent;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Iurii Shchekochikhin <amagex@gmail.com> on 14.03.16.
 */
public class HTTPResponse {
    private final int responseCode;
    private final Map<String,List<String>> header;
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

    @Override
    public String toString() {
        return "HTTPResponse{" +
                "status='" + responseCode + '\'' +
                ", header=" + header +
                ", body='" + body + '\'' +
                '}';
    }
}
