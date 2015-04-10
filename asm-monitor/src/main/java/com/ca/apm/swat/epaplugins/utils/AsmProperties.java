package com.ca.apm.swat.epaplugins.utils;

/**
 * Contains all constants for CA App Synthetic Monitor EPA plugin.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface AsmProperties {

    // constants
    public static final String TRUE                     = "true";
    public static final String FALSE                    = "false";
    public static final String EMPTY_STRING             = "";
    public static final String[] EMPTY_STRING_ARRAY     = new String[0];
    public static final String ZERO                     = "0";
    public static final String ONE                      = "1"; 
    public static final String YES                      = "y";
    public static final String NO                       = "n";
    public static final String NO_TYPE                  = "no type";
    public static final String DATE_FORMAT              = "yyyy-MM-dd";
    public static final String DEFAULT_DELIMITER        = ",";
    public static final String BLANK                    = " ";

    public static final String ASM_PRODUCT_NAME         = "App Synthetic Monitor";
    public static final String ASM_PRODUCT_NAME_SHORT   = "ASM";

    public static final String PROPERTY_FILE_NAME       = "AppSyntheticMonitor.properties";
    public static final String PASSWORD_URL             =
            "https://dashboard.cloudmonitor.ca.com/en/change_passwd.php";

    public static final String LOCALE                   = "asm.locale";
    public static final String DEFAULT_LOCALE           = "en_US";
    public static final String ENCODING                 = "asm.encoding";
    public static final String WAIT_TIME                = "asm.waittime";
    public static final String DISPLAY_STATIONS         = "asm.displayMonitoringStations";

    // which metrics to get and display
    public static final String METRICS_STATS_FOLDER     = "asm.metrics.stats.folder";
    public static final String METRICS_STATS_MONITOR    = "asm.metrics.stats.monitor";
    public static final String METRICS_CREDITS          = "asm.metrics.credits";
    public static final String METRICS_PUBLIC           = "asm.metrics.public";
    public static final String METRICS_LOGS             = "asm.metrics.logs";

    public static final String USE_PROXY                = "asm.useProxy";
    public static final String PROXY_HOST               = "asm.proxyHost";
    public static final String PROXY_PORT               = "asm.proxyPort";
    public static final String PROXY_USER               = "asm.proxyUser";
    public static final String PROXY_PASSWORD_ENCRYPTED = "asm.proxyPasswordEncrypted";
    public static final String PROXY_PASSWORD           = "asm.proxyPassword";

    public static final String LOCAL_TEST               = "asm.localtest";
    public static final String LOCAL_TEST_PATH          = "asm.localtestpath";

    public static final String URL                      = "asm.URL";
    public static final String USER                     = "asm.userEmail";
    public static final String PASSWORD                 = "asm.APIPassword";
    public static final String PASSWORD_ENCRYPTED       = "asm.APIPasswordEncrypted";
    public static final String INCLUDE_FOLDERS          = "asm.includeFolders";
    public static final String EXCLUDE_FOLDERS          = "asm.excludeFolders";
    public static final String SKIP_INACTIVE_FOLDERS    = "asm.skipInactiveFolders";
    public static final String SKIP_INACTIVE_MONITORS   = "asm.skipInactiveMonitors";
    public static final String FOLDER_PREFIX            = "asm.folder.";
    public static final String NUM_LOGS                 = "asm.numlogs";

    // folder and other constants in properties
    public static final String ROOT_FOLDER              = "root_folder";
    public static final String ALL_FOLDERS              = "all_folders";

    // TODO: not implemented yet!!!
    public static final String INCLUDE_TAGS             = "asm.includeTags";
    public static final String INCLUDE_MONITORS         = "asm.includeMonitors";
    public static final String RESULT_GROUPS            = "asm.resultsGroups";
    public static final String ALL_MONITORS             = "all_monitors";
    public static final String ALL_TAGS                 = "all_tags";
    public static final String GROUP_BY_FOLDER          = "by_folder";
    public static final String GROUP_BY_STATION         = "by_station";
    public static final String GROUP_MIXED              = "mixed";
    public static final String GROUP_NONE               = "no_groups";
    public static final String REPORT_JMETER_STEPS      = "asm.reportJMeterSteps";
    public static final String SUPPRESS_STEP_WITH_CODES = "asm.suppressStepsWithCodes";
    public static final String RESPONSE_CODES           = "asm.responseCodes";
    public static final String REPORT_STRING_RESULTS    = "asm.reportStringResults";
    public static final String REPORT_ASSERTION_FAILURES_AS = "asm.reportAssertionFailureAs";
    public static final String LOG_ASM_ISSUES           = "asm.logASMIssues=";

    // error codes
    public static final int    ERROR_OK                 = 0;
    public static final int    ERROR_AUTHORIZATION      = 1000;
    public static final int    ERROR_NORM               = 1001;
    public static final int    ERROR_SESSION_EXPIRED    = 1008;

    // result codes
    public static final int    RESULT_NOT_FOUND             = 404;
    public static final int    RESULT_EXECUTION_TIMEOUT     = 1042;
    public static final int    RESULT_PAGE_LOAD_TIMEOUT     = 1043;
    public static final int    RESULT_BANDWITH_EXCEEDED     = 1060;
    public static final int    RESULT_DOMAIN_REDIRECT       = 6001;
    public static final int    RESULT_PAGE_ELEMENT_404      = 6404;
    public static final int    RESULT_CONNECTION_TERMINATED = 6007;
    public static final int    RESULT_URL_CANNOT_BE_SHOWN   = 6101;
    public static final int    RESULT_RESPONSE_ASSERTION    = 7001;
    public static final int    RESULT_ASSERTION_NOT_MATCHED = 9501;

    // retries
    public static final int    INIT_RETRIES             = 10;
    public static final int    THREAD_RETRIES           = 10;

    public static final int    BUFFER_WAIT_TIME         = 60000;

    public static final String JAVA_NET_EXCEPTION_REGEX =
            ".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|"
            + "ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*";

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
    public static final String NUM_PARAM                = "&num=";
    public static final String START_DATE_PARAM         = "&start_date=";
    public static final String ACCOUNT_PARAM            = "&acct=";
    public static final String FULL_PARAM               = "&full=y";

    // response tags
    public static final String XML_PREFIX               = "<?xml";
    public static final String ACTIVE_TAG               = "active";
    public static final String AVAILABLE_TAG            = "available";
    public static final String AREA_TAG                 = "areas";
    public static final String COUNTRY_TAG              = "country_name";
    public static final String CITY_TAG                 = "city";
    public static final String CODE_TAG                 = "code";
    public static final String CHECKPOINTS_TAG          = "checkpoints";
    public static final String CREDITS_TAG              = "credits";
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
    public static final String VERSION_TAG              = "version";
    public static final String OUTPUT_TAG               = "output";
    public static final String TAGS_TAG                 = "tags";   
    public static final String HAR_OR_LOG_TAG           = "{\"har\""; //"{\"har\": {\"log\"";
    public static final String UNDEFINED                = "Undefined";

    // monitor types
    public static final String HTTP_MONITOR             = "http";
    public static final String HTTPS_MONITOR            = "https";
    public static final String SCRIPT_MONITOR           = "script";
    public static final String FULL_PAGE_MONITOR        = "browser";
    public static final String REAL_BROWSER_MONITOR     = "script_firefox";
    
    // JMeter log result tags
    public static final String TEST_RESULTS             = "testResults";
    public static final String RESPONSE_CODE_TAG        = "rc";
    public static final String RESPONSE_MESSAGE_TAG     = "rm";
    public static final String SUCCESS_FLAG_TAG         = "s";
    public static final String ERROR_COUNT_TAG          = "ec";
    public static final String TEST_URL_TAG             = "lb";
    public static final String UNDEFINED_ASSERTION      = "Undefined Assertion";
    public static final String ASSERTION_RESULT         = "assertionResult";
    public static final String FAILURE                  = "failure";
    public static final String STEP                     = "Step ";
    public static final String ASSERTION_FAILURE        = " - Assertion Failure";
    public static final String ASSERTION_ERROR          = " - Assertion Error";

    // JMeter metrics
    public static final String STATUS_MESSAGE           = "Status Message";
    public static final String STATUS_MESSAGE_VALUE     = "Status Message Value";
    public static final String RESPONSE_CODE            = "Response Code";
    public static final String ERROR_COUNT              = "Error Count";
    public static final String ASSERTION_FAILURES       = "Assertion Failures";
    public static final String ASSERTION_ERRORS         = "Assertion Errors";
    public static final String TEST_URL                 = "URL";
   
    //  status indicator colors, see AsmPropertiesImpl.APM_CM_COLORS
    public static final String GREEN                    = "green";
    public static final String YELLOW                   = "yellow";
    public static final String ORANGE                   = "orange";
    public static final String RED                      = "red";
}
