package org.playstat.tests;

import org.junit.Test;
import org.playstat.spider.ISpider;
import org.playstat.spider.Spider;

public class SpiderTest {
    @Test
    public void runLoopTest() {
        // Init spider
        ISpider spider = new Spider();
        // Load initial transaction data and seeds (start urls)
        spider.setInitialURLs("a.html");
        // Get transaction
        
        // If download is OK change it state NEW -> PROC
        // Find all fits handlers and execute them
        // Change state PROC->COMPLETE
        // Goto 2 until unprocessed transaction exists
    }
}
