package org.playstat.crawler;

import java.io.InputStream;
import java.util.Map;

public interface CacheData {
    Map<String, String> getMata();
    InputStream getData();
}
