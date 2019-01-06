package org.playstat.spider;

import org.jsoup.nodes.Document;

public interface IPageProcessor {
    void processPage(Document page, String url);
}
