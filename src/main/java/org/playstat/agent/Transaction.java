package org.playstat.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction {
    private final HTTPRequest request;
    private HTTPResponse response = null;

    private long initTime;
    private long responseSetTime;

    public Transaction(HTTPRequest request) {
        this.request = request;
        initTime = System.currentTimeMillis();
    }

    public boolean isComplete() {
        return response != null;
    }
    public void addRequestParam(String key, String value) {
        if (isComplete()) {
            throw new IllegalStateException("Transaction in complete");
        }
        final Map<String, List<String>> requestParams = request.getHeader();
        if (!requestParams.containsKey(key)) {
            requestParams.put(key, new ArrayList<>());
        }
        requestParams.get(key).add(value);
        initTime = System.currentTimeMillis();
    }

    public String getUrl() {
        return request.getUrl();
    }

    public RequestMethod getMethod() {
        return request.getMethod();
    }

    public Map<String, List<String>> getRequestParams() {
        return request.getHeader();
    }

    public long getInitTime() {
        return initTime;
    }

    public void setInitTime(long initTime) {
        this.initTime = initTime;
    }

    public static Transaction create(String url) {
        final Transaction transaction = create(url, RequestMethod.GET, Collections.emptyMap(), "");
        transaction.setInitTime(System.currentTimeMillis());
        return transaction;
    }

    public static Transaction create(String url, RequestMethod method, Map<String, String> requestParams, String body) {
        final Map<String, List<String>> prep = new HashMap<>();
        requestParams.entrySet().stream().forEach(e -> {
            if (!prep.containsKey(e.getKey())) {
                prep.put(e.getKey(), new ArrayList<>());
            }
            prep.get(e.getKey()).add(e.getValue());
        });
        return new Transaction(new HTTPRequest(method, url, prep, body));
    }

    public HTTPResponse getResponse() {
        return response;
    }

    public void setResponse(HTTPResponse response) {
        this.responseSetTime = System.currentTimeMillis();
        this.response = response;
    }

    public long getResponseSetTime() {
        return responseSetTime;
    }

    public HTTPRequest getRequest() {
        return request;
    }
}
