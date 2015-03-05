package com.ca.apm.swat.epaplugins.asm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.utils.ASMProperties;
import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.ca.apm.swat.epaplugins.utils.JSONHelper;
import com.ca.apm.swat.epaplugins.utils.PropertiesUtils;
import com.ca.apm.swat.epaplugins.utils.StringFilter;
import com.ca.apm.swat.epaplugins.utils.TextNormalizer;

public class CloudMonitorRequestHelper {

  private CloudMonitorAccessor cloudMonitorAccessor;
  private PropertiesUtils apmcmProperties;
  private String nkey;

  public CloudMonitorRequestHelper(CloudMonitorAccessor cloudMonitorAccessor, PropertiesUtils apmcmProperties) {
    this.cloudMonitorAccessor = cloudMonitorAccessor;
    this.apmcmProperties = apmcmProperties;
  }

  public void connect() throws Exception {
    this.nkey = cloudMonitorAccessor.login();
  }

  public String[] getFolders() throws Exception {
    String[] apmcmFolders;
    if ((apmcmProperties.getProperty(ASMProperties.FOLDERS, EPAConstants.EMPTY_STRING).length() == 0)
      || (apmcmProperties.getProperty(ASMProperties.FOLDERS, EPAConstants.EMPTY_STRING).contains(EPAConstants.apmcmAllFolders)))
      apmcmFolders = getFolders(EPAConstants.apmcmAllFolders, cloudMonitorAccessor, apmcmProperties);
    else {
      apmcmFolders = getFolders(apmcmProperties.getProperty(ASMProperties.FOLDERS), cloudMonitorAccessor, apmcmProperties);
    }

    return apmcmFolders;
  }

  private String[] getFolders(String folderList, CloudMonitorAccessor cloudMonitorAccessor,
    PropertiesUtils apmcmProperties) throws Exception {
    List<String> folderQueryOutput = new ArrayList<String>();
    String folderRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMFoldersCmd, getCommandString());

    JSONArray folderJA = extractJSONArray(folderRequest, EPAConstants.kAPMCMFolders);

    folderQueryOutput.add(EPAConstants.apmcmRootFolder);
    for (int i = 0; i < folderJA.length(); i++) {
      JSONObject folderJO = folderJA.getJSONObject(i);

      if ((!folderJO.optString(EPAConstants.kAPMCMActive, EPAConstants.EMPTY_STRING).equals(EPAConstants.YES))
        && (apmcmProperties.getProperty(ASMProperties.SKIP_INACTIVE_FOLDERS, EPAConstants.EMPTY_STRING).equals(ASMProperties.TRUE)))
        continue;
      folderQueryOutput.add(folderJO.get(EPAConstants.kAPMCMName).toString());
    }

    if (!folderList.equals(EPAConstants.apmcmAllFolders)) {
      return compareList(folderQueryOutput, folderList);
    }
    return (String[]) folderQueryOutput.toArray(EPAConstants.kNoStringArrayProperties);
  }

  private String getCommandString() {
	return EPAConstants.kAPMCMNKeyParam + this.nkey + EPAConstants.kAPMCMCallbackParam + EPAConstants.apmcmCallback;
}

private JSONArray extractJSONArray(String metricInput, String arrayName) throws Exception {
    JSONObject entireJO = new JSONObject(JSONHelper.unpadJSON(metricInput));
    JSONArray thisJA = new JSONArray();

    if (entireJO.optJSONObject(EPAConstants.kAPMCMResult) != null) {
      JSONObject resultJO = entireJO.getJSONObject(EPAConstants.kAPMCMResult);

      if (resultJO.optJSONArray(arrayName) != null) {
        thisJA = resultJO.optJSONArray(arrayName);
      }
    }

    return thisJA;
  }

  private String[] compareList(List<String> masterList, String comparisonString) {
    List<String> checkList = Arrays.asList(comparisonString.split(","));
    masterList.retainAll(checkList);
    return (String[]) masterList.toArray(EPAConstants.kNoStringArrayProperties);
  }

  public HashMap<String, String> getCredits() throws Exception {
    HashMap<String, String> metric_map = new HashMap<String, String>();
    String creditsRequest = EPAConstants.EMPTY_STRING;
    creditsRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMCreditsCmd, getCommandString());

    JSONArray creditJA = extractJSONArray(creditsRequest, EPAConstants.kAPMCMCredits);

    for (int i = 0; i < creditJA.length(); i++) {
      JSONObject thisCreditJO = creditJA.getJSONObject(i);

      String thisKey = thisCreditJO.optString(EPAConstants.kAPMCMType, EPAConstants.NO_TYPE);
      String thisValue = thisCreditJO.optString("available", EPAConstants.ZERO);

      if (EPAConstants.apmcmMetrics.containsKey(thisKey)) {
        thisKey = ((String) EPAConstants.apmcmMetrics.get(thisKey)).toString();
      }

      String rawMetric = EPAConstants.kCreditsCategory + ":" + thisKey;
      metric_map.put(fixMetric(rawMetric), fixMetric(thisValue));
    }

    return metric_map;
  }

  public static String fixMetric(String rawMetric) {
    StringFilter thisNormalizer = null;
    try {
      thisNormalizer = TextNormalizer.getNormalizationStringFilter();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1001);
    }

    String metricKeyNormalized = thisNormalizer.filter(rawMetric);
    Pattern pattern = Pattern.compile(EPAConstants.kJsonPattern);
    String fixedMetric = pattern.matcher(metricKeyNormalized).replaceAll(EPAConstants.EMPTY_STRING);

    return fixedMetric.replace("\\", "-").replace("/", "-").replace(",", "_").replace(";", "-").replace("&", "and");
  }

  public HashMap<String, String> getCheckpoints() throws Exception {
    HashMap<String, String> returnCp = new HashMap<String, String>();

    String cpRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMCheckptsCmd, getCommandString());

    JSONArray cpJA = extractJSONArray(cpRequest, EPAConstants.kAPMCMCheckpoints);

    for (int i = 0; i < cpJA.length(); i++) {
      JSONObject cpJO = cpJA.getJSONObject(i);
      if (cpJO.get(EPAConstants.kAPMCMAreas).toString().contains(","))
        returnCp.put(
          cpJO.get(EPAConstants.kAPMCMLoc).toString(),
          cpJO.get(EPAConstants.kAPMCMAreas).toString().split(",")[1] + "|" + cpJO.get(EPAConstants.kAPMCMCountry) + "|" + cpJO.get(EPAConstants.kAPMCMCity));
      else {
        returnCp.put(
          cpJO.get(EPAConstants.kAPMCMLoc).toString(),
          cpJO.get(EPAConstants.kAPMCMAreas) + "|" + cpJO.get(EPAConstants.kAPMCMCountry) + "|" + cpJO.get(EPAConstants.kAPMCMCity));
      }
    }

    return returnCp;
  }

  private String[] getRules(String folder, String rulesList) throws Exception {
    List<String> ruleQueryOutput = new ArrayList<String>();
    String folderStr = EPAConstants.EMPTY_STRING;
    if (!folder.equals(EPAConstants.apmcmRootFolder))
      folderStr = EPAConstants.kAPMCMFolderParam + folder;
    else {
      folder = EPAConstants.EMPTY_STRING;
    }

    String ruleRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMRuleCmd, getCommandString() + folderStr);

    JSONArray ruleJA = extractJSONArray(ruleRequest, EPAConstants.kAPMCMRules);

    for (int i = 0; i < ruleJA.length(); i++) {
      JSONObject thisRuleJO = ruleJA.getJSONObject(i);
      if (!thisRuleJO.optString(EPAConstants.kAPMCMFolder, EPAConstants.EMPTY_STRING).equals(folder)) {
        continue;
      }
      if ((!thisRuleJO.optString(EPAConstants.kAPMCMActive, EPAConstants.NO).equals(EPAConstants.YES))
        && (this.apmcmProperties.getProperty(ASMProperties.SKIP_INACTIVE_FOLDERS, ASMProperties.FALSE).equals(ASMProperties.TRUE)))
        continue;
      ruleQueryOutput.add(thisRuleJO.getString(EPAConstants.kAPMCMName));
    }

    if (!rulesList.equals(EPAConstants.apmcmAllRules)) {
      return compareList(ruleQueryOutput, rulesList);
    }
    return (String[]) ruleQueryOutput.toArray(EPAConstants.kNoStringArrayProperties);
  }

  public HashMap<String, String[]> getFoldersAndRules(String[] apmcmFolders) throws Exception {
    HashMap<String, String[]> foldersAndRules = new HashMap<String, String[]>();


    for (int i = 0; i < apmcmFolders.length; i++) {
      String thisFolderProp = apmcmProperties.getProperty(ASMProperties.FOLDER_PREFIX + apmcmFolders[i], EPAConstants.EMPTY_STRING);
      String[] rules;
      if ((thisFolderProp.length() == 0) || (thisFolderProp.equals(EPAConstants.apmcmAllRules))) {
        String[] allRules = getRules(apmcmFolders[i], EPAConstants.apmcmAllRules);
        rules = new String[allRules.length + 1];
        rules[0] = EPAConstants.apmcmAllRules;
        for (int j = 0; j < allRules.length; j++)
          rules[(j + 1)] = allRules[j];
      } else {
        rules = getRules(apmcmFolders[i], thisFolderProp);
      }
      if (rules.length > 0) {
        foldersAndRules.put(apmcmFolders[i], rules);
      }
    }
    return foldersAndRules;
  }

  public String getStats(String folder, String rule, String apmcmUser) throws Exception {
    String statsRequest = EPAConstants.EMPTY_STRING;
    String folderStr = EPAConstants.EMPTY_STRING;
    String ruleStr = EPAConstants.EMPTY_STRING;

    if ((folder.length() != 0) && (!folder.equals(EPAConstants.apmcmRootFolder)))
      folderStr = EPAConstants.kAPMCMFolderParam + folder;
    else {
      folder = EPAConstants.apmcmRootFolder;
    }

    if (rule.length() != 0) {
      ruleStr = EPAConstants.kAPMCMNameParam + rule;
      folder = folder + "|" + rule;
    }

    String statsStr = EPAConstants.kAPMCMNKeyParam + this.nkey + EPAConstants.kAPMCMAccountParam + apmcmUser + folderStr + ruleStr + EPAConstants.kAPMCMStartDateParam
      + getTodaysDate() + EPAConstants.kAPMCMCallbackParam + EPAConstants.apmcmCallback;
    statsRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMStatsCmd, statsStr);
    return statsRequest;
  }

  public String getPSP(String folder, String rule, String apmcmUser) throws Exception {
    String pspRequest = EPAConstants.EMPTY_STRING;
    String folderStr = EPAConstants.EMPTY_STRING;
    String ruleStr = EPAConstants.EMPTY_STRING;

    if ((folder.length() != 0) && (!folder.equals(EPAConstants.apmcmRootFolder)))
      folderStr = EPAConstants.kAPMCMFolderParam + folder;
    else {
      folder = EPAConstants.apmcmRootFolder;
    }

    if (rule.length() != 0) {
      ruleStr = EPAConstants.kAPMCMNameParam + rule;
    }

    pspRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMPSPCmd, getCommandString() + folderStr + ruleStr);
    return pspRequest;
  }

  public String getLogs(String folder, String rule, int numRules) throws Exception {
    String logRequest = EPAConstants.EMPTY_STRING;
    String folderStr = EPAConstants.EMPTY_STRING;
    String ruleStr = EPAConstants.EMPTY_STRING;
    int numLogs = Integer.parseInt(this.apmcmProperties.getProperty(ASMProperties.NUM_LOGS)) * numRules;
    if ((folder.length() != 0) && (!folder.equals(EPAConstants.apmcmRootFolder)))
      folderStr = EPAConstants.kAPMCMFolderParam + folder;
    else {
      folder = EPAConstants.apmcmRootFolder;
    }

    if (rule.length() != 0) {
      ruleStr = EPAConstants.kAPMCMNameParam + rule;
    }
    String logStr = EPAConstants.kAPMCMNKeyParam + this.nkey + folderStr + ruleStr + EPAConstants.kAPMCMNumParam + numLogs + 
    		EPAConstants.kAPMCMReverseParam + EPAConstants.kAPMCMCallbackParam + EPAConstants.apmcmCallback + EPAConstants.kAPMCMFullParam;
    //    String logStr = "nkey=" + this.nkey + folderStr + ruleStr + "&num=" + numLogs + "&reverse=y&full=y";

    // logRequest = cloudMonitorAccessor.executeAPINew("rule_log", logStr);
    logRequest = cloudMonitorAccessor.executeAPI(EPAConstants.kAPMCMLogsCmd, logStr);
    return logRequest;
  }

  private String getTodaysDate() throws Exception {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat(EPAConstants.DATE_FORMAT);
    String todaysDate = null;
    todaysDate = dateFormat.format(calendar.getTime());
    return todaysDate;
  }



}
