package com.wily.fieldext.epaplugins.utils;

import java.util.HashMap;

public class EPAConstants
{
  public static final HashMap<String, String> apmcmMetrics = new HashMap<String, String>();
  public static final HashMap<String, String> apmcmColors = new HashMap<String, String>();
  public static final String kPerIntervalCounter = "PerIntervalCounter";
  public static final String kIntCounter = "IntCounter";
  public static final String kIntAverage = "IntAverage";
  public static final String kIntRate = "IntRate";
  public static final String kLongCounter = "LongCounter";
  public static final String kLongAverage = "LongAverage";
  public static final String kStringEvent = "StringEvent";
  public static final String kTimestamp = "Timestamp";
  public static final String kFloat = "Float";
  public static final String kDefaultDelimiter = ",";
  public static final String[] kNoStringArrayProperties;
  public static final int kBufferWaitTime = 60000;
  public static final String kJavaNetExceptionRegex = ".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*";
  public static final String kCreditsCategory = "Credits";
  public static final String kLogCategory = "Log";
  public static final String kPSPCategory = "Public Stats";
  public static final String kStatsCategory = "Stats";
  public static final String kMsgDigest = "1D0NTF33LT4RDY";
  public static final String kAPMCMPSPCmd = "rule_psp";
  public static final String kAPMCMStatsCmd = "rule_stats";
  public static final String kAPMCMLoginCmd = "acct_token";
  public static final String kAPMCMLogoutCmd = "acct_logout";
  public static final String kAPMCMLogsCmd = "rule_log";
  public static final String kAPMCMCheckptsCmd = "cp_list";
  public static final String kAPMCMFoldersCmd = "fldr_get";
  public static final String kAPMCMRuleCmd = "rule_get";
  public static final String kAPMCMCreditsCmd = "acct_credits";
  public static final String apmcmMetricTree = "APM Cloud Monitor";
  public static final String apmcmRootFolder = "root_folder";
  public static final String apmcmAllFolders = "all_folders";
  public static final String apmcmAllRules = "all_rules";
  public static final Boolean apmcmQuiet;
  public static final String apmcmCallback = "doCallback";
  public static final String apmcmMethod = "POST";
  public static final String apmcmPasswordPage = "http://www.watchmouse.com/en/change_passwd.php";
  public static final int apmcmAuthErrorCode = 1000;
  public static final int apmcmNormErrorCode = 1001;
  public static final int apmcmInitRetries = 10;
  public static final int apmcmThreadRetries = 10;

  static
  {
    apmcmMetrics.put("active", "Active");
    apmcmMetrics.put("alerts", "Alerts Per Interval");
    apmcmMetrics.put("apdex", "Apdex Score");
    apmcmMetrics.put("api", "API Credits Available");
    apmcmMetrics.put("avg_perf_cur", "Performance Current Average (ms)");
    apmcmMetrics.put("avg_perf_day", "Performance Daily Average (ms)");
    apmcmMetrics.put("avg_uptime_cur", "Uptime Current Average (%)");
    apmcmMetrics.put("avg_uptime_day", "Uptime Daily Average (%)");
    apmcmMetrics.put("check", "Check Credits Available");
    apmcmMetrics.put("check_errors", "Check Errors");
    apmcmMetrics.put("checks", "Checks");
    apmcmMetrics.put("code", "Error Code");
    apmcmMetrics.put("color", "Color");
    apmcmMetrics.put("colors", "Performance Status");
    apmcmMetrics.put("consecutive_errors", "Consecutive Errors");
    apmcmMetrics.put("ctime", "Connect Time (ms)");
    apmcmMetrics.put("descr", "Error Description");
    apmcmMetrics.put("dsize", "Download Size (kB)");
    apmcmMetrics.put("dspeed", "Download Speed (kB/s)");
    apmcmMetrics.put("dtime", "Download Time (ms)");
    apmcmMetrics.put("elapsed", "API Call Time (ms)");
    apmcmMetrics.put("end", "Check End Time");
    apmcmMetrics.put("errors", "Errors Per Interval");
    apmcmMetrics.put("gmtoffset", "Agent GMT Offset");
    apmcmMetrics.put("info", "Info");
    apmcmMetrics.put("ipaddr", "IP Address");
    apmcmMetrics.put("loc", "Location Code");
    apmcmMetrics.put("name", "Rule Name");
    apmcmMetrics.put("probe_errors", "Probe Errors");
    apmcmMetrics.put("probes", "Probes");
    apmcmMetrics.put("ptime", "Processing Time (ms)");
    apmcmMetrics.put("repeat", "Repeat");
    apmcmMetrics.put("result", "Result Code");
    apmcmMetrics.put("rid", "Rule ID");
    apmcmMetrics.put("rtime", "Resolve Time (ms)");
    apmcmMetrics.put("secscan", "Vulnerability Scan Credits Available");
    apmcmMetrics.put("sla_poor", "SLA Violation Poor (%)");
    apmcmMetrics.put("sla_warn", "SLA Violation Warning (%)");
    apmcmMetrics.put("sms", "SMS Credits Available");
    apmcmMetrics.put("spi", "Site Performance Index");
    apmcmMetrics.put("start", "Check Start Time");
    apmcmMetrics.put("timepoor", "Time Threshold Poor (ms)");
    apmcmMetrics.put("timewarn", "Time Threshold Warning (ms)");
    apmcmMetrics.put("ttime", "Total Time (ms)");
    apmcmMetrics.put("type", "Type");
    apmcmMetrics.put("tz", "Agent Time Zone");
    apmcmMetrics.put("uptime", "Uptime (%)");
    apmcmMetrics.put("version", "API Version");
    apmcmMetrics.put("xspeed", "Transfer Speed (kB/s)");
    apmcmMetrics.put("xtime", "Transfer Time (ms)");

    apmcmColors.put("green", "1");
    apmcmColors.put("yellow", "2");
    apmcmColors.put("orange", "2");
    apmcmColors.put("red", "3");

    kNoStringArrayProperties = new String[0];

    //TODO: GG reset to true
			  apmcmQuiet = Boolean.valueOf(false);
  }
}

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.EPAConstants
 * JD-Core Version:    0.6.0
 */