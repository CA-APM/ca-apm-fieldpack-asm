package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.wily.introscope.epagent.EpaUtils;
import java.util.Map;

/**
 * Test class for testing the output formatting.
 *   Run with -DDEBUG=true for debug output.
 *
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class LayoutTest extends FileTest {



    @Override
    public void setup() {


        super.setup();
        System.out.println("DEBUG=" + DEBUG);
        // we need to load the the monitoring station map
        try {
            requestHelper.getMonitoringStations();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("error getting monitoring stations: " + e.getMessage());
        }
    }

    /**
     * Test if asm.printAsmNode is handled correctly.
     */
    @Test
    public void printAsmNode() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running printAsmNode\n**********\n");
        }

        try {
            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(METRICS_LOGS, TRUE);
            props.setProperty(DISPLAY_STATIONS, TRUE);
            props.setProperty(PRINT_ASM_NODE, FALSE);
            props.setProperty(IGNORE_METRICS, EMPTY_STRING);
            props.setProperty(IGNORE_METRICS_MONITOR, EMPTY_STRING);
            boolean stations = true;

            // metricMap should contain those entries
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String PHOENIX    = "|america-north|United States|Phoenix";

            String[] expectedMetrics = {
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

            String[] notExpectedMetrics = {
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Alerts Per Interval",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Resolve Time (ms)",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Processing Time (ms)",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Download Size (kB)",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + ":Result Code",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Check Start Time",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Monitor ID",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001 http_//jmeter.apache.org/index.html:Assertion Errors",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001 http_//jmeter.apache.org/index.html:Assertion Failures",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002 http_//jmeter.apache.org/usermanual/index.html:Errors Per Interval",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002 http_//jmeter.apache.org/usermanual/index.html:Response Code",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message Value",
                                           METRIC_TREE + METRIC_PATH_SEPARATOR + "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 http_//www.apache.org/foundation/thanks.html:URL"
            };

            Map<String, String> metricMap = runTest("target/test-classes/rule_log_script.json", props);
            checkMetricWriter(metricMap, expectedMetrics, notExpectedMetrics);

            // now set to false and switch expected and not expected metrics
            props.setProperty(PRINT_ASM_NODE, TRUE);
            metricMap = runTest("target/test-classes/rule_log_script.json", props);
            checkMetricWriter(metricMap, notExpectedMetrics, expectedMetrics);


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test if asm.stepFormatDigits is handled correctly.
     */
    @Test
    public void stepFormatDigits() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running stepFormatDigits\n**********\n");
        }

        try {
            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(METRICS_LOGS, TRUE);
            props.setProperty(DISPLAY_STATIONS, TRUE);
            props.setProperty(IGNORE_METRICS, EMPTY_STRING);
            props.setProperty(IGNORE_METRICS_MONITOR, EMPTY_STRING);
            props.setProperty(STEP_FORMAT_DIGITS, "2");
            boolean stations = true;

            // metricMap should contain those entries
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String PHOENIX    = "|america-north|United States|Phoenix";

            String[] expectedMetrics = {
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Alerts Per Interval",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Resolve Time (ms)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Processing Time (ms)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Download Size (kB)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + ":Result Code",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Check Start Time",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Monitor ID",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|01 http_//jmeter.apache.org/index.html:Assertion Errors",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|01 http_//jmeter.apache.org/index.html:Assertion Failures",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|02 http_//jmeter.apache.org/usermanual/index.html:Errors Per Interval",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|02 http_//jmeter.apache.org/usermanual/index.html:Response Code",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|03 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|03 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message Value",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|04 http_//www.apache.org/foundation/thanks.html:URL"
            };

            String[] notExpectedMetrics = {
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001 /index.html:Assertion Errors",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001 /index.html:Assertion Failures",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002 /usermanual/index.html:Errors Per Interval",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002 /usermanual/index.html:Response Code",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003 /usermanual/build-test-plan.html:Status Message",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 /usermanual/build-test-plan.html:Status Message Value",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 /foundation/thanks.html:URL"
            };

            Map<String, String> metricMap = runTest("target/test-classes/rule_log_script.json", props);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            checkMetrics(expectedMetrics, metricMap);
            checkNotExistMetrics(notExpectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test if asm.stepFormatPrefix is handled correctly.
     */
    @Test
    public void stepFormatPrefix() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running stepFormatPrefix\n**********\n");
        }

        try {
            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(METRICS_LOGS, TRUE);
            props.setProperty(DISPLAY_STATIONS, TRUE);
            props.setProperty(IGNORE_METRICS, EMPTY_STRING);
            props.setProperty(IGNORE_METRICS_MONITOR, EMPTY_STRING);
            props.setProperty(STEP_FORMAT_PREFIX, "Test");
            boolean stations = true;

            // metricMap should contain those entries
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String PHOENIX    = "|america-north|United States|Phoenix";

            String[] expectedMetrics = {
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Alerts Per Interval",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Resolve Time (ms)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Processing Time (ms)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Download Size (kB)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + ":Result Code",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Check Start Time",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Monitor ID",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|Test 001 http_//jmeter.apache.org/index.html:Assertion Errors",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|Test 001 http_//jmeter.apache.org/index.html:Assertion Failures",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|Test 002 http_//jmeter.apache.org/usermanual/index.html:Errors Per Interval",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|Test 002 http_//jmeter.apache.org/usermanual/index.html:Response Code",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|Test 003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|Test 003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message Value",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|Test 004 http_//www.apache.org/foundation/thanks.html:URL"
            };

            String[] notExpectedMetrics = {
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001 /index.html:Assertion Errors",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001 /index.html:Assertion Failures",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002 /usermanual/index.html:Errors Per Interval",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002 /usermanual/index.html:Response Code",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003 /usermanual/build-test-plan.html:Status Message",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 /usermanual/build-test-plan.html:Status Message Value",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 /foundation/thanks.html:URL"
            };

            Map<String, String> metricMap = runTest("target/test-classes/rule_log_script.json", props);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            checkMetrics(expectedMetrics, metricMap);
            checkNotExistMetrics(notExpectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test if asm.stepFormatURL is handled correctly.
     */
    @Test
    public void stepFormatUrl() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running stepFormatUrl\n**********\n");
        }

        try {
            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(METRICS_LOGS, TRUE);
            props.setProperty(DISPLAY_STATIONS, TRUE);
            props.setProperty(IGNORE_METRICS, EMPTY_STRING);
            props.setProperty(IGNORE_METRICS_MONITOR, EMPTY_STRING);
            props.setProperty(STEP_FORMAT_URL, "false");
            boolean stations = true;

            // metricMap should contain those entries
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String PHOENIX    = "|america-north|United States|Phoenix";

            String[] expectedMetrics = {
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Alerts Per Interval",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + ":Resolve Time (ms)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Processing Time (ms)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + ":Download Size (kB)",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + ":Result Code",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Check Start Time",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + ":Monitor ID",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001:Assertion Errors",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001:Assertion Failures",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002:Errors Per Interval",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002:Response Code",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003:Status Message",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003:Status Message Value",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004:URL"
            };

            String[] notExpectedMetrics = {
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|001 /index.html:Assertion Errors",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|001 /index.html:Assertion Failures",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|002 /usermanual/index.html:Errors Per Interval",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|002 /usermanual/index.html:Response Code",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? CALGARY   : "") + "|003 /usermanual/build-test-plan.html:Status Message",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 /usermanual/build-test-plan.html:Status Message Value",
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 /foundation/thanks.html:URL"
            };

            Map<String, String> metricMap = runTest("target/test-classes/rule_log_script.json", props);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            checkMetrics(expectedMetrics, metricMap);
            checkNotExistMetrics(notExpectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Run the test with the supplied properties and check results.
     * @param props properties
     * @param expectedMetrics metrics that must be returned
     * @param notExpectedMetrics metrics that must not be returned
     * @return the metric Map
     */
    private Map<String, String> runTest(String file, Properties props) {
        try {
            Formatter.setProperties(props);

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple HTTP validation test",
//                HTTP_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, file);

            // call API
            Map<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix, null).getMap();

            return metricMap;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        return null;
    }

    /**
     * Check metrics returned from MetricWriter for a metric map.
     * @param metricMap map of metrics to write
     * @param expectedMetrics array of expected metric name
     * @param notExpectedMetrics array of not expected metric name
     */
    private void checkMetricWriter(Map<String, String> metricMap,
                                   String[] expectedMetrics,
                                   String[] notExpectedMetrics) {
        try {
            // create a filtered MetricWriter
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            MetricWriter writer = AsmReader.getMetricWriter(new PrintStream(byteStream));

            ExecutorService reporterService = Executors.newSingleThreadExecutor();
            reporterService.execute(new AsmMetricReporter(writer, metricMap, true));

            // This will make the executor accept no new threads
            // and finish all existing threads in the queue
            reporterService.shutdown();
            // Wait until all threads are finish
            reporterService.awaitTermination(5, TimeUnit.SECONDS);

            // get output
            String metricString = byteStream.toString();
            String[] lines = metricString.split(System.getProperty("line.separator"));

            if (DEBUG) {
                System.out.println(metricString);
                System.out.flush();
            }

            // extract metric names from lines
            final String name = "name=\"";
            String[] actual = new String[lines.length];

            for (int j = 0; j < lines.length; ++j) {
                int begin = lines[j].indexOf(name) + name.length();
                Assert.assertNotEquals("invalid output line: " + lines[j], -1, begin);

                int end = lines[j].indexOf('"', begin);
                Assert.assertNotEquals("invalid output line: " + lines[j], -1, end);

                actual[j] = lines[j].substring(begin, end);
            }

            //check
            checkMetrics(expectedMetrics, actual);
            checkNotExistMetrics(notExpectedMetrics, actual);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    /**
     * Test if asm.reportStringResults is handled correctly.
     */
    @Test
    public void reportStringResults() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running reportStringResults\n**********\n");
        }

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);
            EpaUtils.getProperties().setProperty(REPORT_STRING_RESULTS, FALSE);
            boolean stations = false;

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_script.json");

            // call API
            Map<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix, null).getMap();

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
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? TORONTO   : "") + "|003 http_//jmeter.apache.org/usermanual/build-test-plan.html:Status Message Value",
                                        "Monitors|Tests|Simple JMeter recording" + (stations ? VANCOUVER : "") + "|004 http_//www.apache.org/foundation/thanks.html:URL"
            };

            String[] notExpectedMetrics = {
                                           "Monitors|Tests|Simple JMeter recording" + (stations ? PHOENIX   : "") + "|003 /usermanual/build-test-plan.html:Status Message"
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
            checkNotExistMetrics(notExpectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test if asm.stepFormatAlways is handled correctly.
     */
    @Test
    public void stepFormatAlways() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running stepFormatAlways\n**********\n");
        }

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);
            EpaUtils.getProperties().setProperty(STEP_FORMAT_ALWAYS, TRUE);
            EpaUtils.getProperties().setProperty(STEP_FORMAT_URL, FALSE);
            Formatter.setProperties(EpaUtils.getProperties());

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_all.json");

            MonitorFactory.createMonitor("Simple HTTP validation test", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("Simple HTTP validation test - fail", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("DNS test", DNS_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("FTP test", FTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("Custom page ping", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("Bad request test http_//ca.com/foo", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            // call API
            Map<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix, null).getMap();

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
                                        "Monitors|Tests|Simple HTTP validation test|001:Response Code",
                                        "Monitors|Tests|Simple HTTP validation test|001:Status Message Value",
                                        "Monitors|Tests|Simple HTTP validation test|001:URL",
                                        "Monitors|Tests|DNS test:Alerts Per Interval",
                                        "Monitors|Tests|DNS test:Check End Time",
                                        "Monitors|Tests|DNS test:Check Start Time",
                                        "Monitors|Tests|DNS test:Download Size (kB)",
                                        "Monitors|Tests|DNS test:Download Time (ms)",
                                        "Monitors|Tests|DNS test|001:Response Code",
                                        "Monitors|Tests|DNS test|001:Status Message Value",
                                        "Monitors|Tests|DNS test|001:URL",
                                        "Monitors|Tests|FTP test:IP Address",
                                        "Monitors|Tests|FTP test:Location Code",
                                        "Monitors|Tests|FTP test:Processing Time (ms)",
                                        "Monitors|Tests|FTP test:Repeat",
                                        "Monitors|Tests|FTP test:Result Code",
                                        "Monitors|Tests|FTP test:Monitor ID",
                                        "Monitors|Tests|FTP test:Monitor Name",
                                        "Monitors|Tests|FTP test|001:Response Code",
                                        "Monitors|Tests|FTP test|001:Status Message Value",
                                        "Monitors|Tests|FTP test|001:URL",
                                        "Monitors|Tests|Custom page ping:Total Time (ms)",
                                        "Monitors|Tests|Custom page ping:Type",
                                        "Monitors|Tests|Custom page ping:Monitor ID",
                                        "Monitors|Tests|Custom page ping:Connect Time (ms)",
                                        "Monitors|Tests|Custom page ping|001:Response Code",
                                        "Monitors|Tests|Custom page ping|001:Status Message Value",
                                        "Monitors|Tests|Custom page ping|001:URL",
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
     * Test if asm.stepFormatAlways is handled correctly.
     */
    @Test
    public void stepFormatAlwaysScript() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running stepFormatAlwaysScript\n**********\n");
        }

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);
            EpaUtils.getProperties().setProperty(STEP_FORMAT_ALWAYS, TRUE);
            EpaUtils.getProperties().setProperty(STEP_FORMAT_URL, TRUE);
            boolean stations = false;

            Formatter.setProperties(EpaUtils.getProperties());

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_script.json");

            // call API
            Map<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix, null).getMap();

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
     * Test if asm.stepFormatAlways is handled correctly.
     */
    @Test
    public void inactiveMonitors() {

        if (DEBUG) {
            System.out.println("\n**********\n* Running inactiveMonitors\n**********\n");
        }

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, FALSE);
            EpaUtils.getProperties().setProperty(STEP_FORMAT_ALWAYS, TRUE);
            EpaUtils.getProperties().setProperty(STEP_FORMAT_URL, FALSE);
            Formatter.setProperties(EpaUtils.getProperties());

            String folder = "Tests";
//            Monitor monitor = MonitorFactory.getMonitor("Simple JMeter recording",
//                SCRIPT_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_all.json");

            MonitorFactory.createMonitor("Simple HTTP validation test", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("Simple HTTP validation test - fail", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, false);
            MonitorFactory.createMonitor("DNS test", DNS_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("FTP test", FTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, false);
            MonitorFactory.createMonitor("Custom page ping", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, true);
            MonitorFactory.createMonitor("Bad request test http_//ca.com/foo", HTTP_MONITOR, folder, EMPTY_STRING_ARRAY, EMPTY_STRING, false);
            // call API
            Map<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix, null).getMap();

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
                                        "Monitors|Tests|Simple HTTP validation test|001:Status Message Value",
                                        "Monitors|Tests|Simple HTTP validation test|001:Response Code",
                                        "Monitors|Tests|Simple HTTP validation test|001:URL",
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
                                        "Monitors|Tests|Custom page ping:Total Time (ms)",
                                        "Monitors|Tests|Custom page ping:Type",
                                        "Monitors|Tests|Custom page ping:Monitor ID",
                                        "Monitors|Tests|Custom page ping:Connect Time (ms)",
            };
            String[] notExpectedMetrics = {
                                           "Monitors|Tests|FTP test|001:IP Address",
                                           "Monitors|Tests|FTP test|001:Location Code",
                                           "Monitors|Tests|FTP test|001:Processing Time (ms)",
                                           "Monitors|Tests|FTP test|001:Repeat",
                                           "Monitors|Tests|FTP test|001:Result Code",
                                           "Monitors|Tests|FTP test|001:Monitor ID",
                                           "Monitors|Tests|FTP test|001:Monitor Name",
                                           "Monitors|Tests|Simple HTTP validation test - fail|001:Monitor Name",
                                           "Monitors|Tests|Bad request test http_//ca.com/foo|001:Repeat",
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
            checkNotExistMetrics(notExpectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
