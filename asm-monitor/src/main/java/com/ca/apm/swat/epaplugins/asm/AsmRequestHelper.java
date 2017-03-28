package com.ca.apm.swat.epaplugins.asm;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMapReplacing;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.AsmPropertiesImpl;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;
import java.util.Map;

/**
 * Interface to App Synthetic Monitor API.
 */
public class AsmRequestHelper implements AsmProperties {

    private Accessor accessor;
    private String nkey;
    private String user;
    private static HashMap<String, String> stationMap;
    private static HashMap<String, Long> apiCallMap = null;
    private static HashMap<String, HashMap<String, Long>> objectApiCallMap = null;
    private long lastPrintApiTimestamp = 0;
    private static final long PRINT_API_INTERVAL = 900000; // 15 minutes

    /**
     * Create new CloudMonitorRequestHelper.
     * @param accessor accessor
     */
    public AsmRequestHelper(Accessor accessor) {
        this.accessor = accessor;
        this.user = EpaUtils.getProperty(USER);

        if (null == apiCallMap) {
            apiCallMap = new HashMap<String, Long>();
        }

        if (null == objectApiCallMap) {
            objectApiCallMap = new HashMap<String, HashMap<String, Long>>();
        }
    }

    /**
     * Get the global monitoring station map.
     * @return the monitoring station map
     */
    public static HashMap<String, String> getMonitoringStationMap() {
        return stationMap;
    }


    /**
     * Connect to App Synthetic Monitor API.
     * @throws Exception errors
     */
    public void connect() throws Exception {
        this.nkey = accessor.login();
    }

    /**
     * Count API calls per object (folder or monitor).
     * @param cmd API command called
     */
    private void countApiCall(String cmd) {
        Long count = apiCallMap.get(cmd);
        if (null == count) {
            apiCallMap.put(cmd, new Long(1));
        } else {
            apiCallMap.put(cmd, new Long(count.longValue() + 1));
        }
    }

    /**
     * Count API calls per object (folder or monitor).
     * @param cmd API command called
     * @param name of the object (folder or monitor)
     */
    private void countApiCall(String cmd, String name) {
        HashMap<String, Long> map = objectApiCallMap.get(name);

        if (null == map) {
            map = new HashMap<String, Long>();
            map.put(cmd, new Long(1));
            objectApiCallMap.put(name, map);
        } else {
            Long count = map.get(cmd);
            if (null == count) {
                map.put(cmd, new Long(1));
            } else {
                map.put(cmd, new Long(count.intValue() + 1));
            }
        }
    }

    /**
     * Write the API call statistics to the log.
     * Statistics are reset to 0 each day.
     */
    public void printApiCallStatistics() {
        if (EpaUtils.getBooleanProperty(PRINT_API_STATISTICS, false)) {
            long sum = 0;
            long count = 0;
            boolean resetValues = false;
            final Date now = new Date();

            // determine if a new day and we have to reset stats to 0
            Calendar today = GregorianCalendar.getInstance();
            today.setTime(now);

            Calendar before = GregorianCalendar.getInstance();
            before.setTimeInMillis(lastPrintApiTimestamp);

            if (today.get(Calendar.DAY_OF_MONTH) != before.get(Calendar.DAY_OF_MONTH)) {
                resetValues = true;
            }

            long timeElapsed = now.getTime() - lastPrintApiTimestamp;
            if (PRINT_API_INTERVAL < timeElapsed) {
                lastPrintApiTimestamp = now.getTime();
                Module module = new Module(Thread.currentThread().getName());

                EpaUtils.getFeedback().info(module,
                    AsmMessages.getMessage(AsmMessages.API_CALL_STATS_502));

                for (Iterator<String> it = apiCallMap.keySet().iterator(); it.hasNext(); ) {
                    String cmd = it.next();
                    count = apiCallMap.get(cmd);
                    if (resetValues) {
                        apiCallMap.put(cmd, Long.valueOf(0));
                    }
                    sum += count;
                    EpaUtils.getFeedback().info(module,"  " + cmd + " = " + count);
                }

                for (Iterator<String> it = objectApiCallMap.keySet().iterator(); it.hasNext(); ) {
                    String name = it.next();
                    HashMap<String, Long> map = objectApiCallMap.get(name);

                    StringBuffer buf = new StringBuffer();
                    count = 0;
                    for (Iterator<String> mit = map.keySet().iterator(); mit.hasNext(); ) {
                        String cmd = mit.next();
                        buf.append(map.get(cmd)).append(' ').append(cmd).append(',');
                        count += map.get(cmd);
                        if (resetValues) {
                            map.put(cmd, Long.valueOf(0));
                        }
                    }
                    sum += count;
                    EpaUtils.getFeedback().info(module,"  " + name + " = " + count
                        + " (" + buf + ")");
                }

                EpaUtils.getFeedback().info(module,"  sum = " + sum);
            }
        }
    }

    /**
     * Get the folders to monitor.
     * Properties like asm.includeFolders and asm.excludeFolders are taken
     * into account.
     * @return array of folders to monitor
     * @throws Exception errors
     */
    public String[] getFolders() throws Exception {
        String includeFolders = EpaUtils.getProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
        String excludeFolders = EpaUtils.getProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
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

        try {

            String folderRequest = accessor.executeApi(FOLDER_CMD, getCommandString());
            countApiCall(FOLDER_CMD);

            JSONArray folderJsonArray = extractJsonArray(folderRequest, FOLDERS_TAG);
            Module module = new Module(Thread.currentThread().getName());

            folderQueryOutput.add(ROOT_FOLDER);
            for (int i = 0; i < folderJsonArray.length(); i++) {
                JSONObject folderJsonObject = folderJsonArray.getJSONObject(i);

                if ((EpaUtils.getBooleanProperty(SKIP_INACTIVE_FOLDERS, false))
                        && (!YES.equals(folderJsonObject.optString(ACTIVE_TAG, NO)))) {
                    if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                        EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
                            AsmMessages.SKIP_FOLDER_305,
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
        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getFolders", e.getMessage()));
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
        JSONArray thisJsonArray = new JSONArray();

        try {
            JSONObject entireJsonObject = new JSONObject(metricInput);

            if (entireJsonObject.optJSONObject(RESULT_TAG) != null) {
                JSONObject resultJsonObject = entireJsonObject.getJSONObject(RESULT_TAG);

                if (resultJsonObject.optJSONArray(arrayName) != null) {
                    thisJsonArray = resultJsonObject.optJSONArray(arrayName);
                }
            }

        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "extractJsonArray", e.getMessage()));
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
        List<String> checkList = Arrays.asList(comparisonString.split(", *"));
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
        List<String> checkList = Arrays.asList(removeString.split(", *"));
        masterList.removeAll(checkList);
        return masterList;
    }

    /**
     * Get the credits from the App Synthetic Monitor API.
     * @return metric map for credits
     * @throws Exception errors
     */
    public HashMap<String, String> getCredits() throws Exception {
        MetricMap metricMap = new MetricMap();

        try {
            String creditsRequest = EMPTY_STRING;
            creditsRequest = accessor.executeApi(CREDITS_CMD, getCommandString());
            countApiCall(CREDITS_CMD);

            JSONArray creditJsonArray = extractJsonArray(creditsRequest, CREDITS_TAG);

            for (int i = 0; i < creditJsonArray.length(); i++) {
                JSONObject creditJsonObject = creditJsonArray.getJSONObject(i);

                String key = creditJsonObject.optString(TYPE_TAG, NO_TYPE);
                String value = creditJsonObject.optString(AVAILABLE_TAG, ZERO);

                if (AsmPropertiesImpl.ASM_METRICS.containsKey(key)) {
                    key = ((String) AsmPropertiesImpl.ASM_METRICS.get(key)).toString();
                }

                String rawMetric = CREDITS_CATEGORY + METRIC_NAME_SEPARATOR + key;
                metricMap.put(rawMetric, value);
            }
        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getCredits", e.getMessage()));
        }

        return metricMap;
    }

    /**
     * Get the monitoring stations from the App Synthetic Monitor API.
     * @return map of monitoring stations
     * @throws Exception errors
     */
    public HashMap<String, String> getMonitoringStations() throws Exception {
        HashMap<String, String> stationMap = new HashMap<String, String>();

        try {
            String cpRequest = accessor.executeApi(STATIONS_GET_CMD, getCommandString());
            countApiCall(STATIONS_GET_CMD);

            JSONArray cpJsonArray = extractJsonArray(cpRequest, CHECKPOINTS_TAG);

            for (int i = 0; i < cpJsonArray.length(); i++) {
                JSONObject cpJsonObject = cpJsonArray.getJSONObject(i);
                if (cpJsonObject.get(AREA_TAG).toString().contains(DEFAULT_DELIMITER)) {
                    stationMap.put(
                        cpJsonObject.get(LOCATION_TAG).toString(),
                        cpJsonObject.get(AREA_TAG).toString().split(DEFAULT_DELIMITER)[1] 
                                + METRIC_PATH_SEPARATOR + cpJsonObject.get(COUNTRY_TAG)
                                + METRIC_PATH_SEPARATOR + cpJsonObject.get(CITY_TAG));
                } else {
                    stationMap.put(
                        cpJsonObject.get(LOCATION_TAG).toString(),
                        cpJsonObject.get(AREA_TAG)
                        + METRIC_PATH_SEPARATOR + cpJsonObject.get(COUNTRY_TAG)
                        + METRIC_PATH_SEPARATOR + cpJsonObject.get(CITY_TAG));
                }
            }
        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getMonitoringStations", e.getMessage()));
        }

        AsmRequestHelper.stationMap = stationMap;
        return stationMap;
    }


    /**
     * Get the monitors (monitors) from the App Synthetic Monitor API.
     * @param folder list of folders
     * @param monitorsList list of monitors
     * @return list of monitors/monitors
     * @throws Exception errors
     */
    private List<Monitor> getMonitors(String folder, String monitorsList) throws Exception {
        List<Monitor> monitors = new ArrayList<Monitor>();

        try {
            String folderStr = EMPTY_STRING;
            if (folder.equals(ROOT_FOLDER)) {
                folder = EMPTY_STRING; // for later comparison
            } else {
                folderStr = FOLDER_PARAM + URLEncoder.encode(folder, EpaUtils.getEncoding());
            }

            String monitorRequest = accessor.executeApi(MONITOR_GET_CMD,
                getCommandString() + folderStr);
            countApiCall(MONITOR_GET_CMD, folder);

            JSONArray monitorJsonArray = extractJsonArray(monitorRequest, RULES_TAG);
            Module module = new Module(Thread.currentThread().getName());

            for (int i = 0; i < monitorJsonArray.length(); i++) {
                try {
                    JSONObject monitorJsonObject = monitorJsonArray.getJSONObject(i);
                    if (!monitorJsonObject.optString(FOLDER_TAG, EMPTY_STRING).equals(folder)) {
                        continue;
                    }

                    if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                        EpaUtils.getFeedback().verbose(module,
                            AsmMessages.getMessage(AsmMessages.READ_MONITOR_307, 
                                monitorJsonObject.getString(NAME_TAG),
                                monitorJsonObject.getString(TYPE_TAG),
                                (monitorJsonObject.isNull(FOLDER_TAG) ? ROOT_FOLDER :
                                    monitorJsonObject.getString(FOLDER_TAG))));
                    }

                    if ((EpaUtils.getBooleanProperty(SKIP_INACTIVE_MONITORS, false))
                            && (!YES.equals(monitorJsonObject.optString(ACTIVE_TAG, NO)))) {
                        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                            EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
                                AsmMessages.SKIP_MONITOR_308,
                                monitorJsonObject.getString(NAME_TAG),
                                folder.length() > 0 ? folder : ROOT_FOLDER));
                        }
                        // do not skip the inactive monitors, we need that information later!
                        // continue;
                    }

                    String url = "";
                    if (monitorJsonObject.get(HOST_TAG) instanceof String) {
                        url = MonitorFactory.createMonitorUrl(
                            monitorJsonObject.getString(TYPE_TAG),
                            monitorJsonObject.getString(HOST_TAG),
                            monitorJsonObject.getString(PORT_TAG),
                            monitorJsonObject.optString(PATH_TAG, EMPTY_STRING));
                    }

                    monitors.add(MonitorFactory.createMonitor(
                        monitorJsonObject.getString(NAME_TAG),
                        monitorJsonObject.getString(TYPE_TAG),
                        monitorJsonObject.isNull(FOLDER_TAG) ? EMPTY_STRING :
                            monitorJsonObject.getString(FOLDER_TAG),
                            monitorJsonObject.isNull(TAGS_TAG) ? EMPTY_STRING_ARRAY :
                                monitorJsonObject.getString(TAGS_TAG).split(","),
                                url,
                                YES.equals(monitorJsonObject.optString(ACTIVE_TAG, NO))));
                } catch (JSONException e) {
                    EpaUtils.getFeedback().warn(module,
                        AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                            "getMonitors", e.getMessage()));
                }
            }
        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getMonitors", e.getMessage()));
        }

        if (!monitorsList.equals(ALL_MONITORS)) {
            monitors = matchList(monitors, monitorsList);
        }

        return monitors;
    }

    /**
     * Get the folders and monitors (monitors) from the App Synthetic Monitor API.
     * Properties like asm.skipInactiveMonitors are taken into account.
     * @param folders list of folders
     * @return map of folders and monitors
     * @throws Exception errors
     */
    public HashMap<String, List<Monitor>> getMonitors(String[] folders) throws Exception {
        HashMap<String, List<Monitor>> foldersAndMonitors = new HashMap<String, List<Monitor>>();

        try {
            for (int i = 0; i < folders.length; i++) {
                String folderProp = EpaUtils.getProperty(FOLDER_PREFIX + folders[i], ALL_MONITORS);
                List<Monitor> monitors;
                if (((folderProp.length() == 0) || (folderProp.equals(ALL_MONITORS)))
                        // if we skip inactive monitors we can't use ALL_MONITORS
                        && (!EpaUtils.getBooleanProperty(SKIP_INACTIVE_MONITORS, false))) {
                    monitors = getMonitors(folders[i], ALL_MONITORS);
                    monitors.add(0, MonitorFactory.getAllMonitorsMonitor());
                } else {
                    monitors = getMonitors(folders[i], folderProp);
                }
                // must be at least one monitor != ALL_MONITORS
                if (((monitors.size() > 0) && (!monitors.get(0).equals(ALL_MONITORS)))
                        || (monitors.size() > 1))  {
                    foldersAndMonitors.put(folders[i], monitors);
                }
            }
        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getMonitors", e.getMessage()));
        }

        return foldersAndMonitors;
    }

    /**
     * Get statistics for folder and monitor.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param metricPrefix prefix for metrics
     * @param aggregate aggregate folder stats?
     * @return metric map
     * @throws Exception if an error occurred
     */
    public Map<String, String> getStats(String folder, String metricPrefix, boolean aggregate)
            throws Exception {

        try {
            String aggregateStr = NOT_AGGREGATE_PARAM;
            String folderStr = EMPTY_STRING;

            if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
                folderStr = FOLDER_PARAM + URLEncoder.encode(folder, EpaUtils.getEncoding());
            } else {
                folder = ROOT_FOLDER;
            }

            Monitor monitor = MonitorFactory.getAllMonitorsMonitor();
            countApiCall(STATS_CMD, folder);

            if (aggregate) {
                aggregateStr = AGGREGATE_PARAM;
            }

            String statsStr = NKEY_PARAM + this.nkey + ACCOUNT_PARAM + this.user
                    + folderStr + aggregateStr + START_DATE_PARAM
                    + getTodaysDate() + CALLBACK_PARAM + DO_CALLBACK;
            String statsRequest = accessor.executeApi(STATS_CMD, statsStr);

            Module module = new Module(Thread.currentThread().getName());
            if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
                    AsmMessages.METHOD_FOR_FOLDER_MONITOR_309,
                    "getStats", folder, monitor.getName(), monitor.getType()));
            }

            return monitor.generateMetrics(new MetricMap(), statsRequest, metricPrefix);

        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getStats", e.getMessage()));
        }

        return new HashMap<String, String>();
    }

    /**
     * Get PSP information for folder and monitor.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @return metric map
     * @throws Exception errors
     */
    public Map<String, String> getPsp(String folder, String metricPrefix)
            throws Exception {

        try {
            String pspRequest = EMPTY_STRING;
            String folderStr = EMPTY_STRING;
            String monitorStr = EMPTY_STRING;

            if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
                folderStr = FOLDER_PARAM + URLEncoder.encode(folder, EpaUtils.getEncoding());
            } else {
                folder = ROOT_FOLDER;
            }

            countApiCall(PSP_CMD, folder);
            pspRequest = accessor.executeApi(PSP_CMD, getCommandString()
                + folderStr + monitorStr);

            Module module = new Module(Thread.currentThread().getName());
            if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                EpaUtils.getFeedback().verbose(module,
                    AsmMessages.getMessage(AsmMessages.METHOD_FOR_FOLDER_306,
                        "getPsp", folder));
            }

            Monitor monitor = MonitorFactory.getAllMonitorsMonitor();
            return monitor.generateMetrics(new MetricMap(), pspRequest, metricPrefix);

        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getPsp", e.getMessage()));
        }

        return new HashMap<String, String>();
    }

    /**
     * Get logs for folder and monitor.
     * @param folder defaults to {@link AsmProperties#ROOT_FOLDER}
     * @param numMonitors number of monitors in folder
     * @param metricPrefix
     * @param lastId UUID of last event returned in previous call or null
     * @return metric map
     * @throws Exception errors
     */
    public LogResult getLogs(String folder,
        int numMonitors,
        String metricPrefix, String lastId) throws Exception {

        try {
            String folderStr = EMPTY_STRING;

            if ((folder.length() != 0) && (!folder.equals(ROOT_FOLDER))) {
                folderStr = FOLDER_PARAM + URLEncoder.encode(folder, EpaUtils.getEncoding());
            } else {
                folder = ROOT_FOLDER;
                //TODO: check this again
                //monitor = MonitorFactory.getAllMonitorsMonitor();
            }

            countApiCall(LOGS_CMD, folder);           

            String logStr = NKEY_PARAM + this.nkey + folderStr                    
                    + CALLBACK_PARAM + DO_CALLBACK + FULL_PARAM;

            // only download full data if configured
            if (EpaUtils.getBooleanProperty(METRICS_DOWNLOAD_FULL, false)) {
                logStr += 'y';
                if (!EpaUtils.getBooleanProperty(LEGACY_OUTPUT_FORMAT, true)) {
                    // Use the new output format. 
                    // Output contains URL of the resource, agent downloads it directly from the checkpoint.
                    logStr += NEW_OUTPUT_PARAM;
                }
            }
            Map<String, String> metrics;
            
            if(lastId == null) {
                // get n latest records on the first run
                int numLogs = Integer.parseInt(EpaUtils.getProperty(NUM_LOGS));
                if (numMonitors > 0) {
                    numLogs =  numLogs * numMonitors;
                }
                logStr += REVERSE_PARAM + NUM_PARAM + numLogs;
                metrics = new MetricMap();
            } else {
                // get all records newer than last uuid
                logStr += UUID_PARAM + lastId + NUM_PARAM + Integer.parseInt(EpaUtils.getProperty(MAX_LOG_LIMIT));
                metrics = new MetricMapReplacing();
            }

            long maxRuntime = Long.parseLong(EpaUtils.getProperty(WAIT_TIME)) * 2/3;
            long start = new Date().getTime();
            double d = Double.parseDouble(EpaUtils.getProperty(REQUEST_RETRY_DELAY));
            long delay = Math.round(d + d*(Math.random()-0.5));
            String logResponse;

            while(true) {
                try {
                    logResponse = accessor.executeApi(LOGS_CMD, logStr);
                    break;
                } catch (Exception ex) {
                    if(new Date().getTime() - start > maxRuntime) {
                        throw ex;
                    }
                    // retry API call on failure, after a delay
                    EpaUtils.getFeedback().info(new Module(Thread.currentThread().getName()),
                            AsmMessages.getMessage(AsmMessages.API_RETRY_508, ex.getMessage(), delay));
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ignore) {
                        throw ex;
                    }
                    delay *= 2;
                }
            }

            Module module = new Module(Thread.currentThread().getName());
            if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                EpaUtils.getFeedback().verbose(module,
                    AsmMessages.getMessage(AsmMessages.METHOD_FOR_FOLDER_306,
                        "getLogs", folder));
            }

            // report JMeter steps?
            String monitorType = SCRIPT_MONITOR;
            if (!EpaUtils.getBooleanProperty(REPORT_JMETER_STEPS, true)) {
                monitorType = HTTP_MONITOR;
            }

            Monitor monitor =
                    MonitorFactory.createMonitor("dummy",
                        monitorType,
                        folderStr,
                        null,
                        EMPTY_STRING,
                        false);            
            monitor.generateMetrics(metrics, logResponse, metricPrefix);
            return new LogResult(metrics, metrics.remove(UUID_TAG));

        } catch (JSONException e) {
            EpaUtils.getFeedback().warn(new Module(Thread.currentThread().getName()),
                AsmMessages.getMessage(AsmMessages.JSON_PARSING_ERROR_713,
                    "getLogs", e.getMessage()));
        }

        return new LogResult(new HashMap<String, String>(), null);
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
