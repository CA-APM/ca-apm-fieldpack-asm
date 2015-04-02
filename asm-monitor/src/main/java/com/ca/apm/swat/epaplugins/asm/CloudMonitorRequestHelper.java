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

    private CloudMonitorAccessor accessor;
    private Properties properties;
    private String nkey;
    private String user;

    /**
     * Create new CloudMonitorRequestHelper.
     * @param accessor accessor
     * @param properties properties
     */
    public CloudMonitorRequestHelper(CloudMonitorAccessor accessor, Properties properties) {
        this.accessor = accessor;
        this.properties = properties;
        this.user = properties.getProperty(USER);
    }

    /**
     * Connect to App Synthetic Monitor API.
     * @throws Exception errors
     */
    public void connect() throws Exception {
        this.nkey = accessor.login();
    }

    /**
     * Get the folders to monitor.
     * @return array of folders to monitor
     * @throws Exception errors
     */
    public String[] getFolders() throws Exception {
        String folderProperty = properties.getProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
        String[] folders;

        if ((folderProperty.length() == 0) || (folderProperty.contains(ALL_FOLDERS))) {
            folders = getFolders(ALL_FOLDERS);
        } else {
            folders = getFolders(folderProperty);
        }

        return folders;
    }

    /**
     * Get the folders to monitor.
     * @param folderList list of folders to query or {@link AsmProperties#ALL_FOLDERS}
     * @return array of folders to monitor
     * @throws Exception errors
     */
    private String[] getFolders(String folderList) throws Exception {
        List<String> folderQueryOutput = new ArrayList<String>();
        String folderRequest = accessor.executeApi(FOLDER_CMD, getCommandString());

        JSONArray folderJsonArray = extractJsonArray(folderRequest, FOLDERS_TAG);

        folderQueryOutput.add(ROOT_FOLDER);
        for (int i = 0; i < folderJsonArray.length(); i++) {
            JSONObject folderJsonObject = folderJsonArray.getJSONObject(i);

            if ((!folderJsonObject.optString(ACTIVE_TAG, NO).equals(YES))
                    && (properties.getProperty(SKIP_INACTIVE_FOLDERS, NO).equals(TRUE))) {
                continue;
            }
            folderQueryOutput.add(folderJsonObject.get(NAME_TAG).toString());
        }

        if (!folderList.equals(ALL_FOLDERS)) {
            return compareList(folderQueryOutput, folderList);
        }
        return (String[]) folderQueryOutput.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Get the command string.
     */
    private String getCommandString() {
        return NKEY_PARAM + this.nkey + CALLBACK_PARAM + DO_CALLBACK;
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

        if (entireJsonObject.optJSONObject(RESULT_TAG) != null) {
            JSONObject resultJsonObject = entireJsonObject.getJSONObject(RESULT_TAG);

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
        return (String[]) masterList.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Get the credits from the App Synthetic Monitor API.
     * @return metric map for credits
     * @throws Exception errors
     */
    public HashMap<String, String> getCredits() throws Exception {
        HashMap<String, String> metricMap = new HashMap<String, String>();
        String creditsRequest = EMPTY_STRING;
        creditsRequest = accessor.executeApi(CREDITS_CMD, getCommandString());

        JSONArray creditJsonArray = extractJsonArray(creditsRequest, CREDITS_TAG);

        for (int i = 0; i < creditJsonArray.length(); i++) {
            JSONObject creditJsonObject = creditJsonArray.getJSONObject(i);

            String key = creditJsonObject.optString(TYPE_TAG, NO_TYPE);
            String value = creditJsonObject.optString(AVAILABLE_TAG, ZERO);

            if (AsmPropertiesImpl.ASM_METRICS.containsKey(key)) {
                key = ((String) AsmPropertiesImpl.ASM_METRICS.get(key)).toString();
            }

            String rawMetric = CREDITS_CATEGORY + METRIC_NAME_SEPARATOR + key;
            metricMap.put(fixMetric(rawMetric), fixMetric(value));
        }

        return metricMap;
    }

    /**
     * Replace unsupported characters in metric name.
     * @param rawMetric raw metric name
     * @return cleansed metric name
     */
    public static String fixMetric(String rawMetric) {
        StringFilter normalizer = null;
        try {
            normalizer = TextNormalizer.getNormalizationStringFilter();
        } catch (ClassNotFoundException e) {
            EpaUtils.getFeedback().error(e.getMessage());
            System.err.print(e.getMessage());
            System.exit(NORM_ERROR_CODE);
        }

        String metricKeyNormalized = normalizer.filter(rawMetric);
        Pattern pattern = Pattern.compile(JSON_PATTERN);
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

        String cpRequest = accessor.executeApi(CHECKPOINTS_CMD, getCommandString());

        JSONArray cpJsonArray = extractJsonArray(cpRequest, CHECKPOINTS_TAG);

        for (int i = 0; i < cpJsonArray.length(); i++) {
            JSONObject cpJsonObject = cpJsonArray.getJSONObject(i);
            if (cpJsonObject.get(AREA_TAG).toString().contains(DEFAULT_DELIMITER)) {
                returnCp.put(
                    cpJsonObject.get(LOCATION_TAG).toString(),
                    cpJsonObject.get(AREA_TAG).toString().split(DEFAULT_DELIMITER)[1] 
                            + METRIC_PATH_SEPARATOR + cpJsonObject.get(COUNTRY_TAG)
                            + METRIC_PATH_SEPARATOR + cpJsonObject.get(CITY_TAG));
            } else {
                returnCp.put(
                    cpJsonObject.get(LOCATION_TAG).toString(),
                    cpJsonObject.get(AREA_TAG)
                    + METRIC_PATH_SEPARATOR + cpJsonObject.get(COUNTRY_TAG)
                    + METRIC_PATH_SEPARATOR + cpJsonObject.get(CITY_TAG));
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
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = EMPTY_STRING;
        }

        String ruleRequest = accessor.executeApi(RULE_CMD,
            getCommandString() + folderStr);

        JSONArray ruleJsonArray = extractJsonArray(ruleRequest, RULES_TAG);

        for (int i = 0; i < ruleJsonArray.length(); i++) {
            JSONObject ruleJsonObject = ruleJsonArray.getJSONObject(i);
            if (!ruleJsonObject.optString(FOLDER_TAG, EMPTY_STRING).equals(folder)) {
                continue;
            }
            if ((!ruleJsonObject.optString(ACTIVE_TAG, NO).equals(YES))
                    && (this.properties.getProperty(SKIP_INACTIVE_FOLDERS, FALSE)
                            .equals(TRUE))) {
                continue;
            }
            ruleQueryOutput.add(ruleJsonObject.getString(NAME_TAG));
        }

        if (!rulesList.equals(ALL_RULES)) {
            return compareList(ruleQueryOutput, rulesList);
        }
        return (String[]) ruleQueryOutput.toArray(EMPTY_STRING_ARRAY);
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
            String folderProp = properties.getProperty(FOLDER_PREFIX + folders[i], ALL_RULES);
            String[] rules;
            if ((folderProp.length() == 0) || (folderProp.equals(ALL_RULES))) {
                String[] allRules = getRules(folders[i], ALL_RULES);
                rules = new String[allRules.length + 1];
                rules[0] = ALL_RULES;
                for (int j = 0; j < allRules.length; j++) {
                    rules[(j + 1)] = allRules[j];
                }
            } else {
                rules = getRules(folders[i], folderProp);
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
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule.length() != 0) {
            ruleStr = NAME_PARAM + rule;
            folder = folder + "|" + rule;
        }

        String statsStr = NKEY_PARAM + this.nkey + ACCOUNT_PARAM + this.user
                + folderStr + ruleStr + START_DATE_PARAM
                + getTodaysDate() + CALLBACK_PARAM + DO_CALLBACK;
        statsRequest = accessor.executeApi(STATS_CMD, statsStr);
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
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule.length() != 0) {
            ruleStr = NAME_PARAM + rule;
        }

        pspRequest = accessor.executeApi(PSP_CMD, getCommandString()
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
        int numLogs = Integer.parseInt(this.properties.getProperty(NUM_LOGS)) * numRules;
        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule.length() != 0) {
            ruleStr = NAME_PARAM + rule;
        }
        String logStr = NKEY_PARAM + this.nkey + folderStr + ruleStr
                + NUM_PARAM + numLogs + REVERSE_PARAM + CALLBACK_PARAM 
                + DO_CALLBACK + FULL_PARAM;
        //    String logStr = "nkey=" + this.nkey + folderStr + ruleStr
        //        + "&num=" + numLogs + "&reverse=y&full=y";

        logRequest = accessor.executeApi(LOGS_CMD, logStr);
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
