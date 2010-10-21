package com.frostwire.plugins.tests;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public final class PluginTestSuite extends TestCase {
    public static TestSuite suite() {
        TestSuite testSuite;
        testSuite = new TestSuite();
        
        //testSuite.addTestSuite(PluginLoaderTest.class);
        //testSuite.addTestSuite(PluginDownloaderTest.class);
        testSuite.addTestSuite(PluginManagerTest.class);

        return testSuite;
    }
}