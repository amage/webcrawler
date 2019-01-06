package org.playstat.spider;

import org.jsoup.nodes.Document;

import java.util.Collection;

public interface IUrlExtractor {
    Collection<String> extract(Document page);
}
