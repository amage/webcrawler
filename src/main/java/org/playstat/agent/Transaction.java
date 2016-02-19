package org.playstat.agent;

import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private String url;
    private long initTime;
    private RequestMethod method;
    private final Map<String, String> requestParams = new HashMap<String, String>();

    public static Transaction create(String url) {
        final Transaction transaction = new Transaction();
        transaction.setUrl(url);
        transaction.setMethod(RequestMethod.GET);
        transaction.setInitTime(System.currentTimeMillis());
        return transaction;
    }
    public static Transaction create(String url, Map<String, String> requestParams) {
        final Transaction transaction = create(url);
        transaction.getRequestParams().putAll(requestParams);
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

    public Map<String, String> getRequestParams() {
        return requestParams;
    }
    public long getInitTime() {
        return initTime;
    }
    public void setInitTime(long initTime) {
        this.initTime = initTime;
    }
}
