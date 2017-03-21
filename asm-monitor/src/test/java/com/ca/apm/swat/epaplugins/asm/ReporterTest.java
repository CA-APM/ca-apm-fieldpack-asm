package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;

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
     * Test returnMetricType().
     */
    @Test
    public void returnMetricType() {

        try {

            // kPerIntervalCounter and kIntAverage do not make sense
            // as we are sending metrics only every 5 minutes, they are 0 in between

            // metric, value, expected type
            String[] testData = {
                                 "Monitors|Tests:Agent Time Zone",
                                 "PT",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Alerts Per Interval",
                                 "17",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Check End Time",
                                 "2015-04-08 23:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Check Start Time",
                                 "2015-04-08 22:56:09",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Connect Time (ms)",
                                 "13",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Download Size (kB)",
                                 "96496828134",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kLongCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Pi",
                                 "3.141592",
                                 MetricWriter.kFloat,
                                 "Monitors|Tests|Simple HTTP validation test:Download Time (ms)",
                                 "63469847",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:IP Address",
                                 "10.12.12.13",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Location Code",
                                 "ca",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Processing Time (ms)",
                                 "135",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Repeat",
                                 "1",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Resolve Time (ms)",
                                 "123",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Result Code",
                                 "-17",
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "435",
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor Name",
                                 "monitor",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|Simple HTTP validation test:Total Time (ms)",
                                 "987",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Download Speed (kB/s)",
                                 "987",
                                 //MetricWriter.kIntAverage,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Type",
                                 "42",
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|Simple HTTP validation test:Monitor ID",
                                 "abc-456-def",
                                 MetricWriter.kStringEvent,
                                 "Monitors|Tests|DNS test:Alerts Per Interval",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|DNS test:Errors Per Interval",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|DNS test:Probe Errors",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|DNS test:Probes",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|FTP test:Check Errors",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|FTP test:Checks",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|FTP test:Repeat",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
                                 "Monitors|Tests|FTP test:Consecutive Errors",
                                 "4",
                                 //MetricWriter.kPerIntervalCounter,
                                 MetricWriter.kIntCounter,
            };


            HashMap<String, String> metricMap = new HashMap<String, String>();

            for (int index = 0; index < testData.length; index += 3) {
                String key = testData[index];
                metricMap.put(key, testData[index+1]);

                Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
                Map.Entry<String, String> metricPair = metricIt.next();

                // exactly one entry
                String metricType = AsmMetricReporter.returnMetricType(metricPair.getKey(),
                                                                       metricPair.getValue());

                //if (DEBUG) {
                    System.out.println(key + ", " + metricMap.get(key) + " -> " + metricType);
                //}
                
                Assert.assertEquals(testData[index], testData[index+2], metricType);

                // remove test value from map
                metricMap.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}