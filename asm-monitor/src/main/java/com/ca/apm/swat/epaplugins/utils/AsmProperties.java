package com.ca.apm.swat.epaplugins.utils;

import com.ca.apm.swat.epaplugins.asm.har.Entry;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Contains all constants for CA App Synthetic Monitor EPA plugin.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface AsmProperties {

    // constants
    public static final String EMPTY_STRING             = "";
    public static final String[] EMPTY_STRING_ARRAY     = new String[0];
    public static final String ZERO                     = "0";
    public static final String ONE                      = "1"; 
    public static final String YES                      = "y";
    public static final String NO                       = "n";
    public static final String NO_TYPE                  = "no type";
    public static final String DATE_FORMAT              = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DELIMITER        = ",";
    public static final String BLANK                    = " ";
    public static final String TRUE                     = "true";
    public static final String FALSE                    = "false";
    public static final int    UNLIMITED_RETRIES        = -1;

    public static final String ASM_PRODUCT_NAME         = "App Synthetic Monitor";
    public static final String ASM_PRODUCT_NAME_SHORT   = "ASM";

    public static final String PROPERTY_FILE_NAME       = "AppSyntheticMonitor.properties";
    public static final String PROPERTY_FILE_DIR        = "config";

    public static final String PASSWORD_URL             =
            "https://asm.ca.com/en/change_passwd.php";

    public static final String VERSION                  = "asm.version";
    public static final String LOCALE                   = "asm.locale";
    public static final String FIX_AMPERSAND            = "asm.fixAmpersand";   
    public static final String DEFAULT_LOCALE           = "en_US";
    public static final String ENCODING                 = "asm.encoding";
    public static final String WAIT_TIME                = "asm.waittime";
    public static final String CONFIG_UPDATE_INTERVAL   = "asm.configUpdateInterval";
    public static final String DISPLAY_STATIONS         = "asm.displayMonitoringStations";
    public static final String PRINT_API_STATISTICS     = "asm.printApiStatistics";
    public static final String CONNECTION_RETRIES       = "asm.connectionRetries";
    public static final String CONNECTION_RETRY_INTERVAL = "asm.connectionRetryInterval";

    // which metrics to get and display
    public static final String METRICS_STATS_FOLDER     = "asm.metrics.stats.folder";
    public static final String METRICS_STATS_MONITOR    = "asm.metrics.stats.monitor";
    public static final String METRICS_CREDITS          = "asm.metrics.credits";
    public static final String METRICS_PUBLIC           = "asm.metrics.public";
    public static final String METRICS_LOGS             = "asm.metrics.logs";
    public static final String REPORT_LABELS_IN_PATH    = "asm.metrics.labelInPath";
    public static final String METRICS_DOWNLOAD_FULL    = "asm.metrics.download.full";
    public static final String METRICS_HAR_REQUESTS     = "asm.metrics.har.requests";
    public static final String METRICS_HAR_FPM          = "asm.metrics.har.fpm";

    
    public static final String USE_PROXY                = "asm.useProxy";
    public static final String PROXY_HOST               = "asm.proxyHost";
    public static final String PROXY_PORT               = "asm.proxyPort";
    public static final String PROXY_USER               = "asm.proxyUser";
    public static final String PROXY_PASSWORD_ENCRYPTED = "asm.proxyPasswordEncrypted";
    public static final String PROXY_PASSWORD           = "asm.proxyPassword";
    public static final String HTTP_READ_TIMEOUT        = "asm.httpReadTimeout";
    public static final String REQUEST_RETRY_DELAY      = "asm.requestRetryDelay";

    public static final String LOCAL_TEST               = "asm.localtest";
    public static final String LOCAL_TEST_PATH          = "asm.localtestpath";
    public static final String STAGING                  = "asm.staging";
    public static final String STAGING_AUTH             = "asm.staging.auth";

    public static final String URL                      = "asm.URL";
    public static final String USER                     = "asm.userEmail";
    public static final String PASSWORD                 = "asm.APIPassword";
    public static final String PASSWORD_ENCRYPTED       = "asm.APIPasswordEncrypted";
    public static final String ACCOUNT                  = "asm.account";
    public static final String LOGS_FOR_USER            = "asm.logsForUser";
    public static final String INCLUDE_FOLDERS          = "asm.includeFolders";
    public static final String EXCLUDE_FOLDERS          = "asm.excludeFolders";
    public static final String QUERY_BY_FOLDERS         = "asm.queryByFolders";
    public static final String FOLDER_THREADS           = "asm.folderThreads";
    public static final String SKIP_INACTIVE_FOLDERS    = "asm.skipInactiveFolders";
    public static final String SKIP_INACTIVE_MONITORS   = "asm.skipInactiveMonitors";
    public static final String FOLDER_PREFIX            = "asm.folder.";
    public static final String NUM_LOGS                 = "asm.numlogs";
    public static final String MAX_LOG_LIMIT            = "asm.maxLogLimit";
    public static final String LEGACY_OUTPUT_FORMAT     = "asm.legacyOutputFormat";
    public static final String IGNORE_TAGS              = "asm.ignoreTags";
    public static final String IGNORE_METRICS           = "asm.ignoreMetrics";
    public static final String SUPPRESS_STEP_WITH_CODES = "asm.suppressStepsWithCodes";
    public static final String REPORT_STRING_RESULTS    = "asm.reportStringResults";
    public static final String REPORT_ASSERTION_FAILURES_AS = "asm.reportAssertionFailureAs";
    public static final String REPORT_JMETER_STEPS      = "asm.reportJMeterSteps";
    public static final String REPORT_JTL_SUBTREE       = "asm.reportJTLSubtree";
    public static final String METRICS_STATS_WDW_SIZE   = "asm.metrics.stats.windowSize";
    public static final String PRINT_ASM_NODE           = "asm.printAsmNode";
    public static final String STEP_FORMAT_DIGITS       = "asm.stepFormatDigits";
    public static final String STEP_FORMAT_URL          = "asm.stepFormatURL";
    public static final String STEP_FORMAT_PREFIX       = "asm.stepFormatPrefix";
    public static final String STEP_FORMAT_ALWAYS       = "asm.stepFormatAlways";
    public static final String TIMEOUT_REPORT_ALWAYS    = "asm.alwaysReportTimeout";
    public static final String SKIP_NO_CHECKPOINT_AVAILABLE = "asm.skipNoCheckpointAvailable";
    public static final String REPORT_PER_INTERVAL_COUNTER  = "asm.report.perIntervalCounter";
    public static final String REPORT_LONG_AVERAGE      = "asm.report.longAverage";
    public static final String REPORT_MAINTENANCE       = "asm.report.maintenance";

    // folder and other constants in properties
    public static final String ROOT_FOLDER              = "root_folder";
    public static final String ALL_FOLDERS              = "all_folders";

    // TODO: not implemented yet!!!
    public static final String LOG_ASM_ISSUES           = "asm.logASMIssues";
    public static final String IGNORE_TAGS_MONITOR      = "asm.ignoreTags.monitor";
    public static final String IGNORE_METRICS_MONITOR   = "asm.ignoreMetrics.monitor";
    public static final String INCLUDE_TAGS             = "asm.includeTags";
    public static final String INCLUDE_MONITORS         = "asm.includeMonitors";
    public static final String RESULT_GROUPS            = "asm.resultsGroups";
    public static final String ALL_MONITORS             = "all_monitors";
    public static final String ALL_TAGS                 = "all_tags";
    public static final String GROUP_BY_FOLDER          = "by_folder";
    public static final String GROUP_BY_STATION         = "by_station";
    public static final String GROUP_MIXED              = "mixed";
    public static final String GROUP_NONE               = "no_groups";
    public static final String RESPONSE_CODES           = "asm.responseCodes";

    // ASM API error codes
    public static final int    ERROR_OK                 = 0;
    public static final int    ERROR_AUTHORIZATION      = 1000;
    public static final int    ERROR_NORM               = 1001;
    public static final int    ERROR_SESSION_EXPIRED    = 1008;

    // ASM monitor internal error codes 700-899 warnings, >=900 error
    public static final int    DECOMPRESS_ERROR_711         = 711;
    public static final int    DECODE_ERROR_712             = 712;
    public static final int    ERROR_900                    = 900;
    public static final int    INVALID_HANDLER_CHAIN_910    = 910;

    // result codes
    public static final int    RESULT_CONNECT_TIMEOUT       = 110;
    public static final int    RESULT_OK                    = 200;
    public static final int    RESULT_NOT_FOUND             = 404;
    public static final int    RESULT_EXECUTION_TIMEOUT     = 1042;
    public static final int    RESULT_PAGE_LOAD_TIMEOUT     = 1043;
    public static final int    RESULT_BANDWITH_EXCEEDED     = 1060;
    public static final int    RESULT_DOMAIN_REDIRECT       = 6001;
    public static final int    RESULT_PAGE_ELEMENT_404      = 6404;
    public static final int    RESULT_CONNECTION_TERMINATED = 6007;
    public static final int    RESULT_URL_CANNOT_BE_SHOWN   = 6101;
    public static final int    RESULT_RESPONSE_ASSERTION    = 7001;
    public static final int    RESULT_OPERATION_TIMEOUT     = 7011;
    public static final int    RESULT_ASSERTION_NOT_MATCHED = 9501;
    public static final int    RESULT_NO_CHECKPOINT_AVAILABLE = -93;
    // status codes
    public static final int    STATUS_CODE_OK               = 1;
    public static final int    STATUS_CODE_ASSERTION_ERROR  = 3;
    public static final int    RESULT_HTTP_CLIENT_ERROR     = 400;

    // retries
    public static final int    INIT_RETRIES             = 10;
    public static final int    THREAD_RETRIES           = 10;

    public static final int    BUFFER_WAIT_TIME         = 60000;

    public static final String JAVA_NET_EXCEPTION_REGEX =
            ".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException"
                    + "|ProtocolException|SocketException|SocketTimeoutException"
                    + "|UnknownHostException|IOException).*";

    // JSON constants
    public static final String JSON_PATTERN             = "\\p{InCombiningDiacriticalMarks}+";

    // Crypto constants
    public static final String kAlgorithm               = "PBEWithMD5AndDES";
    public static final String MESSAGE_DIGEST           = "1D0NTF33LT4RDY";

    // metric categories
    public static final String METRIC_TREE              = "App Synthetic Monitor";
    public static final String CREDITS_CATEGORY         = "Credits";
    public static final String LOG_CATEGORY             = "Log";
    public static final String PSP_CATEGORY             = "Public Stats";
    public static final String STATS_CATEGORY           = "Stats";
    public static final String MONITOR_METRIC_PREFIX    = "Monitors|";
    public static final String STATUS_METRIC_PREFIX     = "Status Monitoring|";
    public static final String METRIC_PATH_SEPARATOR    = "|";
    public static final String METRIC_NAME_SEPARATOR    = ":";
    public static final String OPMS                     = "OPMS";

    // API commands
    public static final String HTTP_POST                = "POST";
    public static final String LOGIN_CMD                = "acct_token";
    public static final String LOGOUT_CMD               = "acct_logout";
    public static final String CREDITS_CMD              = "acct_credits";
    public static final String STATIONS_GET_CMD         = "cp_list";
    public static final String FOLDER_CMD               = "fldr_get";
    public static final String MONITOR_GET_CMD          = "rule_get";
    public static final String PSP_CMD                  = "rule_psp";
    public static final String STATS_CMD                = "rule_stats";
    public static final String LOGS_CMD                 = "rule_log";

    // command parameters
    public static final String NKEY_PARAM               = "nkey=";
    public static final String CALLBACK_PARAM           = "&callback=";
    public static final String DO_CALLBACK              = "doCallback";
    public static final String FOLDER_PARAM             = "&folder=";
    public static final String NAME_PARAM               = "&name=";
    public static final String REVERSE_PARAM            = "&reverse=y";
    public static final String NOT_AGGREGATE_PARAM      = "&aggregate=n";
    public static final String AGGREGATE_PARAM          = "&aggregate=y";
    public static final String NUM_PARAM                = "&num=";
    public static final String UUID_PARAM               = "&uuid=";
    public static final String START_DATE_PARAM         = "&start_date=";
    public static final String END_DATE_PARAM           = "&end_date=";
    public static final String ACCOUNT_PARAM            = "&acct=";
    public static final String FULL_PARAM               = "&full=";
    public static final String NEW_OUTPUT_PARAM         = "&legacy_output=n";

    // response tags
    public static final String XML_PREFIX               = "<?xml";
    public static final String UUID_TAG                 = "uuid";
    public static final String ACTIVE_TAG               = "active";
    public static final String AVAILABLE_TAG            = "available";
    public static final String AREA_TAG                 = "areas";
    public static final String COUNTRY_TAG              = "country_name";
    public static final String CITY_TAG                 = "city";
    public static final String CODE_TAG                 = "code";
    public static final String CHECKPOINTS_TAG          = "checkpoints";
    public static final String CREDITS_TAG              = "credits";
    public static final String GMT_OFFSET_TAG           = "gmtoffset";
    public static final String TIMEZONE_TAG             = "tz";
    public static final String RESULT_TAG               = "result";
    public static final String NKEY_TAG                 = "nkey";
    public static final String ERROR_TAG                = "error";
    public static final String ERRORS_TAG               = "errors";
    public static final String FOLDER_TAG               = "folder";
    public static final String FOLDERS_TAG              = "folders";
    public static final String RULES_TAG                = "rules";
    public static final String INFO_TAG                 = "info";
    public static final String NAME_TAG                 = "name";
    public static final String DESCR_TAG                = "descr";
    public static final String LOCATION_TAG             = "loc";
    public static final String MONITORS_TAG             = "monitors";
    public static final String STATS_TAG                = "stats";
    public static final String COLOR_TAG                = "color";
    public static final String COLORS_TAG               = "colors";
    public static final String ELAPSED_TAG              = "elapsed";
    public static final String TYPE_TAG                 = "type";
    public static final String HOST_TAG                 = "host";
    public static final String PORT_TAG                 = "port";
    public static final String PATH_TAG                 = "path";
    public static final String VERSION_TAG              = "version";
    public static final String CURSOR_TAG               = "cursor";
    public static final String OUTPUT_TAG               = "output";
    public static final String TAGS_TAG                 = "tags";
    public static final String IPADDRESS_TAG            = "ipaddr";
    public static final String HAR_OR_LOG_TAG           = "{\"har\""; //"{\"har\": {\"log\"";
    public static final String UNDEFINED                = "Undefined";
    public static final String HTTP_PORT                = "80";
    public static final String HTTPS_PORT               = "443";
    public static final String DATABASE_ERROR           = "Database error";

    // monitor types
    public static final String HTTP_MONITOR             = "http";
    public static final String HTTPS_MONITOR            = "https";
    public static final String FTP_MONITOR              = "ftp";
    public static final String DNS_MONITOR              = "dns";
    public static final String SCRIPT_MONITOR           = "script";
    public static final String FULL_PAGE_MONITOR        = "browser";
    public static final String REAL_BROWSER_MONITOR     = "script_firefox";

    // JMeter log result tags
    public static final String TEST_RESULTS             = "testResults";
    public static final String TIMESTAMP_TAG            = "ts";
    public static final String TOTAL_TIME_TAG           = "t";
    public static final String RESOLVE_TIME_TAG         = "rt";
    public static final String PROCESSING_TIME_TAG      = "pt";
    public static final String SIZE_IN_BYTES_TAG        = "by";
    public static final String SENT_BYTES_TAG           = "sby";
    public static final String RESPONSE_CODE_TAG        = "rc";
    public static final String RESPONSE_MESSAGE_TAG     = "rm";
    public static final String SUCCESS_FLAG_TAG         = "s";
    public static final String ERROR_COUNT_TAG          = "ec";
    public static final String SAMPLE_COUNT_TAG         = "sc";
    public static final String TEST_URL_TAG             = "lb";
    public static final String UNDEFINED_ASSERTION      = "Undefined Assertion";
    public static final String ASSERTION_RESULT         = "assertionResult";
    public static final String HTTP_SAMPLE              = "httpSample";
    public static final String SAMPLE                   = "sample";
    public static final String JAVA_NET_URL             = "java.net.URL";
    public static final String FAILURE_TAG              = "failure";
    public static final String FAILURE__MESSAGE_TAG     = "failureMessage";
    public static final String STEP                     = "Step ";
    public static final String ASSERTION_FAILURE        = "Assertion Failure";
    public static final String ASSERTION_ERROR          = "Assertion Error";
    public static final String RESPONSE_CODE_NON_HTTP   = "Non HTTP response code";
    public static final String RESPONSE_CODE_EXCEPTION  = "Exception";
    public static final String RESPONSE_MESSAGE_NON_HTTP    = "Non HTTP response message";
    public static final String RESPONSE_MESSAGE_TIMEOUT = "timed out";

    // JMeter metrics
    public static final String STATUS_MESSAGE           = "Status Message";
    public static final String STATUS_MESSAGE_VALUE     = "Status Message Value";
    public static final String RESPONSE_CODE            = "Response Code";
    public static final String RESULT_CODE              = "Result Code";
    public static final String RESPONSE_MESSAGE         = "Response Message";
    public static final String SUCCESS                  = "Success";
    public static final String ERROR_COUNT              = "Error Count";
    public static final String SAMPLE_COUNT             = "Sample Count";
    public static final String TIMESTAMP                = "Timestamp";
    public static final String TOTAL_TIME               = "Total Time (ms)";
    public static final String RESOLVE_TIME             = "Resolve Time (ms)";
    public static final String PROCESSING_TIME          = "Processing Time (ms)";
    public static final String SIZE_IN_BYTES            = "Size In Bytes (B)";
    public static final String SENT_BYTES               = "Sent Bytes (B)";
    public static final String ASSERTION_FAILURES       = "Assertion Failures";
    public static final String ASSERTION_ERRORS         = "Assertion Errors";
    public static final String TEST_URL                 = "URL";
    public static final String TEST_LABEL               = "Label";
    public static final String ASSERTION_NAME           = "Assertion Name";
    public static final String ASSERTION_FAILURE_MSG    = "Assertion Failure Message";
    
    // RBM Assertion metrics
    public static final String ASSERTION_MESSAGE         = "Assertion Message";
    
    //  status indicator colors, see AsmPropertiesImpl.APM_CM_COLORS
    public static final String GREEN                    = "green";
    public static final String YELLOW                   = "yellow";
    public static final String ORANGE                   = "orange";
    public static final String RED                      = "red";
    
    // target metric names for mapping
    public static final String METRIC_NAME_PROBE_ERRORS = "Probe Errors";
    public static final String METRIC_NAME_PROBES       = "Probes";
    public static final String METRIC_NAME_CHECK_ERRORS = "Check Errors";
    public static final String METRIC_NAME_CHECKS       = "Checks";
    public static final String METRIC_NAME_REPEAT       = "Repeat";
    public static final String METRIC_NAME_CONSECUTIVE_ERRORS  = "Consecutive Errors";
    public static final String METRIC_NAME_ERRORS_PER_INTERVAL = "Errors Per Interval";
    public static final String METRIC_NAME_ALERTS_PER_INTERVAL = "Alerts Per Interval";
    public static final String METRIC_NAME_DATA_RECEIVED       = "Data Received";
    
    public static final String METRIC_NAME_LOAD_TIME            = "Load Time";
    public static final String METRIC_NAME_CONTENT_LOAD_TIME    = "Content Load Time";
    public static final String METRIC_NAME_TOTAL_TIME           = "Total Time";
    public static final String METRIC_NAME_RESPONSE_HEADER_SIZE = "Response Header Size";
    public static final String METRIC_NAME_RESPONSE_BODY_SIZE   = "Response Body Size";
    public static final String METRIC_NAME_RECEIVE_TIME         = "Receive Time";
    public static final String METRIC_NAME_SEND_TIME            = "Send Time";
    public static final String METRIC_NAME_CONNECT_TIME         = "Connect Time";
    public static final String METRIC_NAME_DNS_TIME             = "DNS Time";
    public static final String METRIC_NAME_BLOCKED_TIME         = "Blocked Time";
    public static final String METRIC_NAME_WAIT_TIME            = "Wait Time";
        
    //types of probes in rule_log
    public static final int PROBE_TYPE_NORMAL = 0;
    public static final int PROBE_TYPE_EXTRA = 1;
    public static final int PROBE_TYPE_SECOND_OPINION = 2;
    public static final int PROBE_TYPE_MAINTENANCE = 3;
    public static final int PROBE_TYPE_FINAL = 4;
    public static final int PROBE_TYPE_INTERNAL_ERROR_OPMS = 5;
    public static final int PROBE_TYPE_INTERNAL_ERROR_PUBLIC = 6;
    
    //results of probes in rule_log
    public static final int PROBE_RESULT_OK = 0;
}
