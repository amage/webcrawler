package org.playstat.tests;

import java.net.MalformedURLException;

import org.jsoup.nodes.Element;
import org.junit.Test;
import org.playstat.crawler.dop.DOPWrapper;
import org.playstat.crawler.dop.Extractor;
import org.playstat.crawler.dop.Page;
import org.playstat.crawler.WebClient;

import junit.framework.Assert;

// FIXME: make test without connection to yandex.ru
public class DOPTests {
    @Test
    public void testExtractor() throws MalformedURLException {
        final WebClient web = new WebClient();
        final DOPWrapper wrapper = new DOPWrapper(web);
        final ExtractorTest result = wrapper.get(ExtractorTest.class);
        Assert.assertEquals("success", result.getResult());
    }

    @Page("https://www.yandex.ru/")
    private static class ExtractorTest {
        private String result;

        @Extractor("result")
        private String generateResult(Element e) {
            return "success";
        }

        public String getResult() {
            return result;
        }
    }
}
