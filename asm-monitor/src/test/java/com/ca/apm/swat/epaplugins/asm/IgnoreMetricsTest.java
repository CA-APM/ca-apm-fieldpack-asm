package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.wily.introscope.epagent.EpaUtils;

/**
 * This test checks if asm.ignoreMetrics is handled correctly.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class IgnoreMetricsTest extends FileTest {

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
     * Test asm.ignoreMetrics property handling.
     */
    @Test
    public void ignoreOneMetric() {

        // set properties
        Properties props = EpaUtils.getProperties(); 
        props.setProperty(METRICS_LOGS, TRUE);
        props.setProperty(IGNORE_METRICS, "Repeat");
        props.setProperty(DISPLAY_STATIONS, "true");

        // metricMap should contain those entries
        String[] expectedMetrics = {
                                    "App Synthetic Monitor|Monitors|Tests:Agent Time Zone",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Alerts Per Interval",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check End Time",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check Start Time",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Connect Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Size (kB)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:IP Address",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Location Code",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Processing Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID"
        };

        String[] notExpectedMetrics = {
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Repeat"
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
//            Monitor monitor = MonitorFactory.getMonitor("Simple HTTP validation test",
//                HTTP_MONITOR, folder, EMPTY_STRING_ARRAY);
            int numMonitors = 5;
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;

            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_http.json");

            // call API
            HashMap<String, String> metricMap =
                    requestHelper.getLogs(folder, numMonitors, metricPrefix);

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
     * Test asm.ignoreMetrics property handling.
     */
    @Test
    public void ignoreSeveralMetrics() {

        // set properties
        Properties props = EpaUtils.getProperties(); 
        props.setProperty(METRICS_LOGS, TRUE);
        props.setProperty(IGNORE_METRICS, "Repeat,Type,Alerts Per Interval,Download Time (ms)");
        props.setProperty(DISPLAY_STATIONS, "true");

        // metricMap should contain those entries
        String[] expectedMetrics = {
                                    "App Synthetic Monitor|Monitors|Tests:Agent Time Zone",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check End Time",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check Start Time",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Connect Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Size (kB)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:IP Address",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Location Code",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Processing Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID"
        };

        String[] notExpectedMetrics = {
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Repeat",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Alerts Per Interval",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Alerts Per Interval",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Alerts Per Interval",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Alerts Per Interval",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Download Time (ms)",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Time (ms)",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Download Time (ms)",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Download Time (ms)",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Type",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Type",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Type",
                                       "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type"
        };

        runTest(props, expectedMetrics, notExpectedMetrics);
    }

    /**
     * Test asm.ignoreMetrics property handling.
     */
    @Test
    public void ignoreEmptyMetric() {

        // set properties
        Properties props = EpaUtils.getProperties(); 
        props.setProperty(METRICS_LOGS, TRUE);
        props.setProperty(IGNORE_METRICS, EMPTY_STRING);
        props.setProperty(DISPLAY_STATIONS, "true");

        // metricMap should contain those entries
        String[] expectedMetrics = {
                                    "App Synthetic Monitor|Monitors|Tests:Agent Time Zone",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Alerts Per Interval",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check End Time",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Check Start Time",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Connect Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Size (kB)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|New York:Download Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:IP Address",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Location Code",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Processing Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Resolve Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Result Code",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor ID",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Monitor Name",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Total Time (ms)",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|San Diego:Type",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Charlotte:Repeat",
                                    "App Synthetic Monitor|Monitors|Tests|Simple HTTP validation test|america-north|United States|Phoenix:Monitor ID"
        };

        runTest(props, expectedMetrics, EMPTY_STRING_ARRAY);
    }
}
