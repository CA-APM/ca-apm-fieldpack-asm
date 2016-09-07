package com.ca.apm.swat.epaplugins.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localizable log messages for App Synthetic Monitor EPA plugin.
 * All INFO, WARN and ERROR messages should only use this class and 
 * message.properties for log messages.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmMessages {

    public static final String BUNDLE_NAME = "com.ca.apm.swat.epaplugins.utils.messages";
    
    //debug messages
    public static final String DUPLICATE_METRIC_200         = "200_duplicateMetric";
    
    // verbose messages
    public static final String READING_PROPERTIES_300       = "300_readingProperties";
    public static final String GET_FOLDER_DATA_301          = "301_getFolderData";
    public static final String GET_STATS_DATA_302           = "302_getStatsData";
    public static final String GET_NO_STATS_DATA_303        = "303_getNoStatsData";
    public static final String GET_FOLDER_METRICS_304       = "304_getFolderMetrics";
    public static final String SKIP_FOLDER_305              = "305_skipFolder";
    public static final String METHOD_FOR_FOLDER_306        = "306_methodForFolder";
    public static final String READ_MONITOR_307             = "307_readingMonitor";
    public static final String SKIP_MONITOR_308             = "308_skipMonitor";
    public static final String METHOD_FOR_FOLDER_MONITOR_309    = "309_methodForFolderMonitor";
    public static final String HTTP_REQUEST_310             = "310_httpRequest";
    public static final String HTTP_RESPONSE_311            = "311_httpResponse";
    public static final String THREAD_STARTED_312           = "312_threadStarted";
    
    // info messages
    public static final String READING_PROPERTIES_FINISHED_500  = "500_readingPropertiesFinished";
    public static final String CONNECTION_RETRY_501         = "501_connectionRetry";
    public static final String API_CALL_STATS_502           = "502_apiCallStats";
    public static final String CONNECTED_503                = "503_connected";
    public static final String PUT_PW_IN_PROPERTIES_504     = "504_putInProperties";
    public static final String READ_CONFIGURATION_505       = "505_readConfiguration";
    public static final String PROPERTY_FILE_CHANGED_506    = "506_propertyFileChanged";
    public static final String CONFIG_POLLING_STARTED_507   = "507_configPollingStarted";   

    // warning messages
    public static final String NON_INT_PROPERTY_WARN_700    = "700_nonIntegerWarningDefault";
    public static final String NON_INT_PROPERTY_WARN_701    = "701_nonIntegerWarningIgnore";
    public static final String OUTPUT_HANDLE_WARN_702       = "702_outputHandleWarning";
    public static final String METRIC_NULL_WARN_703         = "703_metricNullWarning";
    public static final String METRIC_READ_WARN_704         = "704_metricReadWarning";
    public static final String OUTPUT_EMPTY_WARN_705        = "705_outputEmptyWarning";
    public static final String GENERATE_METRICS_ERROR_710   = "710_generateMetricsError";
    public static final String DECOMPRESS_ERROR_711         = "711_decompressError";
    public static final String DECODE_ERROR_712             = "712_decodeError";
    public static final String JSON_PARSING_ERROR_713       = "713_jsonParsingError";
    public static final String ASSET_DOWNLOAD_ERROR_714     = "714_assetDownloadError";

    //error messages
    public static final String INITIALIZATION_ERROR_900     = "900_initializationError";
    public static final String READING_PROPERTIES_ERROR_901 = "901_readingPropertiesError";
    public static final String CONNECTION_ERROR_902         = "902_connectionError";
    public static final String CONNECTION_RETRY_ERROR_903   = "903_connectionRetryError";
    public static final String RUN_ERROR_904                = "904_runError";
    public static final String FOLDER_THREAD_TIMEOUT_905    = "905_folderThreadTimeout";
    public static final String FOLDER_THREAD_ERROR_906      = "906_folderThreadError";
    public static final String LOGIN_ERROR_907              = "907_loginError";
    public static final String LOGIN_INFO_908               = "908_loginInfo";
    public static final String BYTES_DECODED_NULL_909       = "909_bytesDecodedNull";
    public static final String INVALID_HANDLER_CHAIN_910    = "910_invalidHandlerChain";
    public static final String METRIC_WRITE_ERROR_913       = "913_metricWriteError";
    public static final String API_ERROR_914                = "914_apiError";
    public static final String DECRYPT_ERROR_915            = "915_decryptError";
    public static final String NO_ERROR_916                 = "916_noError";
    public static final String NO_INFO_917                  = "917_noInfo";
    public static final String TEXT_NORMALIZER_NOT_FOUND_918    = "918_textNormalizerNotFound";
    public static final String NULL_RESPONSE_919            = "919_nullResponse";
    public static final String API_ERROR_920                = "920_apiError";
    public static final String PROPERTY_FILE_NOT_FOUND_921  = "921_propertyFileNotFound";
    public static final String METRIC_SHUT_OFF_ERROR_922    = "922_metricShutOffError";
    public static final String METRIC_TURN_ON_ERROR_923     = "923_metricTurnOnError";
    
    
    public static final String PARENT_THREAD                = "parentThread";
    public static final String AGENT_INITIALIZATION         = "agentInitialization";

    
    /**
     * The resource bundle.
     */
    private static ResourceBundle messages = null;

    /**
     * The locale.
     */
    private static Locale locale = null;

    /**
     * Set the locale.
     * @param loc new locale
     */
    public static void setLocale(Locale loc) {
        locale = loc;
    }

    /**
     * Get all messages.
     * @return all messages
     */
    public static ResourceBundle getMessages() {
        if (null == messages) {
            if (null == locale) {
                messages = ResourceBundle.getBundle(BUNDLE_NAME);
            } else {
                messages = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            }
        }

        return messages;
    }

    /**
     * Get a message.
     * @param key message key
     * @return message string
     */
    public static String getMessage(String key) {
        return getMessages().getString(key);
    }

    /**
     * Get a formatted messages.
     * @param key message key
     * @param params message parameters
     * @return formatted message
     */
    public static String getMessage(String key, Object... params) {
        MessageFormat formatter = null;
        
        if (null == locale) {
            formatter = new MessageFormat(getMessages().getString(key));
        } else {
            formatter = new MessageFormat(getMessages().getString(key), locale);
        }
        
        return formatter.format(params);
    }
}
