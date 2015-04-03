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

import com.ca.apm.swat.epaplugins.asm.error.InitializationError;
import com.ca.apm.swat.epaplugins.asm.rules.Rule;
import com.ca.apm.swat.epaplugins.asm.rules.RuleFactory;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.AsmPropertiesImpl;
import com.ca.apm.swat.epaplugins.utils.StringFilter;
import com.ca.apm.swat.epaplugins.utils.TextNormalizer;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Interface to App Synthetic Monitor API.
 */
public class AsmRequestHelper implements AsmProperties {

    private AsmAccessor accessor;
    private Properties properties;
    private String nkey;
    private String user;
    private HashMap<String, String> checkpointMap;

    /**
     * Create new CloudMonitorRequestHelper.
     * @param accessor accessor
     * @param properties properties
     */
    public AsmRequestHelper(AsmAccessor accessor,
                                     Properties properties) {
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
     * Properties like asm.includeFolders and asm.excludeFolders are taken
     * into account.
     * @return array of folders to monitor
     * @throws Exception errors
     */
    public String[] getFolders() throws Exception {
        String includeFolders = properties.getProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
        String excludeFolders = properties.getProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
        String[] folders;

        if ((includeFolders.length() == 0) || (includeFolders.contains(ALL_FOLDERS))) {
            folders = getFolders(ALL_FOLDERS, excludeFolders);
        } else {
            folders = getFolders(includeFolders, excludeFolders);
        }

        return folders;
    }

    /**
     * Get the folders to monitor.
     * @param folderList comma-separated list of folders to query or
     * {@link AsmProperties#ALL_FOLDERS}
     * @param excludeList comma-separated list of folders to exclude
     * @return array of folders to monitor
     * @throws Exception errors
     */
    private String[] getFolders(String folderList, String excludeList) throws Exception {
        List<String> folderQueryOutput = new ArrayList<String>();
        String folderRequest = accessor.executeApi(FOLDER_CMD, getCommandString());

        JSONArray folderJsonArray = extractJsonArray(folderRequest, FOLDERS_TAG);

        folderQueryOutput.add(ROOT_FOLDER);
        for (int i = 0; i < folderJsonArray.length(); i++) {
            JSONObject folderJsonObject = folderJsonArray.getJSONObject(i);

            if ((TRUE.equals(this.properties.getProperty(SKIP_INACTIVE_FOLDERS, FALSE)))
                    && (!YES.equals(folderJsonObject.optString(ACTIVE_TAG, NO)))) {
                if (EpaUtils.getFeedback().isVerboseEnabled()) {
                    EpaUtils.getFeedback().verbose(AsmMessages.getMessage(AsmMessages.SKIP_FOLDER,
                        folderJsonObject.getString(NAME_TAG)));
                }
                continue;
            }
            folderQueryOutput.add(folderJsonObject.get(NAME_TAG).toString());
        }

        if (!folderList.equals(ALL_FOLDERS)) {
            folderQueryOutput = matchList(folderQueryOutput, folderList);
        }

        if (!excludeList.equals(EMPTY_STRING)) {
            folderQueryOutput = removeList(folderQueryOutput, excludeList);
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
        JSONObject entireJsonObject = new JSONObject(metricInput);
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
     * @param <T> a type that can be compared to a string,
     *     i.e. implements <code>equals(String s)</code>
     * @param masterList master list
     * @param comparisonString comma-separated string of entries to match
     * @return reduced list matching <code>comparisonString</code>
     */
    private <T> List<T> matchList(List<T> masterList, String comparisonString) {
        List<String> checkList = Arrays.asList(comparisonString.split(","));
        masterList.retainAll(checkList);
        return masterList;
    }

    /**
     * Remove from a list all entries that match an item in the <code>removeString</code>.
     * All list entries that are matched in the removeString are removed from the list.
     * @param masterList master list
     * @param removeString comma-separated string of entries to remove
     * @return reduced list
     */
    private List<String> removeList(List<String> masterList, String removeString) {
        List<String> checkList = Arrays.asList(removeString.split(","));
        masterList.removeAll(checkList);
        return masterList;
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
            throw new InitializationError(
                AsmMessages.getMessage(AsmMessages.NORMALIZER_INFO, e.getMessage()));
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

        this.checkpointMap = returnCp;
        return returnCp;
    }


    /**
     * Get the rules (monitors) from the App Synthetic Monitor API.
     * @param folder list of folders
     * @param rulesList list of rules
     * @return list of rules/monitors
     * @throws Exception errors
     */
    private List<Rule> getRules(String folder, String rulesList) throws Exception {
        List<Rule> rules = new ArrayList<Rule>();
        String folderStr = EMPTY_STRING;
        if (folder.equals(ROOT_FOLDER)) {
            folder = EMPTY_STRING; // for later comparison
        } else {
            folderStr = FOLDER_PARAM + folder;
        }

        String ruleRequest = accessor.executeApi(RULE_CMD, getCommandString() + folderStr);

        JSONArray ruleJsonArray = extractJsonArray(ruleRequest, RULES_TAG);

        for (int i = 0; i < ruleJsonArray.length(); i++) {
            JSONObject ruleJsonObject = ruleJsonArray.getJSONObject(i);
            if (!ruleJsonObject.optString(FOLDER_TAG, EMPTY_STRING).equals(folder)) {
                continue;
            }

            if (EpaUtils.getFeedback().isVerboseEnabled()) {
                EpaUtils.getFeedback().verbose(
                    "found rule '" + ruleJsonObject.getString(NAME_TAG)
                    + "' of type " + ruleJsonObject.getString(TYPE_TAG)
                    + " in folder " + (ruleJsonObject.isNull(FOLDER_TAG) ? ROOT_FOLDER :
                        ruleJsonObject.getString(FOLDER_TAG)));
            }

            if ((TRUE.equals(this.properties.getProperty(SKIP_INACTIVE_MONITORS, FALSE)))
                    && (!YES.equals(ruleJsonObject.optString(ACTIVE_TAG, NO)))) {
                if (EpaUtils.getFeedback().isVerboseEnabled()) {
                    EpaUtils.getFeedback().verbose(AsmMessages.getMessage(AsmMessages.SKIP_MONITOR,
                        ruleJsonObject.getString(NAME_TAG),
                        folder.length() > 0 ? folder : ROOT_FOLDER));
                }
                continue;
            }
            rules.add(RuleFactory.getRule(
                ruleJsonObject.getString(NAME_TAG),
                ruleJsonObject.getString(TYPE_TAG),
                ruleJsonObject.isNull(FOLDER_TAG) ? EMPTY_STRING :
                    ruleJsonObject.getString(FOLDER_TAG),
                    ruleJsonObject.isNull(TAGS_TAG) ? EMPTY_STRING_ARRAY :
                        ruleJsonObject.getString(TAGS_TAG).split(",")));
        }

        if (!rulesList.equals(ALL_RULES)) {
            rules = matchList(rules, rulesList);
        }

        return rules;
    }

    /**
     * Get the folders and rules (monitors) from the App Synthetic Monitor API.
     * Properties like asm.skipInactiveMonitors are taken into account.
     * @param folders list of folders
     * @return map of folders and rules
     * @throws Exception errors
     */
    public HashMap<String, List<Rule>> getFoldersAndRules(String[] folders) throws Exception {
        HashMap<String, List<Rule>> foldersAndRules = new HashMap<String, List<Rule>>();


        for (int i = 0; i < folders.length; i++) {
            String folderProp = properties.getProperty(FOLDER_PREFIX + folders[i], ALL_RULES);
            List<Rule> rules;
            if (((folderProp.length() == 0) || (folderProp.equals(ALL_RULES)))
                    // if we skip inactive monitors we can't use ALL_RULES
                    && (!TRUE.equals(properties.getProperty(SKIP_INACTIVE_MONITORS, FALSE)))) {
                rules = getRules(folders[i], ALL_RULES);
                rules.add(0, RuleFactory.getAllRulesRule());
            } else {
                rules = getRules(folders[i], folderProp);
            }
            // must be at least one rule != ALL_RULES
            if (((rules.size() > 0) && !rules.get(0).equals(ALL_RULES)) || (rules.size() > 1))  {
                foldersAndRules.put(folders[i], rules);
            }
        }
        return foldersAndRules;
    }

    /**
     * Get statistics for folder and rule.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param rule gets all rules if null
     * @return metric map
     * @throws Exception errors
     */
    public HashMap<String, String> getStats(String folder, Rule rule, String metricPrefix)
            throws Exception {
        String statsRequest = EMPTY_STRING;
        String folderStr = EMPTY_STRING;
        String ruleStr = EMPTY_STRING;

        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule != null) {
            ruleStr = NAME_PARAM + rule.getName();
            folder = folder + "|" + rule.getName();
        } else {
            rule = RuleFactory.getAllRulesRule();
        }

        String statsStr = NKEY_PARAM + this.nkey + ACCOUNT_PARAM + this.user
                + folderStr + ruleStr + START_DATE_PARAM
                + getTodaysDate() + CALLBACK_PARAM + DO_CALLBACK;
        statsRequest = accessor.executeApi(STATS_CMD, statsStr);

        return rule.generateMetrics(statsRequest, metricPrefix, properties, checkpointMap);
    }

    /**
     * Get PSP information for folder and rule.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param rule gets all rules if null
     * @return metric map
     * @throws Exception errors
     */
    public HashMap<String, String> getPsp(String folder, Rule rule, String metricPrefix)
            throws Exception {
        String pspRequest = EMPTY_STRING;
        String folderStr = EMPTY_STRING;
        String ruleStr = EMPTY_STRING;

        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = ROOT_FOLDER;
        }

        if (rule != null) {
            ruleStr = NAME_PARAM + rule.getName();
        } else {
            rule = RuleFactory.getAllRulesRule();
        }

        pspRequest = accessor.executeApi(PSP_CMD, getCommandString()
            + folderStr + ruleStr);
        return rule.generateMetrics(pspRequest, metricPrefix, properties, checkpointMap);
    }

    /**
     * Get logs for folder and rule.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param rule gets all rules if null
     * @param numRules number of rules in folder
     * @return metric map
     * @throws Exception errors
     */
    public HashMap<String, String> getLogs(String folder,
        Rule rule,
        int numRules,
        String metricPrefix) throws Exception {

        String logRequest = EMPTY_STRING;
        String folderStr = EMPTY_STRING;
        String ruleStr = EMPTY_STRING;
        int numLogs = Integer.parseInt(this.properties.getProperty(NUM_LOGS)) * numRules;
        if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
            folderStr = FOLDER_PARAM + folder;
        } else {
            folder = ROOT_FOLDER;
            rule = RuleFactory.getAllRulesRule();
        }

        if (rule != null) {
            ruleStr = NAME_PARAM + rule.getName();
        }
        String logStr = NKEY_PARAM + this.nkey + folderStr + ruleStr
                + NUM_PARAM + numLogs + REVERSE_PARAM + CALLBACK_PARAM 
                + DO_CALLBACK + FULL_PARAM;
        //    String logStr = "nkey=" + this.nkey + folderStr + ruleStr
        //        + "&num=" + numLogs + "&reverse=y&full=y";

        logRequest = accessor.executeApi(LOGS_CMD, logStr);

        return rule.generateMetrics(logRequest, metricPrefix, properties, checkpointMap);
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
