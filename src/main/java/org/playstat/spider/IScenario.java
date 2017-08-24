package org.playstat.spider;

import java.util.List;

public interface IScenario {
    List<String> getSeeds();

    boolean isInteresting(String url);

    boolean shouldProcess(String url);
}
