package org.playstat.spider;

import org.jsoup.nodes.Document;
import org.playstat.crawler.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SingleThreadSpider implements ISpider {
    private final Logger log = LoggerFactory.getLogger(SingleThreadSpider.class);
    private final Queue<String> urls = new LinkedList<>();
    private final IUrlExtractor urlExtractor;
    private final IPageProcessor pageProcessor;
    private final WebClient web = new WebClient();

    public SingleThreadSpider(IUrlExtractor urlExtractor, IPageProcessor pageProcessor) {
        this.urlExtractor = urlExtractor;
        this.pageProcessor = pageProcessor;
    }

    @Override
    public void setInitialURLs(String... urls) {
        this.urls.addAll(Arrays.asList(urls));
    }

    @Override
    public void start() {
        while (!urls.isEmpty()) {
            final String currentUrl = urls.poll();
            final Collection<String> newUrls = process(currentUrl);
            urls.addAll(newUrls);
        }
    }

    private Collection<String> process(String url) {
        try {
            final Document page = web.go(url);
            pageProcessor.processPage(page, url);
            return urlExtractor.extract(page);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}
