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
    if ((apmcmProperties.getProperty("apmcm.folders", "").length() == 0)
      || (apmcmProperties.getProperty("apmcm.folders", "").contains("all_folders")))
      apmcmFolders = getFolders("all_folders", cloudMonitorAccessor, apmcmProperties);
    else {
      apmcmFolders = getFolders(apmcmProperties.getProperty("apmcm.folders"), cloudMonitorAccessor, apmcmProperties);
    }

    return apmcmFolders;
  }

  private String[] getFolders(String folderList, CloudMonitorAccessor cloudMonitorAccessor,
    PropertiesUtils apmcmProperties) throws Exception {
    List<String> folderQueryOutput = new ArrayList<String>();
    String folderStr = "nkey=" + this.nkey + "&callback=" + "doCallback";
    String folderRequest = cloudMonitorAccessor.executeAPI("fldr_get", folderStr);

    JSONArray folderJA = extractJSONArray(folderRequest, "folders");

    folderQueryOutput.add("root_folder");
    for (int i = 0; i < folderJA.length(); i++) {
      JSONObject folderJO = folderJA.getJSONObject(i);

      if ((!folderJO.optString("active", "").equals("y"))
        && (apmcmProperties.getProperty("apmcm.skip_inactive.folders", "").equals("true")))
        continue;
      folderQueryOutput.add(folderJO.get("name").toString());
    }

    if (!folderList.equals("all_folders")) {
      return compareList(folderQueryOutput, folderList);
    }
    return (String[]) folderQueryOutput.toArray(EPAConstants.kNoStringArrayProperties);
  }

  private JSONArray extractJSONArray(String metricInput, String arrayName) throws Exception {
    JSONObject entireJO = new JSONObject(JSONHelper.unpadJSON(metricInput));
    JSONArray thisJA = new JSONArray();

    if (entireJO.optJSONObject("result") != null) {
      JSONObject resultJO = entireJO.getJSONObject("result");

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
    String creditsRequest = "";
    String creditsStr = "nkey=" + this.nkey + "&callback=" + "doCallback";
    creditsRequest = cloudMonitorAccessor.executeAPI("acct_credits", creditsStr);

    JSONArray creditJA = extractJSONArray(creditsRequest, "credits");

    for (int i = 0; i < creditJA.length(); i++) {
      JSONObject thisCreditJO = creditJA.getJSONObject(i);

      String thisKey = thisCreditJO.optString("type", "no type");
      String thisValue = thisCreditJO.optString("available", "0");

      if (EPAConstants.apmcmMetrics.containsKey(thisKey)) {
        thisKey = ((String) EPAConstants.apmcmMetrics.get(thisKey)).toString();
      }

      String rawMetric = "Credits:" + thisKey;
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
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    String fixedMetric = pattern.matcher(metricKeyNormalized).replaceAll("");

    return fixedMetric.replace("\\", "-").replace("/", "-").replace(",", "_").replace(";", "-").replace("&", "and");
  }

  public HashMap<String, String> getCheckpoints() throws Exception {
    HashMap<String, String> returnCp = new HashMap<String, String>();

    String cpStr = "nkey=" + this.nkey + "&callback=" + "doCallback";
    String cpRequest = cloudMonitorAccessor.executeAPI("cp_list", cpStr);

    JSONArray cpJA = extractJSONArray(cpRequest, "checkpoints");

    for (int i = 0; i < cpJA.length(); i++) {
      JSONObject cpJO = cpJA.getJSONObject(i);
      if (cpJO.get("areas").toString().contains(","))
        returnCp.put(
          cpJO.get("loc").toString(),
          cpJO.get("areas").toString().split(",")[1] + "|" + cpJO.get("country_name") + "|" + cpJO.get("city"));
      else {
        returnCp.put(
          cpJO.get("loc").toString(),
          cpJO.get("areas") + "|" + cpJO.get("country_name") + "|" + cpJO.get("city"));
      }
    }

    return returnCp;
  }

  private String[] getRules(String folder, String rulesList) throws Exception {
    List<String> ruleQueryOutput = new ArrayList<String>();
    String folderStr = "";
    if (!folder.equals("root_folder"))
      folderStr = "&folder=" + folder;
    else {
      folder = "";
    }

    String ruleStr = "nkey=" + this.nkey + folderStr + "&callback=" + "doCallback";
    String ruleRequest = cloudMonitorAccessor.executeAPI("rule_get", ruleStr);

    JSONArray ruleJA = extractJSONArray(ruleRequest, "rules");

    for (int i = 0; i < ruleJA.length(); i++) {
      JSONObject thisRuleJO = ruleJA.getJSONObject(i);
      if (!thisRuleJO.optString("folder", "").equals(folder)) {
        continue;
      }
      if ((!thisRuleJO.optString("active", "n").equals("y"))
        && (this.apmcmProperties.getProperty("apmcm.skip_inactive.rules", "false").equals("true")))
        continue;
      ruleQueryOutput.add(thisRuleJO.getString("name"));
    }

    if (!rulesList.equals("all_rules")) {
      return compareList(ruleQueryOutput, rulesList);
    }
    return (String[]) ruleQueryOutput.toArray(EPAConstants.kNoStringArrayProperties);
  }

  public HashMap<String, String[]> getFoldersAndRules(String[] apmcmFolders) throws Exception {
    HashMap<String, String[]> foldersAndRules = new HashMap<String, String[]>();


    for (int i = 0; i < apmcmFolders.length; i++) {
      String thisFolderProp = apmcmProperties.getProperty("apmcm.folder." + apmcmFolders[i], "");
      String[] rules;
      if ((thisFolderProp.length() == 0) || (thisFolderProp.equals("all_rules"))) {
        String[] allRules = getRules(apmcmFolders[i], "all_rules");
        rules = new String[allRules.length + 1];
        rules[0] = "all_rules";
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
    String statsRequest = "";
    String folderStr = "";
    String ruleStr = "";

    if ((folder.length() != 0) && (!folder.equals("root_folder")))
      folderStr = "&folder=" + folder;
    else {
      folder = "root_folder";
    }

    if (rule.length() != 0) {
      ruleStr = "&name=" + rule;
      folder = folder + "|" + rule;
    }

    String statsStr = "nkey=" + this.nkey + "&acct=" + apmcmUser + folderStr + ruleStr + "&start_date="
      + getTodaysDate() + "&callback=" + "doCallback";
    statsRequest = cloudMonitorAccessor.executeAPI("rule_stats", statsStr);
    return statsRequest;
  }

  public String getPSP(String folder, String rule, String apmcmUser) throws Exception {
    String pspRequest = "";
    String folderStr = "";
    String ruleStr = "";

    if ((folder.length() != 0) && (!folder.equals("root_folder")))
      folderStr = "&folder=" + folder;
    else {
      folder = "root_folder";
    }

    if (rule.length() != 0) {
      ruleStr = "&name=" + rule;
    }

    String pspStr = "nkey=" + this.nkey + "&acct=" + apmcmUser + folderStr + ruleStr + "&callback=" + "doCallback";
    pspRequest = cloudMonitorAccessor.executeAPI("rule_psp", pspStr);
    return pspRequest;
  }

  public String getLogs(String folder, String rule, int numRules) throws Exception {
    String logRequest = "";
    String folderStr = "";
    String ruleStr = "";
    int numLogs = Integer.parseInt(this.apmcmProperties.getProperty("apmcm.numlogs")) * numRules;
    if ((folder.length() != 0) && (!folder.equals("root_folder")))
      folderStr = "&folder=" + folder;
    else {
      folder = "root_folder";
    }

    if (rule.length() != 0) {
      ruleStr = "&name=" + rule;
    }
    String logStr = "nkey=" + this.nkey + folderStr + ruleStr + "&num=" + numLogs + "&reverse=y&callback="
      + "doCallback" + "&full=y";
    //    String logStr = "nkey=" + this.nkey + folderStr + ruleStr + "&num=" + numLogs + "&reverse=y&full=y";

    // logRequest = cloudMonitorAccessor.executeAPINew("rule_log", logStr);
    logRequest = cloudMonitorAccessor.executeAPI("rule_log", logStr);
    return logRequest;
  }

  private String getTodaysDate() throws Exception {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String todaysDate = null;
    todaysDate = dateFormat.format(calendar.getTime());
    return todaysDate;
  }



}
