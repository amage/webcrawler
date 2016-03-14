package org.playstat.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Iurii Shchekochikhin <amagex@gmail.com> on 14.03.16.
 */
public class HTTPResponse {
    private final String status;
    private final Map<String,List<String>> header;
    private final String body;

    public HTTPResponse(String status, Map<String, List<String>> header, String body) {
        this.status = status;
        this.header = header;
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HTTPResponse{" +
                "status='" + status + '\'' +
                ", header=" + header +
                ", body='" + body + '\'' +
                '}';
    }
}
