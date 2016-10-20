package org.playstat.tests;

import java.net.MalformedURLException;

import org.jsoup.nodes.Element;
import org.junit.Test;
import org.playstat.crawler.DOPWrapper;
import org.playstat.crawler.Extractor;
import org.playstat.crawler.Page;
import org.playstat.crawler.WebClient;

import junit.framework.Assert;

// FIXME: make test without connection to yandex.ru
public class DOPTests {
    @Page("https://www.yandex.ru/")
    public static class ExtractorTest {
        private String result;

        @Extractor("result")
        private String generateResult(Element e) {
            return "success";
        }

        public String getResult() {
            return result;
        }
    }

    @Test
    public void testExtractor() throws MalformedURLException {
        final WebClient web = new WebClient();
        final DOPWrapper wrapper = new DOPWrapper(web);
        final ExtractorTest result = wrapper.get(ExtractorTest.class);
        Assert.assertEquals("success", result.getResult());
    }
}
