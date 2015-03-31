package com.ca.apm.swat.epaplugins.asm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.AsmPropertiesImpl;
import com.ca.apm.swat.epaplugins.utils.JsonHelper;
import com.ca.apm.swat.epaplugins.utils.StringFilter;
import com.ca.apm.swat.epaplugins.utils.TextNormalizer;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Interface to App Synthetic Monitor API.
 */
public class CloudMonitorRequestHelper implements AsmProperties {

    private CloudMonitorAccessor cloudMonitorAccessor;
    private Properties apmcmProperties;
    private String nkey;
    private String apmcmUser;
    /**
     * Create new CloudMonitorRequestHelper.
     * @param cloudMonitorAccessor accessor
     * @param apmcmProperties properties
     */
    public CloudMonitorRequestHelper(CloudMonitorAccessor cloudMonitorAccessor,
                                     Properties apmcmProperties) {
        this.cloudMonitorAccessor = cloudMonitorAccessor;
        this.apmcmProperties = apmcmProperties;
        this.apmcmUser = apmcmProperties.getProperty(USER);
    }

    /**
     * Connect to App Synthetic Monitor API.
     * @throws Exception errors
     */
    public void connect() throws Exception {
        this.nkey = cloudMonitorAccessor.login();
    }

    /**
     * Get the folders to monitor.
     * @return array of folders to monitor
     * @throws Exception errors
     */
    public String[] getFolders() throws Exception {
        String[] apmcmFolders;
        if ((apmcmProperties.getProperty(FOLDERS, EMPTY_STRING).length() == 0)
                || (apmcmProperties.getProperty(FOLDERS, EMPTY_STRING).contains(ALL_FOLDERS))) {
            apmcmFolders = getFolders(ALL_FOLDERS);
        } else {
            apmcmFolders = getFolders(apmcmProperties.getProperty(FOLDERS));
        }

        return apmcmFolders;
    }

    /**
     * Get the folders to monitor.
     * @param folderList list of folders to query or {@link AsmProperties#ALL_FOLDERS}
     * @return array of folders to monitor
     * @throws Exception errors
     */
    private String[] getFolders(String folderList) throws Exception {
        List<String> folderQueryOutput = new ArrayList<String>();
        String folderRequest = cloudMonitorAccessor.executeApi(kAPMCMFoldersCmd,
            getCommandString());

        JSONArray folderJsonArray = extractJsonArray(folderRequest, kAPMCMFolders);

        folderQueryOutput.add(ROOT_FOLDER);
        for (int i = 0; i < folderJsonArray.length(); i++) {
            JSONObject folderJsonObject = folderJsonArray.getJSONObject(i);

            if ((!folderJsonObject.optString(kAPMCMActive, EMPTY_STRING).equals(YES))
                    && (apmcmProperties.getProperty(SKIP_INACTIVE_FOLDERS, EMPTY_STRING)
                            .equals(TRUE))) {
                continue;
            }
            folderQueryOutput.add(folderJsonObject.get(kAPMCMName).toString());
        }

        if (!folderList.equals(ALL_FOLDERS)) {
            return compareList(folderQueryOutput, folderList);
        }
        return (String[]) folderQueryOutput.toArray(kNoStringArrayProperties);
    }

    /**
     * Get the command string.
     */
    private String getCommandString() {
        return kAPMCMNKeyParam + this.nkey + kAPMCMCallbackParam + apmcmCallback;
    }

    /**
     * Extract a named JSON array from the input.
     * @param metricInput JSON string
     * @param arrayName name of the array to extract
     * @return the array
     * @throws Exception errors
     */
    private JSONArray extractJsonArray(String metricInput, String arrayName) throws Exception {
        JSONObject entireJsonObject = new JSONObject(JsonHelper.unpadJson(metricInput));
        JSONArray thisJsonArray = new JSONArray();

        if (entireJsonObject.optJSONObject(kAPMCMResult) != null) {
            JSONObject resultJsonObject = entireJsonObject.getJSONObject(kAPMCMResult);

            if (resultJsonObject.optJSONArray(arrayName) != null) {
                thisJsonArray = resultJsonObject.optJSONArray(arrayName);
            }
        }

        return thisJsonArray;
    }

    /**
     * Compare list with comma-separated string.
     * All list entries that are not matched in the comparison string are removed from the list.
     * @param masterList master list
     * @param comparisonString comma-separated string of entries to match
     * @return reduced list matching <code>comparisonString</code>
     */
    private String[] compareList(List<String> masterList, String comparisonString) {
        List<String> checkList = Arrays.asList(comparisonString.split(","));
        masterList.retainAll(checkList);
        return (String[]) masterList.toArray(kNoStringArrayProperties);
    }

    /**
     * Get the credits from the App Synthetic Monitor API.
     * @return metric map for credits
     * @throws Exception errors
     */
    public HashMap<String, String> getCredits() throws Exception {
        HashMap<String, String> metricMap = new HashMap<String, String>();
        String creditsRequest = EMPTY_STRING;
        creditsRequest = cloudMonitorAccessor.executeApi(kAPMCMCreditsCmd, getCommandString());

        JSONArray creditJsonArray = extractJsonArray(creditsRequest, kAPMCMCredits);

        for (int i = 0; i < creditJsonArray.length(); i++) {
            JSONObject thisCreditJsonObject = creditJsonArray.getJSONObject(i);

            String thisKey = thisCreditJsonObject.optString(kAPMCMType, NO_TYPE);
            String thisValue = thisCreditJsonObject.optString("available", ZERO);

            if (AsmPropertiesImpl.APM_CM_METRICS.containsKey(thisKey)) {
                thisKey = ((String) AsmPropertiesImpl.APM_CM_METRICS.get(thisKey)).toString();
            }

            String rawMetric = kCreditsCategory + ":" + thisKey;
            metricMap.put(fixMetric(rawMetric), fixMetric(thisValue));
        }

        return metricMap;
    }

    /**
     * Replace unsupported characters in metric name.
     * @param rawMetric raw metric name
     * @return cleansed metric name
     */
    public static String fixMetric(String rawMetric) {
        StringFilter thisNormalizer = null;
        try {
            thisNormalizer = TextNormalizer.getNormalizationStringFilter();
        } catch (ClassNotFoundException e) {
            EpaUtils.getFeedback().error(e.getMessage());
            System.err.print(e.getMessage());
            System.exit(1001);
        }

        String metricKeyNormalized = thisNormalizer.filter(rawMetric);
        Pattern pattern = Pattern.compile(kJsonPattern);
        String fixedMetric = pattern.matcher(metricKeyNormalized).replaceAll(EMPTY_STRING);

        return fixedMetric.replace("\\", "-")
                .replace("/", "-")
                .replace(",", "_")
                .replace(";", "-")
                .replace("&", "and");
    }

    /**
     * Get the checkpoints from the App Synthetic Monitor API.
     * @return map of checkpoints
     * @throws Exception errors
     */
    public HashMap<String, String> getCheckpoints() throws Exception {
        HashMap<String, String> returnCp = new HashMap<String, String>();

        String cpRequest = cloudMonitorAccessor.executeApi(kAPMCMCheckptsCmd, getCommandString());

        JSONArray cpJsonArray = extractJsonArray(cpRequest, kAPMCMCheckpoints);

        for (int i = 0; i < cpJsonArray.length(); i++) {
            JSONObject cpJsonObject = cpJsonArray.getJSONObject(i);
            if (cpJsonObject.get(kAPMCMAreas).toString().contains(",")) {
                returnCp.put(
                    cpJsonObject.get(kAPMCMLoc).toString(),
                    cpJsonObject.get(kAPMCMAreas).toString().split(",")[1] + "|"
                            + cpJsonObject.get(kAPMCMCountry) + "|" + cpJsonObject.get(kAPMCMCity));
            } else {
                returnCp.put(
                    cpJsonObject.get(kAPMCMLoc).toString(),
                    cpJsonObject.get(kAPMCMAreas) + "|" + cpJsonObject.get(kAPMCMCountry)
                    + "|" + cpJsonObject.get(kAPMCMCity));
            }
        }

        return returnCp;
    }

    /**
     * Get the rules (monitors) from the App Synthetic Monitor API.
     * @param folder list of folders
     * @param rulesList list of rules
     * @return list of rules/monitors
     * @throws Exception errors
     */
    private String[] getRules(String folder, String rulesList) throws Exception {
        List<String> ruleQueryOutput = new ArrayList<String>();
        String folderStr = EMPTY_STRING;
        if (!folder.equals(ROOT_FOLDER)) {
            folderStr = kAPMCMFolderParam + folder;
        } else {
            folder = EMPTY_STRING;
        }

        String ruleRequest = cloudMonitorAccessor.executeApi(kAPMCMRuleCmd,
            getCommandString() + folderStr);

        JSONArray ruleJsonArray = extractJsonArray(ruleRequest, kAPMCMRules);

        for (int i = 0; i < ruleJsonArray.length(); i++) {
            JSONObject thisRuleJsonObject = ruleJsonArray.getJSONObject(i);
            if (!thisRuleJsonObject.optString(kAPMCMFolder, EMPTY_STRING).equals(folder)) {
                continue;
            }
            if ((!thisRuleJsonObject.optString(kAPMCMActive, NO).equals(YES))
                    && (this.apmcmProperties.getProperty(SKIP_INACTIVE_FOLDERS, FALSE)
                            .equals(TRUE))) {
                continue;
            }
            ruleQueryOutput.add(thisRuleJsonObject.getString(kAPMCMName));
        }

        if (!rulesList.equals(ALL_RULES)) {
            return compareList(ruleQueryOutput, rulesList);
        }
        return (String[]) ruleQueryOutput.toArray(kNoStringArrayProperties);
    }

    /**
     * Get the folders and rules (monitors) from the App Synthetic Monitor API.
     * @param folders list of folders
     * @return map of folders and rules
     * @throws Exception errors
     */
    public HashMap<String, String[]> getFoldersAndRules(String[] folders) throws Exception {
        HashMap<String, String[]> foldersAndRules = new HashMap<String, String[]>();


        for (int i = 0; i < folders.length; i++) {
            String thisFolderProp = apmcmProperties.getProperty(FOLDER_PREFIX + folders[i],
                EMPTY_STRING);
            String[] rules;
            if ((thisFolderProp.length() == 0) || (thisFolderProp.equals(ALL_RULES))) {
                String[] allRules = getRules(folders[i], ALL_RULES);
                rules = new String[allRules.length + 1];
                rules[0] = ALL_RULES;
                for (int j = 0; j < allRules.length; j++) {
                    rules[(j + 1)] = allRules[j];
                }
            } else {
                rules = getRules(folders[i], thisFolderProp);
            }
            if (rules.length > 0) {
                foldersAndRules.put(folders[i], rules);
            }
        }
        return foldersAndRules;
    }

    /**
     * Get statistics for folder and rule.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param rule gets all rules if empty
     * @return API result
     * @throws Exception errors
     */
    public String getStats(String folder, String rule) throws Exception {
        String statsRequest = EMPTY_STRING;
        String folderStr = EMPTY_STRING;
        String ruleStr = EMPTY_STRING;

        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = kAPMCMFolderParam + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule.length() != 0) {
            ruleStr = kAPMCMNameParam + rule;
            folder = folder + "|" + rule;
        }

        String statsStr = kAPMCMNKeyParam + this.nkey + kAPMCMAccountParam + apmcmUser
                + folderStr + ruleStr + kAPMCMStartDateParam
                + getTodaysDate() + kAPMCMCallbackParam + apmcmCallback;
        statsRequest = cloudMonitorAccessor.executeApi(kAPMCMStatsCmd, statsStr);
        return statsRequest;
    }

    /**
     * Get PSP information for folder and rule.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param rule gets all rules if empty
     * @return API result
     * @throws Exception errors
     */
    public String getPsp(String folder, String rule) throws Exception {
        String pspRequest = EMPTY_STRING;
        String folderStr = EMPTY_STRING;
        String ruleStr = EMPTY_STRING;

        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = kAPMCMFolderParam + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule.length() != 0) {
            ruleStr = kAPMCMNameParam + rule;
        }

        pspRequest = cloudMonitorAccessor.executeApi(kAPMCMPSPCmd, getCommandString()
            + folderStr + ruleStr);
        return pspRequest;
    }

    /**
     * Get logs for folder and rule.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param rule gets all rules if empty
     * @param numRules number of rules in folder
     * @return API result
     * @throws Exception errors
     */
    public String getLogs(String folder, String rule, int numRules) throws Exception {
        String logRequest = EMPTY_STRING;
        String folderStr = EMPTY_STRING;
        String ruleStr = EMPTY_STRING;
        int numLogs = Integer.parseInt(this.apmcmProperties.getProperty(NUM_LOGS)) * numRules;
        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = kAPMCMFolderParam + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule.length() != 0) {
            ruleStr = kAPMCMNameParam + rule;
        }
        String logStr = kAPMCMNKeyParam + this.nkey + folderStr + ruleStr
                + kAPMCMNumParam + numLogs + kAPMCMReverseParam + kAPMCMCallbackParam 
                + apmcmCallback + kAPMCMFullParam;
        //    String logStr = "nkey=" + this.nkey + folderStr + ruleStr
        //        + "&num=" + numLogs + "&reverse=y&full=y";

        logRequest = cloudMonitorAccessor.executeApi(kAPMCMLogsCmd, logStr);
        return logRequest;
    }

    /**
     * Get today's date.
     * @return today's date
     * @throws Exception errors
     */
    private static String getTodaysDate() throws Exception {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }
}
