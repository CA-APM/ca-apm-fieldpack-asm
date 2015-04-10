package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;
import com.wily.introscope.epagent.EpaUtils;

/**
 * This test checks if asm.ignoreTags is handled correctly.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class IgnoreTagTest extends FileTest {

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
     * Test asm.ignoreTags property handling.
     */
    @Test
    public void ignoreOneTag() {

        // set properties
        Properties props = EpaUtils.getProperties(); 
        props.setProperty(METRICS_LOGS, TRUE);
        props.setProperty(IGNORE_TAGS, "repeat");

        // metricMap should contain those entries
        String[] expectedMetrics = {
                                    "Monitors|Tests:Agent Time Zone",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Alerts Per Interval",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check End Time",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check Start Time",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Connect Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Size (kB)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:IP Address",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Location Code",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Processing Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID"
        };

        String[] notExpectedMetrics = {
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Repeat"
        };

        runTest(props, expectedMetrics, notExpectedMetrics);
    }

    /**
     * Run the test with the supplied properties and check results.
     * @param props properties
     * @param expectedMetrics metrics that must be returned
     * @param notExpectedMetrics metrics that must not be returned
     */
    private void runTest(Properties props,
                         String[] expectedMetrics,
                         String[] notExpectedMetrics) {
        try {
            Formatter.setProperties(props);

            String folder = "Tests";
            Monitor monitor = MonitorFactory.getMonitor("Simple HTTP validation test",
                HTTP_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_http.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, monitor, numMonitors, metricPrefix);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            // check
            checkMetrics(expectedMetrics, metricMap);
            checkNotExistMetrics(notExpectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test asm.ignoreTags property handling.
     */
    @Test
    public void ignoreTwoTags() {

        // set properties
        Properties props = EpaUtils.getProperties(); 
        props.setProperty(METRICS_LOGS, TRUE);
        props.setProperty(IGNORE_TAGS, "repeat,type");

        // metricMap should contain those entries
        String[] expectedMetrics = {
                                    "Monitors|Tests:Agent Time Zone",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Alerts Per Interval",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check End Time",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check Start Time",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Connect Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Size (kB)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:IP Address",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Location Code",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Processing Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID"
        };

        String[] notExpectedMetrics = {
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Repeat",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Type",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Type",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Type",
                                       "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type"
        };

        runTest(props, expectedMetrics, notExpectedMetrics);
    }

    /**
     * Test asm.ignoreTags property handling.
     */
    @Test
    public void ignoreEmptyTag() {

        // set properties
        Properties props = EpaUtils.getProperties(); 
        props.setProperty(METRICS_LOGS, TRUE);
        props.setProperty(IGNORE_TAGS, EMPTY_STRING);

        // metricMap should contain those entries
        String[] expectedMetrics = {
                                    "Monitors|Tests:Agent Time Zone",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Alerts Per Interval",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check End Time",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check Start Time",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Connect Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Size (kB)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:IP Address",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Location Code",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Processing Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Repeat",
                                    "Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Monitor ID"
        };

        runTest(props, expectedMetrics, EMPTY_STRING_ARRAY);
    }
}
