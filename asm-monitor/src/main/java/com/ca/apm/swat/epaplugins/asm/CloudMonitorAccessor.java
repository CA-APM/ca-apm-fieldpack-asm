package com.ca.apm.swat.epaplugins.asm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.utils.ASMMessages;
import com.ca.apm.swat.epaplugins.utils.ASMProperties;
import com.ca.apm.swat.epaplugins.utils.CryptoUtils;
import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.ca.apm.swat.epaplugins.utils.PropertiesUtils;
import com.ca.apm.swat.epaplugins.utils.RESTClient;

public class CloudMonitorAccessor {

  private boolean apmcmLocalTest;
  private PropertiesUtils apmcmProperties;
  private RESTClient apmcmClient;
  public static final CryptoUtils apmcmCrypto = new CryptoUtils(EPAConstants.kMsgDigest);
  private String apmcmLocalTestPath = "";

  public static final String FAILED = "Failed";
  public static final String LOGGED_OUT = "Logged Out.";
  
  public CloudMonitorAccessor(PropertiesUtils apmcmProperties) {
    this.apmcmProperties = apmcmProperties;

    String proxyHost = this.apmcmProperties.getProperty(ASMProperties.PROXY_HOST, "");
    String proxyPort = this.apmcmProperties.getProperty(ASMProperties.PROXY_PORT, "");
    String proxyUser = this.apmcmProperties.getProperty(ASMProperties.PROXY_USER, "");
    String proxyPass;
    if (this.apmcmProperties.getProperty(ASMProperties.PROXY_PASS_ENCRYPTED, ASMProperties.FALSE).equals(ASMProperties.TRUE))
      proxyPass = CloudMonitorAccessor.apmcmCrypto.decrypt(this.apmcmProperties.getProperty(ASMProperties.PROXY_PASS, ""));
    else {
      proxyPass = this.apmcmProperties.getProperty(ASMProperties.PROXY_PASS, "");
    }

    this.apmcmLocalTest = Boolean.parseBoolean(this.apmcmProperties.getProperty(ASMProperties.LOCAL_TEST, ASMProperties.FALSE));
    if (this.apmcmLocalTest) {
      this.apmcmLocalTestPath = this.apmcmProperties.getProperty(ASMProperties.LOCAL_TEST);
    }
    this.apmcmClient = new RESTClient(proxyHost, proxyPort, proxyUser, proxyPass);
  }

  public String executeAPI(String callType, String callParams) throws Exception {
    String apiResponse = "";
    if (!apmcmLocalTest) {
      URL apiURL = new URL(this.apmcmProperties.getProperty(ASMProperties.URL) + "/" + callType);
      apiResponse = this.apmcmClient.request(EPAConstants.apmcmQuiet.booleanValue(), EPAConstants.apmcmMethod, apiURL, callParams);
      String inputLine = null;
      String inputFileName = this.apmcmLocalTestPath + "\\" + callType + ".txt";
      BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
      while ((inputLine = inputFile.readLine()) != null)
        apiResponse = apiResponse + inputLine;
      inputFile.close();
    } else {
      return LOGGED_OUT;
    }

    return apiResponse.trim();
  }

  public String login() throws Exception {
    String apmcmUser = this.apmcmProperties.getProperty(ASMProperties.USER);
    String apmcmPass = null;
    if (this.apmcmProperties.getProperty(ASMProperties.PASS_ENCRYPTED).equals(ASMProperties.TRUE))
      apmcmPass = CloudMonitorAccessor.apmcmCrypto.decrypt(this.apmcmProperties.getProperty(ASMProperties.PASS));
    else {
      apmcmPass = this.apmcmProperties.getProperty(ASMProperties.PASS);
    }

    String loginStr = "user=" + apmcmUser + "&password=" + apmcmPass + "&callback=" + EPAConstants.apmcmCallback;
    String loginRequest = executeAPI(EPAConstants.kAPMCMLoginCmd, loginStr);
    JSONObject entireJO = new JSONObject(unpadJSON(loginRequest));

    if (entireJO.getInt(EPAConstants.kAPMCMCode) == 0) {
      JSONObject resultJO = entireJO.optJSONObject(EPAConstants.kAPMCMResult);
      if (resultJO != null) {
        return resultJO.optString(EPAConstants.kAPMCMNKey, null);
      }
      return FAILED;
    }

    String errorStr = entireJO.optString(EPAConstants.kAPMCMError, ASMMessages.getMessage(ASMMessages.noError));
    int errorCode = entireJO.optInt(EPAConstants.kAPMCMCode, -1);
    String errorInfo = entireJO.optString(EPAConstants.kAPMCMInfo, ASMMessages.getMessage(ASMMessages.noInfo));

    System.err.println(ASMMessages.getMessage(ASMMessages.loginError, new Object[]{errorStr, errorCode, errorInfo}));

    if (errorCode == 1000) {
      System.err.print(ASMMessages.getMessage(ASMMessages.loginInfo,
    		  new Object[]{this.apmcmProperties.getProperty(ASMProperties.URL),
    		  EPAConstants.kAPMCMLoginCmd,
    		  EPAConstants.apmcmProductName,
    		  EPAConstants.apmcmPasswordPage}));
   }

    System.exit(1000);

    return FAILED;
  }


  private String unpadJSON(String jsonWithPadding) throws Exception {
    String patternToMatch = EPAConstants.kJsonRegex;

    Pattern unpad = Pattern.compile(patternToMatch);
    Matcher matched = unpad.matcher(jsonWithPadding);

    if (matched.find()) {
      return matched.group(1);
    }
    return null;
  }

//  public String executeAPINew(String callType, String callParams) throws Exception {
//    String apiResponse = "";
//    if (!apmcmLocalTest) {
//      URL apiURL = new URL(this.apmcmProperties.getProperty("apmcm.URL") + "/" + callType);
//      apiResponse = this.apmcmClient.requestNew(EPAConstants.apmcmQuiet.booleanValue(), "POST", apiURL, callParams);
//    } else if (!callType.equals("acct_logout")) {
//      String inputLine = null;
//      String inputFileName = this.apmcmLocalTestPath + "\\" + callType + ".txt";
//      BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
//      while ((inputLine = inputFile.readLine()) != null)
//        apiResponse = apiResponse + inputLine;
//      inputFile.close();
//    } else {
//      return "Logged Out.";
//    }
//
//    return apiResponse.trim();
//  }

}
