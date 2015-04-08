package com.ca.apm.swat.epaplugins.asm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ca.apm.swat.epaplugins.asm.error.LoginError;

public abstract class Accessor {

    public static final String JSON_REGEX = "doCallback\\((.*)\\)([\n]*)";
    private static final Pattern unpad = Pattern.compile(JSON_REGEX);

    /**
     * Execute a call against the App Synthetic Monitor API.
     * @param callType API call
     * @param callParams parameters
     * @return unpadded API call result
     * @throws Exception if an error occurred,
     *     e.g. an error code like 1000 (authentication error) or
     *     1001 (call syntax error) was returned by the API call
     */
    public abstract String executeApi(String callType, String callParams)
        throws Exception;

    /**
     * Login to the App Synthetic Monitor API.
     * @return the token returned or {@link FAILED}
     * @throws LoginError if the authentication fails
     * @throws Exception if another error occurred
     */
    public abstract String login() throws LoginError, Exception;

    /**
     * Remove padding from JSON string. 
     * @param jsonWithPadding JSON string with padding
     * @return JSON string without padding
     */
    protected static String unpadJson(String jsonWithPadding) {
        Matcher matcher = unpad.matcher(jsonWithPadding);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}