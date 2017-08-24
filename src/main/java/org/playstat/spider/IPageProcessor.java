package org.playstat.spider;

import org.jsoup.nodes.Document;

public interface IPageProcessor {
    void process(String url, Document doc);
}
