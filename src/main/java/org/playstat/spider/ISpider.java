package org.playstat.spider;

import java.io.IOException;
import java.nio.file.Path;

public interface ISpider {

    void setInitialURLs(String... urls);

    void start();

    void start(long requestLimit);

    boolean loadState(Path statePath) throws IOException;

    void saveState(Path statePath) throws IOException;
}
