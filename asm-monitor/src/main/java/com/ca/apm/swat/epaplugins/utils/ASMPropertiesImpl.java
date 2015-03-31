package com.ca.apm.swat.epaplugins.utils;

import java.util.HashMap;

/**
 * Contains constant maps for CA App Synthetic Monitor EPA plugin.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class ASMPropertiesImpl implements ASMProperties {

    public static final HashMap<String, String> APM_CM_METRICS = new HashMap<String, String>();
    public static final HashMap<String, String> APM_CM_COLORS = new HashMap<String, String>();

    static
    {
        APM_CM_METRICS.put(kAPMCMActive, "Active");
        APM_CM_METRICS.put("alerts", "Alerts Per Interval");
        APM_CM_METRICS.put("apdex", "Apdex Score");
        APM_CM_METRICS.put("api", "API Credits Available");
        APM_CM_METRICS.put("avg_perf_cur", "Performance Current Average (ms)");
        APM_CM_METRICS.put("avg_perf_day", "Performance Daily Average (ms)");
        APM_CM_METRICS.put("avg_uptime_cur", "Uptime Current Average (%)");
        APM_CM_METRICS.put("avg_uptime_day", "Uptime Daily Average (%)");
        APM_CM_METRICS.put("check", "Check Credits Available");
        APM_CM_METRICS.put("check_errors", "Check Errors");
        APM_CM_METRICS.put("checks", "Checks");
        APM_CM_METRICS.put(kAPMCMCode, "Error Code");
        APM_CM_METRICS.put(kAPMCMColor, "Color");
        APM_CM_METRICS.put(kAPMCMColors, "Performance Status");
        APM_CM_METRICS.put("consecutive_errors", "Consecutive Errors");
        APM_CM_METRICS.put("ctime", "Connect Time (ms)");
        APM_CM_METRICS.put("descr", "Error Description");
        APM_CM_METRICS.put("dsize", "Download Size (kB)");
        APM_CM_METRICS.put("dspeed", "Download Speed (kB/s)");
        APM_CM_METRICS.put("dtime", "Download Time (ms)");
        APM_CM_METRICS.put("elapsed", "API Call Time (ms)");
        APM_CM_METRICS.put("end", "Check End Time");
        APM_CM_METRICS.put("errors", "Errors Per Interval");
        APM_CM_METRICS.put("gmtoffset", "Agent GMT Offset");
        APM_CM_METRICS.put("info", "Info");
        APM_CM_METRICS.put("ipaddr", "IP Address");
        APM_CM_METRICS.put("loc", "Location Code");
        APM_CM_METRICS.put("name", "Rule Name");
        APM_CM_METRICS.put("probe_errors", "Probe Errors");
        APM_CM_METRICS.put("probes", "Probes");
        APM_CM_METRICS.put("ptime", "Processing Time (ms)");
        APM_CM_METRICS.put("repeat", "Repeat");
        APM_CM_METRICS.put("result", "Result Code");
        APM_CM_METRICS.put("rid", "Rule ID");
        APM_CM_METRICS.put("rtime", "Resolve Time (ms)");
        APM_CM_METRICS.put("secscan", "Vulnerability Scan Credits Available");
        APM_CM_METRICS.put("sla_poor", "SLA Violation Poor (%)");
        APM_CM_METRICS.put("sla_warn", "SLA Violation Warning (%)");
        APM_CM_METRICS.put("sms", "SMS Credits Available");
        APM_CM_METRICS.put("spi", "Site Performance Index");
        APM_CM_METRICS.put("start", "Check Start Time");
        APM_CM_METRICS.put("timepoor", "Time Threshold Poor (ms)");
        APM_CM_METRICS.put("timewarn", "Time Threshold Warning (ms)");
        APM_CM_METRICS.put("ttime", "Total Time (ms)");
        APM_CM_METRICS.put(kAPMCMType, "Type");
        APM_CM_METRICS.put("tz", "Agent Time Zone");
        APM_CM_METRICS.put("uptime", "Uptime (%)");
        APM_CM_METRICS.put("version", "API Version");
        APM_CM_METRICS.put("xspeed", "Transfer Speed (kB/s)");
        APM_CM_METRICS.put("xtime", "Transfer Time (ms)");

        APM_CM_COLORS.put("green", "1");
        APM_CM_COLORS.put("yellow", "2");
        APM_CM_COLORS.put("orange", "2");
        APM_CM_COLORS.put("red", "3");
    }
}
