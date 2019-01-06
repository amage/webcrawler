package org.playstat.spider;

import org.jsoup.nodes.Document;
import org.playstat.crawler.WebClient;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SingleThreadSpider implements ISpider {
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

    protected Collection<String> process(String url) {
        try {
            final Document page = web.go(url);
            pageProcessor.processPage(page, url);
            Collection<String> urls = urlExtractor.extract(page);
            return urls;
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
