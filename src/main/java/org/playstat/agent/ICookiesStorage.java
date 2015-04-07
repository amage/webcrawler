package org.playstat.agent;

import java.util.Map;

public interface ICookiesStorage {
    void addCookie(String host, String name, String value);

    Map<String, String> get(String host);
}
