package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for testing the cp_list API.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class StationTest extends FileTest {

    /**
     * Test getMonitoringStations().
     */
    @Test
    public void getMonitoringStations() {

        try {
            // call API
            HashMap<String, String> stationMap = requestHelper.getMonitoringStations();

            // metricMap should contain those entries
            String expectedMetrics[] = { "sf", "fl", "vi"};
            String expectedValues[] = { "america-north|United States|San Francisco",
                                        "america-north|United States|Orlando",
                                        "europe-east|Austria|Vienna"};

            // check
            checkMetrics(expectedMetrics, expectedValues, stationMap);

            // must have 95 entries
            int expectedCount = 95;
            Assert.assertEquals("expected " + expectedCount + " monitoring stations",
                expectedCount, stationMap.size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
