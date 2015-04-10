package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;


/**
 * Base class for file based tests.
 *   Run with -DDEBUG=true for debug output.
 *
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public abstract class FileTest implements AsmProperties {

    protected TestAccessor accessor = null;
    protected AsmRequestHelper requestHelper = null;

    /**
     * Print debug output to System.out if true.
     *   Run with -DDEBUG=true for debug output.
     */
    public final static boolean DEBUG = TRUE.equals(System.getProperty("DEBUG", FALSE));
    
    /**
     * Set up the test environment.
     *   read properties from file and create {@link TestAccessor}
     */
    @Before
    public void setup() {
        String filePrefix = "target/test-classes/";
        String propertyFileName = filePrefix + "AppSyntheticMonitor.properties";

        try {
            AsmReader.readPropertiesFromFile(propertyFileName);
            accessor = new TestAccessor(filePrefix);
            requestHelper = new AsmRequestHelper(accessor);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Checks that all metric names exist in the metric map.
     * @param expectedMetricNames array of expected metric names
     * @param metricMap metric map to check against
     */
    public void checkMetrics(String[] expectedMetricNames, HashMap<String, String> metricMap) {
        for (int i = 0; i < expectedMetricNames.length; ++i) {
            Assert.assertTrue(expectedMetricNames[i] + " missing",
                metricMap.containsKey(expectedMetricNames[i]));
        }
    }    

    /**
     * Checks that all metric names do not exist in the metric map.
     * @param notExpectedMetricNames array of not expected metric names
     * @param metricMap metric map to check against
     */
    public void checkNotExistMetrics(String[] notExpectedMetricNames,
                                     HashMap<String, String> metricMap) {
        for (int i = 0; i < notExpectedMetricNames.length; ++i) {
            Assert.assertFalse(notExpectedMetricNames[i] + " found",
                metricMap.containsKey(notExpectedMetricNames[i]));
        }
    }    

    /**
     * Checks that no not expected strings exist in the string array.
     * @param notExpected array of not expected metric names
     * @param actual string array to check against
     */
    public void checkNotExistMetrics(String[] notExpected, String[] actual) {
        for (int i = 0; i < notExpected.length; ++i) {
            for (int j = 0; j < actual.length; ++j) {
                Assert.assertNotEquals(notExpected[i] + " found", notExpected[i], actual[j]);
            }
        }
    }    

    /**
     * Checks that all metrics exist in the metric map and the values match expectations.
     * @param expectedMetricNames array of expected metric names
     * @param expectedMetricValues array of their expected values
     * @param metricMap metric map to check against
     */
    public void checkMetrics(String[] expectedMetricNames,
                             String[] expectedMetricValues,
                             HashMap<String, String> metricMap) {
        for (int i = 0; i < expectedMetricNames.length; ++i) {
            Assert.assertEquals(expectedMetricNames + " missing",
                expectedMetricValues[i],
                metricMap.get(expectedMetricNames[i]));
        }
    }    

    /**
     * Checks that all expected strings exist in the string array.
     * @param expected array of expected strings
     * @param actual string array to check against
     */
    public void checkMetrics(String[] expected, String[] actual) {
        for (int i = 0; i < expected.length; ++i) {
            boolean match = false;
            
            for (int j = 0; j < actual.length; ++j) {
                if (expected[i].equals(actual[j])) {
                    match = true; // we have a match
                    break; // jump out of actual loop
                }
            }
            
            // we have no match
            if (!match) {
                Assert.fail(expected[i] + " not found");
            }
        }
    }
}
