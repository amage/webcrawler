package org.playstat.crawler;

import java.util.Map;

import org.playstat.agent.ICookiesStorage;
import org.playstat.agent.nullagent.MemoryCookiesStorage;

// TODO serialization, batch setting, change tracking
public class CrawlerSession implements ICookiesStorage {
    private MemoryCookiesStorage mem = new MemoryCookiesStorage();

    public CrawlerSession() {
    }

    @Override
    public void addCookie(String host, String name, String value) {
        mem.addCookie(host, name, value);
    }

    @Override
    public Map<String, String> get(String host) {
        return mem.get(host);
    }
}
