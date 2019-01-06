package org.playstat.tests;

import org.junit.Test;
import org.playstat.spider.ISpider;
import org.playstat.spider.SingleThreadSpider;

import java.io.IOException;

public class SpiderTest {
    @Test
    public void runLoopTest() throws IOException {
        // Init spider
        ISpider spider = new SingleThreadSpider(null, null);
        // Load initial transaction data and seeds (start urls)
        spider.setInitialURLs("a.html");
        // Get transaction
        
        // If download is OK change it state NEW -> PROC
        // Find all fits handlers and execute them
        // Change state PROC->COMPLETE
        // Goto 2 until unprocessed transaction exists
    }
}
