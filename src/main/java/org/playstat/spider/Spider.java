package org.playstat.spider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.playstat.crawler.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spider {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WebClient web = new WebClient();
    private final IScenario scenario;
    private final IPageProcessor pageProcessor;
    private final Set<String> visited = new HashSet<>();

    public Spider(IScenario scenario, IPageProcessor pageProcessor) {
        this.scenario = scenario;
        this.pageProcessor = pageProcessor;
    }

    public void run() {
        final LinkedList<String> queue = new LinkedList<>(scenario.getSeeds());

        while (!queue.isEmpty()) {
            try {
                final String url = queue.pollFirst();
                final Document doc = web.go(url);
                pageProcessor.process(url, doc);
                doc.getElementsByTag("a").forEach(el -> {
                    final String href = el.attr("href");
                    if (href == null || href.isEmpty()) {
                        return;
                    }
                    final String nhref = naiveNormalize(url, href);
                    if (!visited.contains(nhref) && scenario.isInteresting(nhref)) {
                        queue.add(nhref);
                    }
                });
                visited.add(url);
                System.out.println("visited: " + url);
            } catch (final IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private String naiveNormalize(String pageUrl, String href) {
        if (href.startsWith("/")) {
            final String woProto = pageUrl.substring(pageUrl.indexOf(":") + 3);
            if (woProto.indexOf("/") > 0) {
                return pageUrl.substring(0, woProto.indexOf("/") + pageUrl.indexOf(":") + 3) + href;
            } else {
                return pageUrl + href;
            }
        }

        try {
            new URL(href);
            return href;
        } catch (final MalformedURLException e) {
            return pageUrl + href;
        }
    }
}
