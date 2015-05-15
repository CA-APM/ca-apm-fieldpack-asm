package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the rule_log API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class LogTest extends FileTest {

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
     * Test getLog() for a script monitor.
     */
    @Test
    public void getLogScriptWithStations() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, TRUE);
            boolean stations = true;

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_script.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

            // metricMap should contain those entries
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String PHOENIX    = "|america-north|United States|Phoenix";
            
            String[] expectedMetrics = {
                "Monitors|Tests:Agent Time Zone",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Alerts Per Interval",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Resolve Time (ms)",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Processing Time (ms)",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Download Size (kB)",
                "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + ":Result Code",
                "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Check Start Time",
                "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Monitor ID",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001 http_//jmeter.apache.org/index.html:Assertion Errors",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001 http_//jmeter.apache.org/index.html:Assertion Failures",
                "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002 http_//jmeter.apache.org/usermanual/index.html:Errors Per Interval",
                "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002 http_//jmeter.apache.org/usermanual/index.html:Response Code",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message Value",
                "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 http_//www.apache.org/foundation/thanks.html:URL"
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

    /**
     * Test getLog() for a script monitor.
     */
    @Test
    public void getLogScriptWithoutStations() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);
            boolean stations = false;

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_script.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

            // metricMap should contain those entries
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String PHOENIX    = "|america-north|United States|Phoenix";
            
            String[] expectedMetrics = {
                "Monitors|Tests:Agent Time Zone",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Alerts Per Interval",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Resolve Time (ms)",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Processing Time (ms)",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Download Size (kB)",
                "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + ":Result Code",
                "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Check Start Time",
                "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Monitor ID",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001 http_//jmeter.apache.org/index.html:Assertion Errors",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001 http_//jmeter.apache.org/index.html:Assertion Failures",
                "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002 http_//jmeter.apache.org/usermanual/index.html:Errors Per Interval",
                "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002 http_//jmeter.apache.org/usermanual/index.html:Response Code",
                "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message",
                "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message Value",
                "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 http_//www.apache.org/foundation/thanks.html:URL"
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

    /**
     * Test getLog() for a script monitor.
     */
    @Test
    public void getLogFolder() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_all.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

            // metricMap should contain those entries
            
            String[] expectedMetrics = {
                "Monitors|Tests:Agent Time Zone",
                "Monitors|Tests|Simple HTTP validation test:Alerts Per Interval",
                "Monitors|Tests|Simple HTTP validation test:Check End Time",
                "Monitors|Tests|Simple HTTP validation test:Check Start Time",
                "Monitors|Tests|Simple HTTP validation test:Connect Time (ms)",
                "Monitors|Tests|Simple HTTP validation test:Download Size (kB)",
                "Monitors|Tests|Simple HTTP validation test:Download Time (ms)",
                "Monitors|Tests|Simple HTTP validation test:IP Address",
                "Monitors|Tests|Simple HTTP validation test:Location Code",
                "Monitors|Tests|Simple HTTP validation test:Processing Time (ms)",
                "Monitors|Tests|Simple HTTP validation test:Repeat",
                "Monitors|Tests|Simple HTTP validation test:Resolve Time (ms)",
                "Monitors|Tests|Simple HTTP validation test:Result Code",
                "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                "Monitors|Tests|Simple HTTP validation test:Monitor Name",
                "Monitors|Tests|Simple HTTP validation test:Total Time (ms)",
                "Monitors|Tests|Simple HTTP validation test:Type",
                "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                "Monitors|Tests|DNS test:Alerts Per Interval",
                "Monitors|Tests|DNS test:Check End Time",
                "Monitors|Tests|DNS test:Check Start Time",
                "Monitors|Tests|DNS test:Download Size (kB)",
                "Monitors|Tests|DNS test:Download Time (ms)",
                "Monitors|Tests|FTP test:IP Address",
                "Monitors|Tests|FTP test:Location Code",
                "Monitors|Tests|FTP test:Processing Time (ms)",
                "Monitors|Tests|FTP test:Repeat",
                "Monitors|Tests|FTP test:Result Code",
                "Monitors|Tests|FTP test:Monitor ID",
                "Monitors|Tests|FTP test:Monitor Name",
                "Monitors|Tests|Custom page ping:Total Time (ms)",
                "Monitors|Tests|Custom page ping:Type",
                "Monitors|Tests|Custom page ping:Monitor ID",
                "Monitors|Tests|Custom page ping:Connect Time (ms)",
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

    /**
     * Test getLog() for a http monitor.
     */
    @Test
    public void getLogHttp() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, "true");

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple HTTP validation test",
//                HTTP_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_http.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

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
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Repeat",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type",
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID"
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

    /**
     * Test getLog() for a full page monitor (browser).
     */
    @Test
    public void getLogFullPage() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, "true");

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Amazon.com",
//                FULL_PAGE_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_browser.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

            // metricMap should contain those entries
            String[] expectedMetrics = {
                "Monitors|Tests:Agent GMT Offset",
                "Monitors|Tests|Amazon.com|america-south|Panama|Panama City:Alerts Per Interval",
                "Monitors|Tests|Amazon.com|america-south|Panama|Panama City:Check End Time",
                "Monitors|Tests|Amazon.com|america-south|Panama|Panama City:Check Start Time",
                "Monitors|Tests|Amazon.com|australia|Australia|Perth:Connect Time (ms)",
                "Monitors|Tests|Amazon.com|australia|Australia|Perth:Download Size (kB)",
                "Monitors|Tests|Amazon.com|australia|Australia|Perth:Download Time (ms)",
                "Monitors|Tests|Amazon.com|europe-east|Serbia|Belgrade:IP Address",
                "Monitors|Tests|Amazon.com|europe-east|Serbia|Belgrade:Location Code",
                "Monitors|Tests|Amazon.com|europe-east|Serbia|Belgrade:Processing Time (ms)",
                "Monitors|Tests|Amazon.com|europe-west|Denmark|Copenhagen:Repeat",
                "Monitors|Tests|Amazon.com|europe-west|Denmark|Copenhagen:Result Code",
                "Monitors|Tests|Amazon.com|europe-west|Denmark|Copenhagen:Monitor ID",
                "Monitors|Tests|Amazon.com|europe-west|Switzerland|Zurich:Monitor Name",
                "Monitors|Tests|Amazon.com|europe-west|Switzerland|Zurich:Total Time (ms)",
                "Monitors|Tests|Amazon.com|europe-west|Switzerland|Zurich:Type",
                "Monitors|Tests|Amazon.com|europe-west|Switzerland|Zurich:Monitor ID"
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

    /**
     * Test getLog() for a real browser monitor (RBM).
     */
    @Test
    public void getLogRealBrowserMonitor() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "";
//            Monitor monitor = MonitorFactory.getMonitor("Cat.com click-through RBM",
//                REAL_BROWSER_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX.substring(0,
                MONITOR_METRIC_PREFIX.length() - 1);
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_firefox.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

            // metricMap should contain those entries
            String[] expectedMetrics = {
                "Monitors:Agent GMT Offset"
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

    /**
     * Test getLog() for a real browser monitor (RBM).
     */
    @Test
    public void getLogRealBrowserMonitor2() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "Caterpillar";
//            Monitor monitor = MonitorFactory.getMonitor("SFDC transaction",
//                REAL_BROWSER_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_firefox2.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

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
