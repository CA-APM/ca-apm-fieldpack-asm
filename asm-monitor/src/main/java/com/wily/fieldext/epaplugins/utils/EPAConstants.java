/*     */ package com.wily.fieldext.epaplugins.utils;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ 
/*     */ public class EPAConstants
/*     */ {
/*   7 */   public static final HashMap<String, String> apmcmMetrics = new HashMap<String, String>();
/*   8 */   public static final HashMap<String, String> apmcmColors = new HashMap<String, String>();
/*     */   public static final String kPerIntervalCounter = "PerIntervalCounter";
/*     */   public static final String kIntCounter = "IntCounter";
/*     */   public static final String kIntAverage = "IntAverage";
/*     */   public static final String kIntRate = "IntRate";
/*     */   public static final String kLongCounter = "LongCounter";
/*     */   public static final String kLongAverage = "LongAverage";
/*     */   public static final String kStringEvent = "StringEvent";
/*     */   public static final String kTimestamp = "Timestamp";
/*     */   public static final String kFloat = "Float";
/*     */   public static final String kDefaultDelimiter = ",";
/*     */   public static final String[] kNoStringArrayProperties;
/*     */   public static final int kBufferWaitTime = 60000;
/*     */   public static final String kJavaNetExceptionRegex = ".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*";
/*     */   public static final String kCreditsCategory = "Credits";
/*     */   public static final String kLogCategory = "Log";
/*     */   public static final String kPSPCategory = "Public Stats";
/*     */   public static final String kStatsCategory = "Stats";
/*     */   public static final String kMsgDigest = "1D0NTF33LT4RDY";
/*     */   public static final String kAPMCMPSPCmd = "rule_psp";
/*     */   public static final String kAPMCMStatsCmd = "rule_stats";
/*     */   public static final String kAPMCMLoginCmd = "acct_token";
/*     */   public static final String kAPMCMLogoutCmd = "acct_logout";
/*     */   public static final String kAPMCMLogsCmd = "rule_log";
/*     */   public static final String kAPMCMCheckptsCmd = "cp_list";
/*     */   public static final String kAPMCMFoldersCmd = "fldr_get";
/*     */   public static final String kAPMCMRuleCmd = "rule_get";
/*     */   public static final String kAPMCMCreditsCmd = "acct_credits";
/*     */   public static final String apmcmMetricTree = "APM Cloud Monitor";
/*     */   public static final String apmcmRootFolder = "root_folder";
/*     */   public static final String apmcmAllFolders = "all_folders";
/*     */   public static final String apmcmAllRules = "all_rules";
/*     */   public static final Boolean apmcmQuiet;
/*     */   public static final String apmcmCallback = "doCallback";
/*     */   public static final String apmcmMethod = "POST";
/*     */   public static final String apmcmPasswordPage = "http://www.watchmouse.com/en/change_passwd.php";
/*     */   public static final int apmcmAuthErrorCode = 1000;
/*     */   public static final int apmcmNormErrorCode = 1001;
/*     */   public static final int apmcmInitRetries = 10;
/*     */   public static final int apmcmThreadRetries = 10;
/*     */ 
/*     */   static
/*     */   {
/*  11 */     apmcmMetrics.put("active", "Active");
/*  12 */     apmcmMetrics.put("alerts", "Alerts Per Interval");
/*  13 */     apmcmMetrics.put("apdex", "Apdex Score");
/*  14 */     apmcmMetrics.put("api", "API Credits Available");
/*  15 */     apmcmMetrics.put("avg_perf_cur", "Performance Current Average (ms)");
/*  16 */     apmcmMetrics.put("avg_perf_day", "Performance Daily Average (ms)");
/*  17 */     apmcmMetrics.put("avg_uptime_cur", "Uptime Current Average (%)");
/*  18 */     apmcmMetrics.put("avg_uptime_day", "Uptime Daily Average (%)");
/*  19 */     apmcmMetrics.put("check", "Check Credits Available");
/*  20 */     apmcmMetrics.put("check_errors", "Check Errors");
/*  21 */     apmcmMetrics.put("checks", "Checks");
/*  22 */     apmcmMetrics.put("code", "Error Code");
/*  23 */     apmcmMetrics.put("color", "Color");
/*  24 */     apmcmMetrics.put("colors", "Performance Status");
/*  25 */     apmcmMetrics.put("consecutive_errors", "Consecutive Errors");
/*  26 */     apmcmMetrics.put("ctime", "Connect Time (ms)");
/*  27 */     apmcmMetrics.put("descr", "Error Description");
/*  28 */     apmcmMetrics.put("dsize", "Download Size (kB)");
/*  29 */     apmcmMetrics.put("dspeed", "Download Speed (kB/s)");
/*  30 */     apmcmMetrics.put("dtime", "Download Time (ms)");
/*  31 */     apmcmMetrics.put("elapsed", "API Call Time (ms)");
/*  32 */     apmcmMetrics.put("end", "Check End Time");
/*  33 */     apmcmMetrics.put("errors", "Errors Per Interval");
/*  34 */     apmcmMetrics.put("gmtoffset", "Agent GMT Offset");
/*  35 */     apmcmMetrics.put("info", "Info");
/*  36 */     apmcmMetrics.put("ipaddr", "IP Address");
/*  37 */     apmcmMetrics.put("loc", "Location Code");
/*  38 */     apmcmMetrics.put("name", "Rule Name");
/*  39 */     apmcmMetrics.put("probe_errors", "Probe Errors");
/*  40 */     apmcmMetrics.put("probes", "Probes");
/*  41 */     apmcmMetrics.put("ptime", "Processing Time (ms)");
/*  42 */     apmcmMetrics.put("repeat", "Repeat");
/*  43 */     apmcmMetrics.put("result", "Result Code");
/*  44 */     apmcmMetrics.put("rid", "Rule ID");
/*  45 */     apmcmMetrics.put("rtime", "Resolve Time (ms)");
/*  46 */     apmcmMetrics.put("secscan", "Vulnerability Scan Credits Available");
/*  47 */     apmcmMetrics.put("sla_poor", "SLA Violation Poor (%)");
/*  48 */     apmcmMetrics.put("sla_warn", "SLA Violation Warning (%)");
/*  49 */     apmcmMetrics.put("sms", "SMS Credits Available");
/*  50 */     apmcmMetrics.put("spi", "Site Performance Index");
/*  51 */     apmcmMetrics.put("start", "Check Start Time");
/*  52 */     apmcmMetrics.put("timepoor", "Time Threshold Poor (ms)");
/*  53 */     apmcmMetrics.put("timewarn", "Time Threshold Warning (ms)");
/*  54 */     apmcmMetrics.put("ttime", "Total Time (ms)");
/*  55 */     apmcmMetrics.put("type", "Type");
/*  56 */     apmcmMetrics.put("tz", "Agent Time Zone");
/*  57 */     apmcmMetrics.put("uptime", "Uptime (%)");
/*  58 */     apmcmMetrics.put("version", "API Version");
/*  59 */     apmcmMetrics.put("xspeed", "Transfer Speed (kB/s)");
/*  60 */     apmcmMetrics.put("xtime", "Transfer Time (ms)");
/*     */ 
/*  65 */     apmcmColors.put("green", "1");
/*  66 */     apmcmColors.put("yellow", "2");
/*  67 */     apmcmColors.put("orange", "2");
/*  68 */     apmcmColors.put("red", "3");
/*     */ 
/*  82 */     kNoStringArrayProperties = new String[0];
/*     */ 
/* 108 */     //TODO: GG reset to true
			  apmcmQuiet = Boolean.valueOf(false);
/*     */   }
/*     */ }

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.EPAConstants
 * JD-Core Version:    0.6.0
 */