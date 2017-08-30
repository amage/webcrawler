package org.playstat.tests;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.playstat.agent.Transaction;
import org.playstat.crawler.ICache;

public class CacheTest {
    @Test
    public void testGetHit() throws IOException {
        final ICache cache = Mockito.mock(ICache.class);
        final Transaction t = Transaction.create("http://ya.ru");
        cache.cache(t);
        Assert.assertTrue(cache.isCahed(t.getRequest()));
        cache.get(t.getRequest());
    }
}
