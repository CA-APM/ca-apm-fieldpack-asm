package com.ca.apm.swat.epaplugins.utils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Contains constant maps for CA App Synthetic Monitor EPA plugin.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public abstract class AsmPropertiesImpl implements AsmProperties {

    /**
     * Mapping table of ASM tags to APM metric names.
     */
    public static final HashMap<String, String> ASM_METRICS = new HashMap<String, String>();
    
    /**
     * Mapping table of status indicator colors.
     */
    public static final HashMap<String, String> ASM_COLORS = new HashMap<String, String>();
    
    /**
     * Tags of rule_stats that need to be ignored (due to clashing of names with tags of rule_log and rule_psp)
     */
    public static final HashSet<String> ASM_RULE_STATS_TAGS_TO_IGNORE = new HashSet<String>();

    static {
        ASM_METRICS.put(ACTIVE_TAG, "Active");
        ASM_METRICS.put("alerts", METRIC_NAME_ALERTS_PER_INTERVAL);
        ASM_METRICS.put("apdex", "Apdex Score");
        ASM_METRICS.put("api", "API Credits Available");
        ASM_METRICS.put("avg_perf_cur", "Performance Current Average (ms)");
        ASM_METRICS.put("avg_perf_day", "Performance Daily Average (ms)");
        ASM_METRICS.put("avg_uptime_cur", "Uptime Current Average (%)");
        ASM_METRICS.put("avg_uptime_day", "Uptime Daily Average (%)");
        ASM_METRICS.put("check", "Check Credits Available");
        ASM_METRICS.put("check_errors", METRIC_NAME_CHECK_ERRORS);
        ASM_METRICS.put("checks", METRIC_NAME_CHECKS);
        ASM_METRICS.put(CODE_TAG, "Error Code");
        ASM_METRICS.put(COLOR_TAG, "Color");
        ASM_METRICS.put(COLORS_TAG, "Performance Status");
        ASM_METRICS.put("consecutive_errors", METRIC_NAME_CONSECUTIVE_ERRORS);
        ASM_METRICS.put("ctime", "Connect Time (ms)");
        ASM_METRICS.put(DESCR_TAG, "Error Description");
        ASM_METRICS.put("dsize", "Download Size (kB)");
        ASM_METRICS.put("dspeed", "Download Speed (kB/s)");
        ASM_METRICS.put("dtime", "Download Time (ms)");
        ASM_METRICS.put("elapsed", "API Call Time (ms)");
        ASM_METRICS.put("end", "Check End Time");
        ASM_METRICS.put(ERRORS_TAG, "Consecutive Errors");
        ASM_METRICS.put("errorsince", "Error Since");
        ASM_METRICS.put(GMT_OFFSET_TAG, "Agent GMT Offset");
        ASM_METRICS.put("info", "Info");
        ASM_METRICS.put("ipaddr", "IP Address");
        ASM_METRICS.put("lastcheck", "Last Check");
        ASM_METRICS.put("loc", "Location Code");
        ASM_METRICS.put("name", "Monitor Name");
        ASM_METRICS.put("probe_errors", METRIC_NAME_PROBE_ERRORS);
        ASM_METRICS.put("probes", METRIC_NAME_PROBES);
        ASM_METRICS.put("ptime", "Processing Time (ms)");
        ASM_METRICS.put("repeat", METRIC_NAME_REPEAT);
        ASM_METRICS.put(RESULT_TAG, RESULT_CODE);
        ASM_METRICS.put("id", "ID");
        ASM_METRICS.put("rid", "Monitor ID");
        ASM_METRICS.put("rtime", "Resolve Time (ms)");
        ASM_METRICS.put("secscan", "Vulnerability Scan Credits Available");
        ASM_METRICS.put("sla_poor", "SLA Violation Poor (%)");
        ASM_METRICS.put("sla_warn", "SLA Violation Warning (%)");
        ASM_METRICS.put("slapoor", "SLA Violation Poor (%)");
        ASM_METRICS.put("slawarn", "SLA Violation Warning (%)");
        ASM_METRICS.put("sms", "SMS Credits Available");
        ASM_METRICS.put("spi", "Site Performance Index");
        ASM_METRICS.put("start", "Check Start Time");
        ASM_METRICS.put("timepoor", "Time Threshold Poor (ms)");
        ASM_METRICS.put("timewarn", "Time Threshold Warning (ms)");
        ASM_METRICS.put("ttime", "Total Time (ms)");
        ASM_METRICS.put(TYPE_TAG, "Type");
        ASM_METRICS.put(TIMEZONE_TAG, "Agent Time Zone");
        ASM_METRICS.put("uptime", "Uptime (%)");
        ASM_METRICS.put("version", "API Version");
        ASM_METRICS.put("xspeed", "Transfer Speed (kB/s)");
        ASM_METRICS.put("xtime", "Transfer Time (ms)");

        // mapping table of status indicator colors
        ASM_COLORS.put(GREEN, "1");
        ASM_COLORS.put(YELLOW, "2");
        ASM_COLORS.put(ORANGE, "2");
        ASM_COLORS.put(RED, "3");
        
        ASM_RULE_STATS_TAGS_TO_IGNORE.add("ttime");
        ASM_RULE_STATS_TAGS_TO_IGNORE.add("ctime");
        ASM_RULE_STATS_TAGS_TO_IGNORE.add("ptime");
        ASM_RULE_STATS_TAGS_TO_IGNORE.add("dtime");
        ASM_RULE_STATS_TAGS_TO_IGNORE.add("dsize");
        ASM_RULE_STATS_TAGS_TO_IGNORE.add("consecutive_errors");
    }
}
