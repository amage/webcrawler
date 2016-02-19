package org.playstat.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// HTML files
// a <-> b -> c -> c

@RunWith(Suite.class)
@SuiteClasses({ SpiderTest.class, TransactionTest.class })
public class TestSuit {
}
