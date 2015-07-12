package org.playstat.agent;

import java.io.IOException;
import java.io.InputStream;

import org.playstat.agent.nullagent.NullAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebClientAgent {
    private IAgent agent;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public WebClientAgent() {
        agent = new NullAgent();
    }

    public InputStream go(String url) throws IOException {
        return agent.go(Transaction.create(url));
    }

    public IAgent getAgent() {
        return agent;
    }
}
