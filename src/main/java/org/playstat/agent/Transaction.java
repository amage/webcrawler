package org.playstat.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction {
    private String url;
    private long initTime;
    private RequestMethod method;
    private final Map<String,List<String>> requestParams = new HashMap<>();
    private final Map<String,List<String>> responseParams = new HashMap<>();

    public void addRequestParam(String key, String value) {
        if(!requestParams.containsKey(key)) {
            requestParams.put(key, new ArrayList<>());
        }
        requestParams.get(key).add(value);
    }
    public static Transaction create(String url) {
        final Transaction transaction = new Transaction();
        transaction.setUrl(url);
        transaction.setMethod(RequestMethod.GET);
        transaction.setInitTime(System.currentTimeMillis());
        return transaction;
    }
    public static Transaction create(String url, Map<String,String> requestParams) {
        final Transaction transaction = create(url);
        final Map<String, List<String>> prep =  new HashMap<>();
        requestParams.entrySet().stream().forEach(e -> {
            if(!prep.containsKey(e.getKey())) {
                prep.put(e.getKey(), new ArrayList<>());
            }
            prep.get(e.getKey()).add(e.getValue());
        });
        return transaction;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public Map<String,List<String>> getRequestParams() {
        return requestParams;
    }
    public long getInitTime() {
        return initTime;
    }
    public void setInitTime(long initTime) {
        this.initTime = initTime;
    }
    public Map<String,List<String>> getResponseParams() {
        return responseParams;
    }
}
