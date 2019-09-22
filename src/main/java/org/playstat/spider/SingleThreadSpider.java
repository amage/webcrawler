package org.playstat.spider;

import org.jsoup.nodes.Document;
import org.playstat.crawler.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SingleThreadSpider implements ISpider {
    public static final String QUEUE_PREFIX = "q:";
    public static final String VISITED_PREFIX = "v:";
    private final Logger log = LoggerFactory.getLogger(SingleThreadSpider.class);
    private final Queue<String> urls = new LinkedList<>();
    private final Set<String> visited = new HashSet<>();
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
        start(0);
    }

    @Override
    public void start(long requestLimit) {
        final boolean limited = requestLimit > 0;

        while (!urls.isEmpty() && (!limited || requestLimit-- > 0)) {
            final String currentUrl = urls.poll();
            try {
                Collection<String> newUrls = process(currentUrl);
                visited.add(currentUrl);
                for (String newUrl : newUrls) {
                    if (!visited.contains(newUrl)) {
                        urls.add(newUrl);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                // TODO should the url be returned to queue?
            }
        }
    }

    @Override
    public boolean loadState(Path statePath) throws IOException {
        if (!Files.exists(statePath)) {
            return false;
        }
        urls.clear();
        visited.clear();
        for (String line : Files.readAllLines(statePath)) {
            if (line.startsWith(QUEUE_PREFIX)) {
                urls.add(line.substring(2));
                continue;
            }
            if (line.startsWith("v:")) {
                visited.add(line.substring(2));
            }
        }
        return !urls.isEmpty() || !visited.isEmpty();
    }

    @Override
    public void saveState(Path statePath) throws IOException {
        Files.write(statePath, urls.stream().map(l -> QUEUE_PREFIX + l).collect(Collectors.toSet()));
        Files.write(statePath, visited.stream().map(l -> VISITED_PREFIX + l).collect(Collectors.toSet()),
                StandardOpenOption.APPEND);
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
