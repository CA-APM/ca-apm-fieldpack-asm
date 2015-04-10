package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the acct_credits API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class StatsTest extends FileTest {

    @Override
    public void setup() {
        super.setup();

        // we need to load the the monitoring station map
        try {
            requestHelper.getMonitoringStations();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("error getting monitoring stations: " + e.getMessage());
        }
    }


    /**
     * Test getLog() for a real browser monitor (RBM).
     */
    @Test
    public void getStatsFirefox() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "Caterpillar";
            Monitor monitor = MonitorFactory.getMonitor("SFDC transaction", REAL_BROWSER_MONITOR, folder,
                EMPTY_STRING_ARRAY);
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(STATS_CMD, "target/test-classes/rule_stats_rule.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getStats(folder, monitor, metricPrefix);

            // metricMap should contain those entries
            String[] expectedMetrics = {
                "Monitors|Caterpillar:Agent GMT Offset"
            };

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }
            
            // check
            checkMetrics(expectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
