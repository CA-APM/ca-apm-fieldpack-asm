package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the AsmMetricReporter.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class ReporterTest implements AsmProperties {

    /**
     * Print debug output to System.out if true.
     *   Run with -DDEBUG=true for debug output.
     */
    public final static boolean DEBUG = TRUE.equals(System.getProperty("DEBUG", FALSE));

    /**
     * Test returnMetricType() to only return LongCounters.
     */
    @Test
    public void returnCounters() {

        try {
            EpaUtils.getProperties().setProperty(REPORT_PER_INTERVAL_COUNTER, FALSE);
            EpaUtils.getProperties().setProperty(REPORT_LONG_AVERAGE, FALSE);

            // metric, value, expected type
            String[] testData = {
                                 "Monitors|Tests:Agent Time Zone",
                                 "PT",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Alerts Per Interval",
                                 "17",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Check End Time",
                                 "2015-04-08 23:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Check Start Time",
                                 "2015-04-08 22:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Connect Time (ms)",
                                 "13",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Download Size (kB)",
                                 "96496828134",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Pi",
                                 "3.141592",
                                 MetricWriter.kFloat,
                                 "Monitors|Tests|Simple HTTP validation test:Download Time (ms)",
                                 "63469847",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:IP Address",
                                 "10.12.12.13",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Location Code",
                                 "ca",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Processing Time (ms)",
                                 "135",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Repeat",
                                 "1",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Resolve Time (ms)",
                                 "123",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Result Code",
                                 "-17",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "435",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor Name",
                                 "monitor",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Total Time (ms)",
                                 "987",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Download Speed (kB/s)",
                                 "987",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Type",
                                 "42",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "abc-456-def",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|DNS test:Alerts Per Interval",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|DNS test:Errors Per Interval",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|DNS test:Probe Errors",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|DNS test:Probes",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|FTP test:Check Errors",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|FTP test:Checks",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|FTP test:Repeat",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|FTP test:Consecutive Errors",
                                 "4",
                                 MetricWriter.kLongCounter,
            };

            runTest(testData);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
    
    /**
     * Test returnMetricType() to return PerIntervalCounter.
     */
    @Test
    public void returnPerInterval() {

        try {
            EpaUtils.getProperties().setProperty(REPORT_PER_INTERVAL_COUNTER, TRUE);
            EpaUtils.getProperties().setProperty(REPORT_LONG_AVERAGE, FALSE);

            // metric, value, expected type
            String[] testData = {
                                 "Monitors|Tests:Agent Time Zone",
                                 "PT",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Alerts Per Interval",
                                 "17",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Check End Time",
                                 "2015-04-08 23:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Check Start Time",
                                 "2015-04-08 22:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Connect Time (ms)",
                                 "13",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Download Size (kB)",
                                 "96496828134",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Pi",
                                 "3.141592",
                                 MetricWriter.kFloat,
                                 "Monitors|Tests|Simple HTTP validation test:Download Time (ms)",
                                 "63469847",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:IP Address",
                                 "10.12.12.13",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Location Code",
                                 "ca",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Processing Time (ms)",
                                 "135",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Repeat",
                                 "1",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Resolve Time (ms)",
                                 "123",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Result Code",
                                 "-17",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "435",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor Name",
                                 "monitor",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Total Time (ms)",
                                 "987",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Download Speed (kB/s)",
                                 "987",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Type",
                                 "42",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "abc-456-def",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|DNS test:Alerts Per Interval",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|DNS test:Errors Per Interval",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|DNS test:Probe Errors",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|DNS test:Probes",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Check Errors",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Checks",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Repeat",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Consecutive Errors",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
            };

            runTest(testData);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test returnMetricType() to return LongAverage.
     */
    @Test
    public void returnLongAverage() {

        try {
            EpaUtils.getProperties().setProperty(REPORT_PER_INTERVAL_COUNTER, FALSE);
            EpaUtils.getProperties().setProperty(REPORT_LONG_AVERAGE, TRUE);

            // metric, value, expected type
            String[] testData = {
                                 "Monitors|Tests:Agent Time Zone",
                                 "PT",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Alerts Per Interval",
                                 "17",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Check End Time",
                                 "2015-04-08 23:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Check Start Time",
                                 "2015-04-08 22:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Connect Time (ms)",
                                 "13",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Download Size (kB)",
                                 "96496828134",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Pi",
                                 "3.141592",
                                 MetricWriter.kFloat,
                                 "Monitors|Tests|Simple HTTP validation test:Download Time (ms)",
                                 "63469847",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:IP Address",
                                 "10.12.12.13",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Location Code",
                                 "ca",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Processing Time (ms)",
                                 "135",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Repeat",
                                 "1",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Resolve Time (ms)",
                                 "123",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Result Code",
                                 "-17",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "435",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor Name",
                                 "monitor",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Total Time (ms)",
                                 "987",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Download Speed (kB/s)",
                                 "987",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Type",
                                 "42",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "abc-456-def",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|DNS test:Alerts Per Interval",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|DNS test:Errors Per Interval",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|DNS test:Probe Errors",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|DNS test:Probes",
                                 "4",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|FTP test:Check Errors",
                                 "4",
                                 MetricWriter.kLongCounter,
             };

            runTest(testData);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test returnMetricType() to return PerIntervalCounter and LongAverage.
     */
    @Test
    public void returnBoth() {

        try {
            EpaUtils.getProperties().setProperty(REPORT_PER_INTERVAL_COUNTER, TRUE);
            EpaUtils.getProperties().setProperty(REPORT_LONG_AVERAGE, TRUE);

            // metric, value, expected type
            String[] testData = {
                                 "Monitors|Tests:Agent Time Zone",
                                 "PT",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Alerts Per Interval",
                                 "17",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Check End Time",
                                 "2015-04-08 23:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Check Start Time",
                                 "2015-04-08 22:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Connect Time (ms)",
                                 "13",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Download Size (kB)",
                                 "96496828134",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Pi",
                                 "3.141592",
                                 MetricWriter.kFloat,
                                 "Monitors|Tests|Simple HTTP validation test:Download Time (ms)",
                                 "63469847",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:IP Address",
                                 "10.12.12.13",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Location Code",
                                 "ca",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Processing Time (ms)",
                                 "135",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Repeat",
                                 "1",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Resolve Time (ms)",
                                 "123",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Result Code",
                                 "-17",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "435",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor Name",
                                 "monitor",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Total Time (ms)",
                                 "987",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Download Speed (kB/s)",
                                 "987",
                                 MetricWriter.kLongAverage,
                                 "Monitors|Tests|Simple HTTP validation test:Type",
                                 "42",
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "abc-456-def",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|DNS test:Alerts Per Interval",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|DNS test:Errors Per Interval",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|DNS test:Probe Errors",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|DNS test:Probes",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Check Errors",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Checks",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Repeat",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
                                 "Monitors|Tests|FTP test:Consecutive Errors",
                                 "4",
                                 MetricWriter.kPerIntervalCounter,
            };

            runTest(testData);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /** 
     * Run a test with a set of test data.
     * @param testData the test data, must be tuples of (metric, value, expected type)
     */
    protected void runTest(String[] testData) {
        HashMap<String, String> metricMap = new HashMap<String, String>();

        for (int index = 0; index < testData.length; index += 3) {
            String key = testData[index];
            metricMap.put(key, testData[index+1]);

            Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
            Map.Entry<String, String> metricPair = metricIt.next();

            // exactly one entry
            String metricType = AsmMetricReporter.returnMetricType(metricPair.getKey(),
                                                                   metricPair.getValue());

            if (DEBUG) {
                System.out.println(key + ", " + metricMap.get(key) + " -> " + metricType);
            }

            Assert.assertEquals(testData[index], testData[index+2], metricType);

            // remove test value from map
            metricMap.remove(key);
        }

    }
}
