package com.ca.apm.swat.epaplugins.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localizable log messages for App Synthetic Monitor EPA plugin.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmMessages {

    public static final String BUNDLE_NAME = "com.ca.apm.swat.epaplugins.utils.messages";
    
    public static final String INITIALIZATION_ERROR         = "initializationError";
    public static final String RUN_ERROR                    = "runError";
    public static final String READING_PROPERTIES           = "readingProperties";
    public static final String READING_PROPERTIES_ERROR     = "readingPropertiesError";
    public static final String READING_PROPERTIES_FINISHED  = "readingPropertiesFinished";
    public static final String FOLDER_THREAD_TIMEOUT        = "folderThreadTimeout";
    public static final String FOLDER_THREAD_ERROR          = "folderThreadError";
    public static final String CONNECTION_ERROR             = "connectionError";
    public static final String CONNECTION_RETRY             = "connectionRetry";
    public static final String CONNECTION_RETRY_ERROR       = "connectionRetryError";
    public static final String CONNECTED                    = "connected";
    public static final String LOGIN_ERROR                  = "loginError";
    public static final String LOGIN_INFO                   = "loginInfo";
    public static final String DECRYPT_ERROR                = "decryptError";
    public static final String DECRYPT_INFO                 = "decryptInfo";
    public static final String NO_ERROR                     = "noError";
    public static final String NO_INFO                      = "noInfo";
    public static final String PARENT_THREAD                = "parentThread";
    public static final String AGENT_INITIALIZATION         = "agentInitialization";
    public static final String PUT_PW_IN_PROPERTIES         = "putInProperties";
    public static final String HTTP_REQUEST                 = "httpRequest";
    public static final String HTTP_RESPONSE                = "httpResponse";
    public static final String TEXT_NORMALIZER_NOT_FOUND    = "textNormalizerNotFound";
    public static final String THREAD_STARTED               = "threadStarted";
    public static final String SKIP_FOLDER                  = "skipFolder";
    public static final String SKIP_MONITOR                 = "skipMonitor";
        
    
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
