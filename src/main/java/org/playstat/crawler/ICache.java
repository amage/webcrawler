package org.playstat.crawler;

import java.io.File;

import org.playstat.agent.HTTPRequest;
import org.playstat.agent.Transaction;

public interface ICache {
    File getCacheFile(HTTPRequest request);
    boolean isCahed(HTTPRequest request);
    Transaction get();
}
