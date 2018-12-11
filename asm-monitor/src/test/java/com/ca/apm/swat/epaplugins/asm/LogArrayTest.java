package com.ca.apm.swat.epaplugins.asm;

import com.wily.introscope.epagent.EpaUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Test class for testing the rule_log API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class LogArrayTest extends FileTest {

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
     * Test getLog() for a json string that includes empty arrays.
     */
    @Test
    public void getLogArray() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);

            String folder = "ArrayTest";
            int numMonitors = 4;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_array.json");

            // call API
            Map<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix, null).getMap();

            // metricMap should contain those entries
            
            String[] expectedMetrics = {
                "Monitors|ArrayTest:Agent Time Zone",
                "Monitors|ArrayTest|Monitor1:Alerts Per Interval",
                "Monitors|ArrayTest|Monitor1:Check End Time",
                "Monitors|ArrayTest|Monitor1:Check Start Time",
                "Monitors|ArrayTest|Monitor1:Connect Time (ms)",
                "Monitors|ArrayTest|Monitor1:Download Size (kB)",
                "Monitors|ArrayTest|Monitor1:Download Time (ms)",
                "Monitors|ArrayTest|Monitor1:IP Address",
                "Monitors|ArrayTest|Monitor1:Location Code",
                "Monitors|ArrayTest|Monitor1:Processing Time (ms)",
                "Monitors|ArrayTest|Monitor1:Repeat",
                "Monitors|ArrayTest|Monitor1:Resolve Time (ms)",
                "Monitors|ArrayTest|Monitor1:Result Code",
                "Monitors|ArrayTest|Monitor1:Monitor ID",
                "Monitors|ArrayTest|Monitor1:Monitor Name",
                "Monitors|ArrayTest|Monitor1:Total Time (ms)",
                "Monitors|ArrayTest|Monitor1:Type",
                "Monitors|ArrayTest|Monitor1:Monitor ID",
                "Monitors|ArrayTest|Monitor2:Alerts Per Interval",
                "Monitors|ArrayTest|Monitor2:Check End Time",
                "Monitors|ArrayTest|Monitor2:Check Start Time",
                "Monitors|ArrayTest|Monitor2:Download Size (kB)",
                "Monitors|ArrayTest|Monitor2:Download Time (ms)",
                "Monitors|ArrayTest|Monitor3:IP Address",
                "Monitors|ArrayTest|Monitor3:Location Code",
                "Monitors|ArrayTest|Monitor3:Processing Time (ms)",
                "Monitors|ArrayTest|Monitor3:Repeat",
                "Monitors|ArrayTest|Monitor3:Result Code",
                "Monitors|ArrayTest|Monitor3:Monitor ID",
                "Monitors|ArrayTest|Monitor3:Monitor Name",
                "Monitors|ArrayTest|Monitor4:Total Time (ms)",
                "Monitors|ArrayTest|Monitor4:Type",
                "Monitors|ArrayTest|Monitor4:Monitor ID",
                "Monitors|ArrayTest|Monitor4:Connect Time (ms)",
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
