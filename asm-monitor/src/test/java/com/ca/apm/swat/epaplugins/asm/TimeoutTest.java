package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.monitor.JMeterScriptHandler;
import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the rule_log API and asm.alwaysReportTimeout property.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class TimeoutTest extends FileTest {

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
     * Test with asm.alwaysReportTimeout=true.
     */
    @Test
    public void getLogScriptWithTimeout() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, TRUE);
            EpaUtils.getProperties().setProperty(TIMEOUT_REPORT_ALWAYS, TRUE);

            String folder = "Tests";
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_timeout.json");
            String logRequest = accessor.executeApi(LOGS_CMD, null);

            Monitor monitor = MonitorFactory.createMonitor("dummy", HTTP_MONITOR, folder, null, EMPTY_STRING, true);
            monitor.setSuccessor(new JMeterScriptHandler());

            // call API
            HashMap<String, String> metricMap =
                    monitor.generateMetrics(logRequest, metricPrefix);

            // print
            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    if (key.contains("Response Code")
                            || key.contains("Result Code")
                            || key.contains("Status Message")) {
                        System.out.println(key + " = " + metricMap.get(key));
                    }
                }
            }

            // check
            final String prefix     = "Monitors|Tests|Simple JMeter recording";
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String MONTREAL   = "|america-north|Canada|Montreal";
            final String PHOENIX    = "|america-north|United States|Phoenix";

            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + ":Result Code"                             ), "0");
            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + ":Status Message Value"                    ), "0");
            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + "|001 MyAccount/login:Response Code"       ), "200");
            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + "|001 MyAccount/login:Status Message Value"), "200");

            Assert.assertEquals(metricMap.get(prefix + CALGARY   + ":Result Code"                             ), "1042");
            Assert.assertEquals(metricMap.get(prefix + CALGARY   + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + CALGARY   + "|001 MyAccount/login:Response Code"       ), "200");
            Assert.assertEquals(metricMap.get(prefix + CALGARY   + "|001 MyAccount/login:Status Message Value"), "600");

            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + ":Result Code"                             ), "7011");
            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + "|001 MyAccount/login:Response Code"       ), "200");
            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + "|001 MyAccount/login:Status Message Value"), "600");

            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + ":Result Code"                             ), "1042");
            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + "|001 MyAccount/login:Response Code"       ), "404");
            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + "|001 MyAccount/login:Status Message Value"), "600");

            Assert.assertEquals(metricMap.get(prefix + TORONTO   + ":Result Code"                             ), "7011");
            Assert.assertEquals(metricMap.get(prefix + TORONTO   + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + TORONTO   + "|001 MyAccount/login:Response Code"       ), "404");
            Assert.assertEquals(metricMap.get(prefix + TORONTO   + "|001 MyAccount/login:Status Message Value"), "600");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test with asm.alwaysReportTimeout=false.
     */
    @Test
    public void getLogScriptWithoutTimeout() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(METRICS_LOGS, TRUE);
            EpaUtils.getProperties().setProperty(DISPLAY_STATIONS, TRUE);
            EpaUtils.getProperties().setProperty(TIMEOUT_REPORT_ALWAYS, FALSE);

            String folder = "Tests";
            String metricPrefix = MONITOR_METRIC_PREFIX + folder;
            
            // load file
            accessor.loadFile(LOGS_CMD, "target/test-classes/rule_log_timeout.json");
            String logRequest = accessor.executeApi(LOGS_CMD, null);

            Monitor monitor = MonitorFactory.createMonitor("dummy", HTTP_MONITOR, folder, null, EMPTY_STRING, true);
            monitor.setSuccessor(new JMeterScriptHandler());

            // call API
            HashMap<String, String> metricMap =
                    monitor.generateMetrics(logRequest, metricPrefix);

            // print
            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    if (key.contains("Response Code")
                            || key.contains("Result Code")
                            || key.contains("Status Message")) {
                        System.out.println(key + " = " + metricMap.get(key));
                    }
                }
            }

            // check
            final String prefix     = "Monitors|Tests|Simple JMeter recording";
            final String CALGARY    = "|america-north|Canada|Calgary";
            final String TORONTO    = "|america-north|Canada|Toronto";
            final String VANCOUVER  = "|america-north|Canada|Vancouver";
            final String MONTREAL   = "|america-north|Canada|Montreal";
            final String PHOENIX    = "|america-north|United States|Phoenix";

            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + ":Result Code"                             ), "0");
            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + ":Status Message Value"                    ), "0");
            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + "|001 MyAccount/login:Response Code"       ), "200");
            Assert.assertEquals(metricMap.get(prefix + PHOENIX   + "|001 MyAccount/login:Status Message Value"), "200");

            Assert.assertEquals(metricMap.get(prefix + CALGARY   + ":Result Code"                             ), "1042");
            Assert.assertEquals(metricMap.get(prefix + CALGARY   + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + CALGARY   + "|001 MyAccount/login:Response Code"       ), "200");
            Assert.assertEquals(metricMap.get(prefix + CALGARY   + "|001 MyAccount/login:Status Message Value"), "200");

            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + ":Result Code"                             ), "7011");
            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + "|001 MyAccount/login:Response Code"       ), "200");
            Assert.assertEquals(metricMap.get(prefix + VANCOUVER + "|001 MyAccount/login:Status Message Value"), "200");

            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + ":Result Code"                             ), "1042");
            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + "|001 MyAccount/login:Response Code"       ), "404");
            Assert.assertEquals(metricMap.get(prefix + MONTREAL  + "|001 MyAccount/login:Status Message Value"), "404");

            Assert.assertEquals(metricMap.get(prefix + TORONTO   + ":Result Code"                             ), "7011");
            Assert.assertEquals(metricMap.get(prefix + TORONTO   + ":Status Message Value"                    ), "600");
            Assert.assertEquals(metricMap.get(prefix + TORONTO   + "|001 MyAccount/login:Response Code"       ), "404");
            Assert.assertEquals(metricMap.get(prefix + TORONTO   + "|001 MyAccount/login:Status Message Value"), "404");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

 }
