package com.ca.apm.swat.epaplugins.utils;

/**
 * Contains all constants for CA App Synthetic Monitor EPA plugin.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface AsmProperties {

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String EMPTY_STRING = "";
    public static final String ZERO = "0";
    public static final String ONE = "1"; 
    public static final String YES = "y";
    public static final String NO = "n";
    public static final String NO_TYPE = "no type";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String UTF8 = "UTF-8";

    public static final String ASM_PRODUCT_NAME       = "App Synthetic Monitor";
    public static final String ASM_PRODUCT_NAME_SHORT = "ASM";

    public static final String PROPERTY_FILE_NAME     = "AppSyntheticMonitor.properties";

    public static final String DEFAULT_LOCALE         = "en_US";
    public static final String LOCALE                 = "asm.locale";
    public static final String WAIT_TIME              = "asm.waittime";
    public static final String DISPLAY_CHECKPOINTS    = "asm.displaycheckpoints";

    public static final String METRICS_STATS_FOLDER   = "asm.metrics.stats.folder";
    public static final String METRICS_CREDITS        = "asm.metrics.credits";
    public static final String METRICS_PUBLIC         = "asm.metrics.public";
    public static final String METRICS_LOGS           = "asm.metrics.logs";
    public static final String METRICS_STATS_RULE     = "asm.metrics.stats.rule";

    public static final String PROXY_HOST             = "asm.proxy.host";
    public static final String PROXY_PORT             = "asm.proxy.port";
    public static final String PROXY_USER             = "asm.proxy.user";
    public static final String PROXY_PASS_ENCRYPTED   = "asm.proxy.pass.encrypted";
    public static final String PROXY_PASS             = "asm.proxy.pass";

    public static final String LOCAL_TEST             = "asm.localtest";
    public static final String LOCAL_TEST_PATH        = "asm.localtestpath";

    public static final String URL                    = "asm.URL";
    public static final String USER                   = "asm.userEmail";
    public static final String PASSWORD               = "asm.APIPassword";
    public static final String PASSWORD_ENCRYPTED     = "asm.APIPasswordEncrypted";
    public static final String FOLDERS                = "asm.folders";
    public static final String SKIP_INACTIVE_FOLDERS  = "asm.skip_inactive.folders";
    public static final String FOLDER_PREFIX          = "asm.folder.";
    public static final String NUM_LOGS               = "asm.numlogs";

    // error codes
    public static final int AUTH_ERROR_CODE = 1000;
    public static final int NORM_ERROR_CODE = 1001;

    // retries
    public static final int INIT_RETRIES = 10;
    public static final int THREAD_RETRIES = 10;

    public static final String kDefaultDelimiter = ",";
    public static final String[] kNoStringArrayProperties = new String[0];

    public static final int kBufferWaitTime = 60000;
    public static final String kJavaNetExceptionRegex =
            ".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|"
            + "ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*";
    public static final String kJsonRegex = "doCallback\\((.*)\\)([\n]*)";
    public static final String kJsonPattern = "\\p{InCombiningDiacriticalMarks}+";
    public static final String kAlgorithm = "PBEWithMD5AndDES";
    public static final String kXMLPrefix = "<?xml";
    public static final String kCreditsCategory = "Credits";
    public static final String kLogCategory = "Log";
    public static final String kPSPCategory = "Public Stats";
    public static final String kStatsCategory = "Stats";
    public static final String MESSAGE_DIGEST = "1D0NTF33LT4RDY";
    public static final String METRIC_PATH_SEPARATOR = "|";
    public static final String METRIC_NAME_SEPARATOR = ":";

    public static final String kAPMCMPSPCmd = "rule_psp";
    public static final String kAPMCMStatsCmd = "rule_stats";
    public static final String kAPMCMLoginCmd = "acct_token";
    public static final String kAPMCMLogoutCmd = "acct_logout";
    public static final String kAPMCMLogsCmd = "rule_log";
    public static final String kAPMCMCheckptsCmd = "cp_list";
    public static final String kAPMCMFoldersCmd = "fldr_get";
    public static final String kAPMCMRuleCmd = "rule_get";
    public static final String kAPMCMCreditsCmd = "acct_credits";

    public static final String kAPMCMNKeyParam = "nkey=";
    public static final String kAPMCMCallbackParam = "&callback=";
    public static final String kAPMCMFolderParam = "&folder=";
    public static final String kAPMCMNameParam = "&name=";
    public static final String kAPMCMReverseParam = "&reverse=y";
    public static final String kAPMCMNumParam = "&num=";
    public static final String kAPMCMStartDateParam = "&start_date=";
    public static final String kAPMCMAccountParam = "&acct=";
    public static final String kAPMCMFullParam = "&full=y";

    public static final String kAPMCMActive = "active";
    public static final String kAPMCMAreas = "areas";
    public static final String kAPMCMCountry = "country_name";
    public static final String kAPMCMCity = "city";
    public static final String kAPMCMCode = "code";
    public static final String kAPMCMCheckpoints = "checkpoints";
    public static final String kAPMCMCredits = "credits";
    public static final String kAPMCMResult = "result";
    public static final String kAPMCMNKey = "nkey";
    public static final String kAPMCMError = "error";
    public static final String kAPMCMErrors = "errors";
    public static final String kAPMCMFolder = "folder";
    public static final String kAPMCMFolders = "folders";
    public static final String kAPMCMRules = "rules";
    public static final String kAPMCMInfo = "info";
    public static final String kAPMCMName = "name";
    public static final String kAPMCMDescr = "descr";
    public static final String kAPMCMLoc = "loc";
    public static final String kAPMCMMonitors = "monitors";
    public static final String kAPMCMStats = "stats";
    public static final String kAPMCMColor = "color";
    public static final String kAPMCMColors = "colors";
    public static final String kAPMCMElapsed = "elapsed";
    public static final String kAPMCMType = "type";
    public static final String kAPMCMVersion = "version";
    public static final String kAPMCMOutput = "output";
    public static final String kAPMCMHarOrLog = "{\"har\": {\"log\"";
    public static final String kAPMCMUndefined = "Undefined";
    public static final String METRIC_TREE = "App Synthetic Monitor";
    public static final String MONITOR_METRIC_PREFIX = "Monitors|";
    public static final String STATUS_METRIC_PREFIX = "Status Monitoring|";
    public static final String ROOT_FOLDER = "root_folder";
    public static final String ALL_FOLDERS = "all_folders";
    public static final String ALL_RULES = "all_rules";

    public static final String apmcmCallback = "doCallback";
    public static final String apmcmMethod = "POST";
    public static final String apmcmPasswordPage = "https://dashboard.cloudmonitor.ca.com/en/change_passwd.php";

    // JMeter log result tags
    public static final String TEST_RESULTS         = "testResults";
    public static final String RESPONSE_CODE_TAG    = "rc";
    public static final String RESPONSE_MESSAGE_TAG = "rm";
    public static final String SUCCESS_FLAG_TAG     = "s";
    public static final String ERROR_COUNT_TAG      = "ec";
    public static final String TEST_URL_TAG         = "lb";
    public static final String UNDEFINED_ASSERTION  = "Undefined Assertion";
    public static final String ASSERTION_RESULT     = "assertionResult";
    public static final String FAILURE              = "failure";
    public static final String STEP                 = "Step ";
    public static final String ASSERTION_FAILURE    = " - Assertion Failure";
    public static final String ASSERTION_ERROR      = " - Assertion Error";

    // JMeter metrics
    public static final String STATUS_MESSAGE       = "Status Message";
    public static final String STATUS_MESSAGE_VALUE = "Status Message Value";
    public static final String RESPONSE_CODE        = "Response Code";
    public static final String ERROR_COUNT          = "Error Count";
    public static final String ASSERTION_FAILURES   = "Assertion Failures";
    public static final String ASSERTION_ERRORS     = "Assertion Errors";
    public static final String TEST_URL             = "URL";
   
    //  status indicator colors, see AsmPropertiesImpl.APM_CM_COLORS
    public static final String GREEN                = "green";
    public static final String YELLOW               = "yellow";
    public static final String ORANGE               = "orange";
    public static final String RED                  = "red";
}
