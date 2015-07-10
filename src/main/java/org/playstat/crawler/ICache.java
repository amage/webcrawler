package org.playstat.crawler;

import java.io.File;

public interface ICache {
    File getCacheFile(String url);
}
