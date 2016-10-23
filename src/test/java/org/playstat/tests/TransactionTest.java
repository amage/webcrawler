package org.playstat.tests;

import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.junit.Test;
import org.playstat.agent.HTTPResponse;
import org.playstat.agent.Transaction;
import org.playstat.crawler.WebClient;

public class TransactionTest {
    @Test
    public void transactionBasedCrawling() throws IOException {
        final String html = "<html>\n <head></head> \n <body>  \n </body>\n</html>";
        final WebClient web = new WebClient();
        final Transaction t = Transaction.create("http://test.com/");
        final HTTPResponse response = new HTTPResponse(200, new HashMap<>(),
                new ByteArrayInputStream(html.getBytes(web.getCharsetName())));
        t.setResponse(response);
        final Document page = web.go(t, "http://test.com/");
        Assert.assertEquals(html, page.html());
    }

    @Test
    public void test() {
        final String expectedURL = "a.html";
        Transaction t = Transaction.create(expectedURL);
        Assert.assertEquals(expectedURL, t.getUrl());
    }
}
