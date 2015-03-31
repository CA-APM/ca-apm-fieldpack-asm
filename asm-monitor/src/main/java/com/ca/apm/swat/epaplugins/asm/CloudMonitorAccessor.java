package com.ca.apm.swat.epaplugins.asm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.CryptoUtils;
import com.ca.apm.swat.epaplugins.utils.RestClient;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Access the App Synthetic Monitor API.
 * 
 */
public class CloudMonitorAccessor implements AsmProperties {

    private boolean apmcmLocalTest;
    private Properties apmcmProperties;
    private RestClient apmcmClient;
    public static final CryptoUtils apmcmCrypto = new CryptoUtils(MESSAGE_DIGEST);
    private String apmcmLocalTestPath = "";

    public static final String FAILED = "Failed";
    public static final String LOGGED_OUT = "Logged Out.";

    /**
     * Access the App Synthetic Monitor API.
     * @param apmcmProperties properties
     */
    public CloudMonitorAccessor(Properties apmcmProperties) {
        this.apmcmProperties = apmcmProperties;

        String proxyHost = this.apmcmProperties.getProperty(PROXY_HOST, "");
        String proxyPort = this.apmcmProperties.getProperty(PROXY_PORT, "");
        String proxyUser = this.apmcmProperties.getProperty(PROXY_USER, "");
        String proxyPass;
        if (this.apmcmProperties.getProperty(PROXY_PASS_ENCRYPTED, FALSE).equals(TRUE)) {
            proxyPass = CloudMonitorAccessor.apmcmCrypto.decrypt(
                this.apmcmProperties.getProperty(PROXY_PASS, ""));
        } else {
            proxyPass = this.apmcmProperties.getProperty(PROXY_PASS, "");
        }

        this.apmcmClient = new RestClient(proxyHost, proxyPort, proxyUser, proxyPass);

        this.apmcmLocalTest =
                Boolean.parseBoolean(this.apmcmProperties.getProperty(LOCAL_TEST, FALSE));
        if (this.apmcmLocalTest) {
            this.apmcmLocalTestPath = this.apmcmProperties.getProperty(LOCAL_TEST);
        }
    }

    /**
     * Execute a call against the App Synthetic Monitor API.
     * @param callType API call
     * @param callParams parameters
     * @return API call result
     * @throws Exception errors
     */
    public String executeApi(String callType, String callParams) throws Exception {
        String apiResponse = "";
        if (!apmcmLocalTest) {
            URL apiUrl = new URL(this.apmcmProperties.getProperty(URL) + "/" + callType);
            apiResponse = this.apmcmClient.request(apmcmMethod, apiUrl,
                callParams);
        } else if (!callType.equals(kAPMCMLogoutCmd)) {
            String inputLine = null;
            String inputFileName = this.apmcmLocalTestPath + "\\" + callType + ".txt";
            BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
            while (null != (inputLine = inputFile.readLine())) {
                apiResponse = apiResponse + inputLine;
            }
            inputFile.close();
        } else {
            return LOGGED_OUT;
        }

        return apiResponse.trim();
    }

    /**
     * Login to the App Synthetic Monitor API.
     * @return the token returned or {@link FAILED}
     * @throws Exception errors
     */
    public String login() throws Exception {
        String apmcmUser = this.apmcmProperties.getProperty(USER);
        String apmcmPass = null;
        if (this.apmcmProperties.getProperty(PASSWORD_ENCRYPTED).equals(TRUE)) {
            apmcmPass = CloudMonitorAccessor.apmcmCrypto.decrypt(
                this.apmcmProperties.getProperty(PASSWORD));
        } else {
            apmcmPass = this.apmcmProperties.getProperty(PASSWORD);
        }

        String loginStr = "user=" + apmcmUser + "&password=" + apmcmPass + "&callback="
                + apmcmCallback;
        String loginRequest = executeApi(kAPMCMLoginCmd, loginStr);
        JSONObject entireJsonObject = new JSONObject(unpadJson(loginRequest));

        if (entireJsonObject.getInt(kAPMCMCode) == 0) {
            JSONObject resultJsonObject = entireJsonObject.optJSONObject(kAPMCMResult);
            if (resultJsonObject != null) {
                return resultJsonObject.optString(kAPMCMNKey, null);
            }
            return FAILED;
        }

        String errorStr = entireJsonObject.optString(kAPMCMError,
            AsmMessages.getMessage(AsmMessages.NO_ERROR));
        int errorCode = entireJsonObject.optInt(kAPMCMCode, -1);
        String errorInfo = entireJsonObject.optString(kAPMCMInfo,
            AsmMessages.getMessage(AsmMessages.NO_INFO));

        EpaUtils.getFeedback().error(AsmMessages.getMessage(AsmMessages.LOGIN_ERROR,
            errorStr, errorCode, errorInfo));

        if (errorCode == 1000) {
            System.err.print(AsmMessages.getMessage(AsmMessages.LOGIN_INFO,
                this.apmcmProperties.getProperty(URL),
                kAPMCMLoginCmd,
                APMCM_PRODUCT_NAME,
                apmcmPasswordPage));
        }

        System.exit(1000);

        return FAILED;
    }

    /**
     * Remove padding from JSON string. 
     * @param jsonWithPadding JSON string with padding
     * @return JSON string without padding
     * @throws Exception error
     */
    private String unpadJson(String jsonWithPadding) throws Exception {
        String patternToMatch = kJsonRegex;

        Pattern unpad = Pattern.compile(patternToMatch);
        Matcher matched = unpad.matcher(jsonWithPadding);

        if (matched.find()) {
            return matched.group(1);
        }
        return null;
    }

}
