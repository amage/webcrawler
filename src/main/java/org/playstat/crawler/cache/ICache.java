package org.playstat.crawler.cache;

import java.io.File;

import org.playstat.agent.HTTPRequest;

public interface ICache {
    boolean isChached(HTTPRequest request);
    File getCacheFile(HTTPRequest request);
}
