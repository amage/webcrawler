package org.playstat.agent.nullagent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.playstat.agent.ICookiesStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryCookiesStorage implements ICookiesStorage {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Map<String, String>> cookies = new HashMap<>();

    @Override
    public void addCookie(String host, String name, String value) {
        if (!cookies.containsKey(host)) {
            cookies.put(host, new HashMap<String, String>());
        }
        logger.debug("store cookie: " + name + " -> " + value);
        cookies.get(host).put(name, value);
    }

    @Override
    public Map<String, String> get(String host) {
        if (!cookies.containsKey(host)) {
            return Collections.emptyMap();
        }
        return cookies.get(host);
    }
}
