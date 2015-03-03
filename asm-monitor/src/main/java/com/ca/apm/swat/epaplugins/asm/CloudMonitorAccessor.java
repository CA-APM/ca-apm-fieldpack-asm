package com.ca.apm.swat.epaplugins.asm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.utils.CryptoUtils;
import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.ca.apm.swat.epaplugins.utils.PropertiesUtils;
import com.ca.apm.swat.epaplugins.utils.RESTClient;

public class CloudMonitorAccessor {

  private boolean apmcmLocalTest;
  private PropertiesUtils apmcmProperties;
  private RESTClient apmcmClient;
  public static final CryptoUtils apmcmCrypto = new CryptoUtils("1D0NTF33LT4RDY");
  private String apmcmLocalTestPath = "";

  public CloudMonitorAccessor(PropertiesUtils apmcmProperties) {
    this.apmcmProperties = apmcmProperties;

    String proxyHost = this.apmcmProperties.getProperty("apmcm.proxy.host", "");
    String proxyPort = this.apmcmProperties.getProperty("apmcm.proxy.port", "");
    String proxyUser = this.apmcmProperties.getProperty("apmcm.proxy.user", "");
    String proxyPass;
    if (this.apmcmProperties.getProperty("apmcm.proxy.pass.encrypted", "false").equals("true"))
      proxyPass = CloudMonitorAccessor.apmcmCrypto.decrypt(this.apmcmProperties.getProperty("apmcm.proxy.pass", ""));
    else {
      proxyPass = this.apmcmProperties.getProperty("apmcm.proxy.pass", "");
    }

    this.apmcmLocalTest = Boolean.parseBoolean(this.apmcmProperties.getProperty("apmcm.localtest", "false"));
    if (this.apmcmLocalTest) {
      this.apmcmLocalTestPath = this.apmcmProperties.getProperty("apmcm.localtestpath");
    }
    this.apmcmClient = new RESTClient(proxyHost, proxyPort, proxyUser, proxyPass);
  }

  public String executeAPI(String callType, String callParams) throws Exception {
    String apiResponse = "";
    if (!apmcmLocalTest) {
      URL apiURL = new URL(this.apmcmProperties.getProperty("apmcm.URL") + "/" + callType);
      apiResponse = this.apmcmClient.request(EPAConstants.apmcmQuiet.booleanValue(), "POST", apiURL, callParams);
    } else if (!callType.equals("acct_logout")) {
      String inputLine = null;
      String inputFileName = this.apmcmLocalTestPath + "\\" + callType + ".txt";
      BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));
      while ((inputLine = inputFile.readLine()) != null)
        apiResponse = apiResponse + inputLine;
      inputFile.close();
    } else {
      return "Logged Out.";
    }

    return apiResponse.trim();
  }

  public String login() throws Exception {
    String apmcmUser = this.apmcmProperties.getProperty("apmcm.user");
    String apmcmPass = null;
    if (this.apmcmProperties.getProperty("apmcm.pass.encrypted").equals("true"))
      apmcmPass = CloudMonitorAccessor.apmcmCrypto.decrypt(this.apmcmProperties.getProperty("apmcm.pass"));
    else {
      apmcmPass = this.apmcmProperties.getProperty("apmcm.pass");
    }

    String loginStr = "user=" + apmcmUser + "&password=" + apmcmPass + "&callback=" + "doCallback";
    String loginRequest = executeAPI("acct_token", loginStr);
    JSONObject entireJO = new JSONObject(unpadJSON(loginRequest));

    if (entireJO.getInt("code") == 0) {
      JSONObject resultJO = entireJO.optJSONObject("result");
      if (resultJO != null) {
        return resultJO.optString("nkey", null);
      }
      return "Failed";
    }

    String errorStr = entireJO.optString("error", "No error given.");
    int errorCode = entireJO.optInt("code", -1);
    String errorInfo = entireJO.optString("info", "No information given.");

    System.err.println("Error: " + errorStr);
    System.err.println("Error Code: " + errorCode);
    System.err.println("Error Info: " + errorInfo);

    if (errorCode == 1000) {
      System.err.print("Login Failed.\nTry logging manually at " + this.apmcmProperties.getProperty("apmcm.URL") + "/"
        + "acct_token" + "\nwith the login credentials in your config file.\n"
        + "NOTE: If this is your first time using the APM Cloud Monitor Agent,\n" + "set your API password at : "
        + "http://www.watchmouse.com/en/change_passwd.php" + "\n"
        + "Use the password provided to you by Watchmouse when you signed up.");
    }

    System.exit(1000);

    return "Failed";
  }


  private String unpadJSON(String jsonWithPadding) throws Exception {
    String patternToMatch = "doCallback\\((.*)\\)([\n]*)";

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
