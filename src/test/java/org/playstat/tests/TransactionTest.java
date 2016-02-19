package org.playstat.tests;

import junit.framework.Assert;

import org.junit.Test;
import org.playstat.agent.Transaction;

public class TransactionTest {
    @Test
    public void test() {
        final String expectedURL = "a.html";
        Transaction t = Transaction.create(expectedURL);
        Assert.assertEquals(expectedURL, t.getUrl());
    }
}
