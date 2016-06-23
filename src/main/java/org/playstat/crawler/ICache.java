package org.playstat.crawler;

import java.io.File;

import org.playstat.agent.HTTPRequest;

public interface ICache {
    File getCacheFile(HTTPRequest request);
}
