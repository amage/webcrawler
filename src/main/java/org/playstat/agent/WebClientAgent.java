package org.playstat.agent;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.playstat.agent.nullagent.NullAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebClientAgent {
    private IAgent agent;
    private boolean useReferer = true;
    private int historySize = 512;
    private final LinkedList<Transaction> history = new LinkedList<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public WebClientAgent() {
        logger.debug("Creating WebClientAgent with NullAgent");
        agent = new NullAgent();
    }

    public InputStream go(String url) throws IOException {
        return go(Transaction.create(url));
    }

    public InputStream go(Transaction t) throws IOException {
        if (history.size() > 0 && useReferer) {
            t.getRequestParams().put("Referer", history.getFirst().getUrl());
        }
        putToHistory(t);
        return agent.go(t);
    }

    public IAgent getAgent() {
        return agent;
    }

    private void putToHistory(final Transaction t) {
        if (history.size() > historySize) {
            history.removeLast();
        }
        history.addFirst(t);
    }

}
