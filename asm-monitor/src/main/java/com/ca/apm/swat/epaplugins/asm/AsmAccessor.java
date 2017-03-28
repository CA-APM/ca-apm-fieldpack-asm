package com.ca.apm.swat.epaplugins.asm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.asm.error.LoginError;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.CryptoUtils;
import com.ca.apm.swat.epaplugins.utils.RestClient;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;

/**
 * Access the App Synthetic Monitor API.
 *
 */
public class AsmAccessor extends Accessor implements AsmProperties {

    private boolean localTest;
    private Properties properties;
    private RestClient restClient;
    public static final CryptoUtils crypto = new CryptoUtils(MESSAGE_DIGEST);
    private String localTestPath = "";

    public static final String FAILED = "Failed";
    public static final String LOGGED_OUT = "Logged Out.";
    private static final long DEFAULT_REQUEST_RETRY_DELAY = 30000; // 30s

    /**
     * Access the App Synthetic Monitor API.
     */
    public AsmAccessor() {
        this.properties = EpaUtils.getProperties();

        boolean useProxy = EpaUtils.getBooleanProperty(USE_PROXY, false);

        String proxyHost = null;
        String proxyPort = null;
        String proxyUser = null;
        String proxyPassword = null;

        if (useProxy) {
            proxyHost = this.properties.getProperty(PROXY_HOST, "");
            proxyPort = this.properties.getProperty(PROXY_PORT, "");
            proxyUser = this.properties.getProperty(PROXY_USER, "");

            if (EpaUtils.getBooleanProperty(PROXY_PASSWORD_ENCRYPTED, false)) {
                proxyPassword = AsmAccessor.crypto.decrypt(
                    this.properties.getProperty(PROXY_PASSWORD, ""));
            } else {
                proxyPassword = this.properties.getProperty(PROXY_PASSWORD, "");
            }
        }

        this.restClient = new RestClient(useProxy, proxyHost, proxyPort, proxyUser, proxyPassword);

        this.localTest = EpaUtils.getBooleanProperty(LOCAL_TEST, false);
        if (this.localTest) {
            this.localTestPath = this.properties.getProperty(LOCAL_TEST);
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.swat.epaplugins.asm.Accessor#executeApi(java.lang.String, java.lang.String)
     */
    public String executeApi(String callType, String callParams) throws Exception {
        return executeApi(callType, callParams, true);
    }
    
    /**
     * Execute a call against the App Synthetic Monitor API.
     * @param callType API call
     * @param callParams parameters
     * @param checkError check apiResponse for errors?
     * @return unpadded API call result
     * @throws Exception errors if an error occurred
     */
    private String executeApi(String callType, String callParams, boolean checkError)
            throws Exception {

        String apiResponse = "";
        if (!localTest) {
            long maxRuntime = Long.parseLong(EpaUtils.getProperty(WAIT_TIME)) * 2 / 3;
            long start = new Date().getTime();
            double rrDelay = Double.parseDouble(EpaUtils.getProperty(REQUEST_RETRY_DELAY,
                                                    Long.toString(DEFAULT_REQUEST_RETRY_DELAY)));
            long delay = Math.round(rrDelay + rrDelay * (Math.random() - 0.5));
            boolean retry = false;
            
            while (true) {
                try {
                    URL apiUrl = new URL(this.properties.getProperty(URL) + "/" + callType
                                         + "?" + callParams);
                    apiResponse = this.restClient.request(HTTP_POST, apiUrl, EMPTY_STRING);

                    if (retry) {
                        EpaUtils.getFeedback().info(new Module(Thread.currentThread().getName()),
                            AsmMessages.getMessage(AsmMessages.API_RETRY_SUCCEEDED_509));
                    }
                    break;
                } catch (Exception ex) {
                    retry = true;
                    if (new Date().getTime() - start > maxRuntime) {
                        throw ex;
                    }
                    // retry API call on failure, after a delay
                    EpaUtils.getFeedback().info(new Module(Thread.currentThread().getName()),
                            AsmMessages.getMessage(AsmMessages.API_RETRY_508,
                                                   ex.getMessage(),
                                                   delay));
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ignore) {
                        throw ex;
                    }
                    delay *= 2;
                }
            }
        } else if (!callType.equals(LOGOUT_CMD)) {
            String inputLine = null;
            String inputFileName = this.localTestPath + "\\" + callType + ".txt";
            BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
            while (null != (inputLine = inputFile.readLine())) {
                apiResponse = apiResponse + inputLine;
            }
            inputFile.close();
        } else {
            return LOGGED_OUT;
        }
        
        if (DATABASE_ERROR.equals(apiResponse)) {
            throw new AsmException(AsmMessages.getMessage(AsmMessages.API_ERROR_920,
                ASM_PRODUCT_NAME, DATABASE_ERROR, callType));
        }

        apiResponse = Accessor.unpadJson(apiResponse.trim());

        if (checkError) {
            checkError(callType, apiResponse);
        }
        
        return apiResponse;
    }

    /**
     * Check if an error code was returned by the API and log message.
     * @param command API command
     * @param apiResponse API response
     */
    private void checkError(String command, String apiResponse) {
        Module module = new Module(Thread.currentThread().getName());
        
        if ((null == apiResponse) || (0 == apiResponse.length())) {
            EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                AsmMessages.NULL_RESPONSE_919, command));
            return;
        }
        
        JSONObject jsonObject = new JSONObject(apiResponse);

        int errorCode = jsonObject.optInt(CODE_TAG, -1);

        if (ERROR_OK != errorCode) {
            String errorStr = jsonObject.optString(ERROR_TAG,
                AsmMessages.getMessage(AsmMessages.NO_ERROR_916));
            String errorInfo = jsonObject.optString(INFO_TAG,
                AsmMessages.getMessage(AsmMessages.NO_INFO_917));

            EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(AsmMessages.API_ERROR_914,
                ASM_PRODUCT_NAME, errorCode, errorStr, errorInfo, command));
            
            //TODO: decide if to throw Error
        }
    }


    /**
     * Login to the App Synthetic Monitor API.
     * @return the token returned or {@link FAILED}
     * @throws LoginError if the authentication fails
     * @throws Exception if another error occurred
     */
    public String login() throws LoginError, Exception {
        String user = this.properties.getProperty(USER);
        String password = null;
        if (EpaUtils.getBooleanProperty(PASSWORD_ENCRYPTED, false)) {
            password = AsmAccessor.crypto.decrypt(
                this.properties.getProperty(PASSWORD));
            if (null == password) {
                throw new LoginError(AsmMessages.getMessage(AsmMessages.DECRYPT_ERROR_915));
            }
        } else {
            password = this.properties.getProperty(PASSWORD);
        }

        String loginStr = "user=" + user
                + "&password=" + URLEncoder.encode(password, EpaUtils.getEncoding())
                + "&callback=" + DO_CALLBACK;
        String loginRequest = executeApi(LOGIN_CMD, loginStr, false);
        JSONObject entireJsonObject = new JSONObject(loginRequest);

        if (entireJsonObject.getInt(CODE_TAG) == ERROR_OK) {
            JSONObject resultJsonObject = entireJsonObject.optJSONObject(RESULT_TAG);
            if (resultJsonObject != null) {
                return resultJsonObject.optString(NKEY_TAG, null);
            }
            // should not happen
            // return FAILED;
        }

        String errorStr = entireJsonObject.optString(ERROR_TAG,
            AsmMessages.getMessage(AsmMessages.NO_ERROR_916));
        int errorCode = entireJsonObject.optInt(CODE_TAG, -1);
        String errorInfo = entireJsonObject.optString(INFO_TAG,
            AsmMessages.getMessage(AsmMessages.NO_INFO_917));

        String errorMessage = AsmMessages.getMessage(AsmMessages.LOGIN_ERROR_907,
            errorStr, errorCode, errorInfo);

        if (errorCode == ERROR_AUTHORIZATION) {
            Module module = new Module(Thread.currentThread().getName());
            EpaUtils.getFeedback().error(module, errorMessage);
            throw new LoginError(AsmMessages.getMessage(AsmMessages.LOGIN_INFO_908,
                this.properties.getProperty(URL),
                LOGIN_CMD,
                ASM_PRODUCT_NAME,
                PASSWORD_URL));
        }
        throw new LoginError(errorMessage);
    }
}
