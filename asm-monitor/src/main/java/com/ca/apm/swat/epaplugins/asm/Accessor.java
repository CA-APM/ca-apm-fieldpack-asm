package com.ca.apm.swat.epaplugins.asm;

import com.ca.apm.swat.epaplugins.asm.error.LoginError;

public interface Accessor {

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

}