package org.playstat.agent;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.playstat.crawler.WebClient;

public interface ICaptchaSolver {
    boolean isCaptchaPage(Document doc);
    void solve(WebClient webClient, Document doc) throws IOException;
}
