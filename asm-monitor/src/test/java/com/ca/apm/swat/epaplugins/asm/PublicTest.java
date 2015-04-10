package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;

/**
 * Test class for testing the rule_psp API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class PublicTest extends FileTest {

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
    public void getLogScript() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "Tests";
            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording", SCRIPT_MONITOR, folder,
                EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_script.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, monitor, numMonitors, metricPrefix);

            // metricMap should contain those entries
            String[] expectedMetrics = {
                "Monitors|Tests:Agent Time Zone",
                "Monitors|Tests|Simple JMeter recording|america-north|Canada|"
                        + "Calgary:Alerts Per Interval",
                "Monitors|Tests|Simple JMeter recording|america-north|Canada|"
                        + "Calgary:Resolve Time (ms)",
                "Monitors|Tests|Simple JMeter recording|america-north|Canada|"
                        + "Toronto:Processing Time (ms)",
                "Monitors|Tests|Simple JMeter recording|america-north|Canada|"
                        + "Vancouver:Download Size (kB)",
                "Monitors|Tests|Simple JMeter recording|america-north|Canada|"
                        + "Vancouver:Result Code",
                "Monitors|Tests|Simple JMeter recording|america-north|"
                        + "United States|Phoenix:Check Start Time",
                "Monitors|Tests|Simple JMeter recording|america-north|"
                        + "United States|Phoenix:Monitor ID",
                "Monitors|Tests|Simple JMeter recording|"
                        + "001 /index.html:Assertion Errors",
                "Monitors|Tests|Simple JMeter recording|"
                        + "001 /index.html:Assertion Failures",
                "Monitors|Tests|Simple JMeter recording|"
                        + "002 /usermanual/index.html:Error Count",
                "Monitors|Tests|Simple JMeter recording|"
                        + "002 /usermanual/index.html:Response Code",
                "Monitors|Tests|Simple JMeter recording|"
                        + "003 /usermanual/build-test-plan.html:Status Message",
                "Monitors|Tests|Simple JMeter recording|"
                        + "003 /usermanual/build-test-plan.html:Status Message Value",
                "Monitors|Tests|Simple JMeter recording|"
                        + "004 /foundation/thanks.html:URL"
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "001 /index.html:Assertion Errors",
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "001 /index.html:Assertion Failures",
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "002 /usermanual/index.html:Error Count",
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "002 /usermanual/index.html:Response Code",
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "003 /usermanual/build-test-plan.html:Status Message",
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "003 /usermanual/build-test-plan.html:Status Message Value",
//                "Status Monitoring|Tests|Simple JMeter recording|"
//                        + "004 /foundation/thanks.html:URL"
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
//    @Test
    public void getLogHttp() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "Tests";
            Monitor monitor = MonitorFactory.getMonitor("Simple HTTP validation test", HTTP_MONITOR, folder,
                EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_http.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, monitor, numMonitors, metricPrefix);

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
                "Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:id"
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
//    @Test
    public void getLogFullPage() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "Tests";
            Monitor monitor = MonitorFactory.getMonitor("Amazon.com", FULL_PAGE_MONITOR, folder,
                EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_browser.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, monitor, numMonitors, metricPrefix);

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
                "Monitors|Tests|Amazon.com|europe-west|Switzerland|Zurich:id"
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
    //@Test
    public void getLogRealBrowserMonitor() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "";
            Monitor monitor = MonitorFactory.getMonitor("Cat.com click-through RBM", REAL_BROWSER_MONITOR, folder,
                EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX.substring(0,
                MONITOR_METRIC_PREFIX.length() - 1);
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_firefox.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, monitor, numMonitors, metricPrefix);

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
//    @Test
    public void getLogRealBrowserMonitor2() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(METRICS_LOGS, TRUE);

            String folder = "Caterpillar";
            Monitor monitor = MonitorFactory.getMonitor("SFDC transaction", REAL_BROWSER_MONITOR, folder,
                EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_firefox2.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, monitor, numMonitors, metricPrefix);

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
