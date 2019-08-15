package com.ca.apm.swat.epaplugins.asm.monitor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.ca.apm.swat.epaplugins.asm.AsmReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.asm.AsmRequestHelper;
import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.AsmPropertiesImpl;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;

/**
 * Base class for implementations of the {@link Monitor} interface.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class BaseMonitor extends AbstractMonitor implements AsmProperties {
    private String name = null;
    private String folder = null;
    private String[] tags = null;
    private String type = null;
    private String url = null;
    private boolean active = false;

    protected static Formatter format = Formatter.getInstance();

    // metrics to add to step
    protected static final Set<String> metricsToAddToStep = new HashSet<String>();


    /**
     * Monitor base class.
     * @param successor successor in chain
     * @param name name of the monitor
     * @param type monitor type
     * @param folder folder of the monitor
     * @param tags tags of the monitor
     * @param url URL that is monitored
     * @param active if the monitor is active
     */
    protected BaseMonitor(Handler successor,
                          String name,
                          String type,
                          String folder,
                          String[] tags,
                          String url,
                          boolean active) {
        super(successor);
        this.name = name;
        this.folder = folder;
        this.tags = tags;
        this.type = type;
        this.url = url;
        this.active = active;

        synchronized (metricsToAddToStep) {
            if (metricsToAddToStep.isEmpty()) {
                metricsToAddToStep.add(STATUS_MESSAGE);
                metricsToAddToStep.add(STATUS_MESSAGE_VALUE);
                metricsToAddToStep.add(RESPONSE_CODE);
                metricsToAddToStep.add(RESULT_CODE);
                metricsToAddToStep.add(TEST_URL);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getFolder() {
        return folder;
    }

    public String[] getTags() {
        return tags;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Compare the name of the monitor with a string.
     * Needed to include/exclude monitors by name.
     * @param anotherName string to compare monitor name with
     * @return true if the monitor name equals anotherName
     */
    public boolean equals(String anotherName) {
        return this.name.equals(anotherName);
    }

    /**
     * Compare the names of the monitors.
     * @param anotherMonitor monitor to compare with
     * @return true if the monitor names are equal
     */
    public boolean equals(Monitor anotherMonitor) {
        return this.name.equals(anotherMonitor.getName());
    }


    /**
     * Recursively generate metrics from API call result. 
     * @param metricMap map to insert metrics into
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @return metricMap map containing the metrics
     */
    @SuppressWarnings("rawtypes")
    public Map<String, String> generateMetrics(
                                               Map<String, String> metricMap,
                                               String jsonString,
                                               String metricTree) {

        if (null == jsonString) {
            return metricMap;
        }

        JSONObject jsonObject = new JSONObject(jsonString);
        Module module = new Module(Thread.currentThread().getName());
        int originalMapSize = metricMap.size();
        
        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "generateMetrics(" + metricTree + ", " 
                    + jsonString + ")");
        }

        // return if this monitor is inactive
        if (NO.equals(jsonObject.optString(ACTIVE_TAG, YES))) {
            return metricMap;
        }

        // return if this monitor is in maintenance and REPORT_MAINTENANCE=false
        if (!(EpaUtils.getBooleanProperty(REPORT_MAINTENANCE, true))
            && (3 == jsonObject.optInt(TYPE_TAG, 0))) {
            return metricMap;
        }

        // if we find a name append it to metric tree
        String name = jsonObject.optString(NAME_TAG, null);
        Monitor monitor = null;
        if (name != null) {
            if (null == metricTree) {
                folder = AsmRequestHelper.getFolder(name);
                if (null != folder) {
                    if (folder.equals(EMPTY_STRING)) {
                        metricTree = MONITOR_METRIC_PREFIX.substring(0,
                            MONITOR_METRIC_PREFIX.length() - 1);
                    } else {
                        metricTree = MONITOR_METRIC_PREFIX + folder.replace("|", "");
                    }
                } else {
                    // we don't know the folder, probably a configuration change
                    // e.g. monitor added/activated
                    if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                        EpaUtils.getFeedback().verbose(module,
                                "did not find a folder for monitor " + name + ", skipping it");
                    }
                    return metricMap;
                }
            }

            metricTree = metricTree + METRIC_PATH_SEPARATOR + name.replace("|", "");

            // find the monitor
            monitor = MonitorFactory.findMonitor(name);

            // return if not active
            if ((null != monitor) && (!monitor.isActive())) {
                if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                    EpaUtils.getFeedback().verbose(module,
                                                   "skipping metrics for inactive monitor " + name);
                }
                return metricMap;
            }
        }

        // append monitoring station to metric tree
        if (jsonObject.optString(LOCATION_TAG, null) != null) {
            if (EpaUtils.getBooleanProperty(DISPLAY_STATIONS, true)) {
                String location = AsmRequestHelper.getMonitoringStationMap().get(
                                      jsonObject.getString(LOCATION_TAG));
                if (null == location) {
                    location = OPMS + METRIC_PATH_SEPARATOR + OPMS + METRIC_PATH_SEPARATOR
                            + jsonObject.getString(LOCATION_TAG).replace("|", "");
                } 
                metricTree = metricTree + METRIC_PATH_SEPARATOR + location;
            }
        }

        Map<String, String> outputMap = new MetricMap();
        int result = 0;

        // iterate over JSON object
        Iterator jsonObjectKeys = jsonObject.keys();
        boolean skipNoCheckpointAvailable =
                EpaUtils.getBooleanProperty(SKIP_NO_CHECKPOINT_AVAILABLE, false);

        while (jsonObjectKeys.hasNext()) {
            String thisKey = jsonObjectKeys.next().toString();

            // if this is another object do recursion
            if (jsonObject.optJSONObject(thisKey) != null) {
                JSONObject innerJsonObject = jsonObject.getJSONObject(thisKey);
                generateMetrics(metricMap, innerJsonObject.toString(), metricTree);
            } else if (jsonObject.optJSONArray(thisKey) != null) {
                // iterate over array
                JSONArray innerJsonArray = jsonObject.optJSONArray(thisKey);
                for (int i = 0; i < innerJsonArray.length(); i++) {

                    // recursively generate metrics for these tags
                    if (thisKey.equals(RESULT_TAG)) {

                        // if result of first object was "No checkpoint available"
                        // and there is a next element skip it
                        JSONObject arrayElement = innerJsonArray.optJSONObject(i);

                        // skip empty? json array
                        if (null != arrayElement) {
                            if (skipNoCheckpointAvailable
                                    && arrayElement.has(RESULT_TAG)
                                    && (RESULT_NO_CHECKPOINT_AVAILABLE
                                    == arrayElement.optInt(RESULT_TAG))
                                    && ((i + 1) < innerJsonArray.length())) {

                                EpaUtils.getFeedback().debug(module,
                                        "skipping node '" + thisKey
                                                + "' with result value "
                                                + arrayElement.optInt(RESULT_TAG));
                            } else {
                                JSONObject resultObj = innerJsonArray.getJSONObject(i);
                                try {
                                    // find the monitor
                                    Monitor mon =
                                            MonitorFactory.findMonitor(resultObj.getString("name"));
                                    if (mon != null) {
                                        // hand over parsing to a monitor-specific handler chain
                                        mon.generateMetrics(metricMap,
                                                resultObj.toString(),
                                                metricTree);
                                    } else {
                                        // recursively generate metrics for these tags
                                        generateMetrics(metricMap,
                                                resultObj.toString(),
                                                metricTree);
                                    }
                                } catch (JSONException e) {
                                    EpaUtils.getFeedback().debug(module,
                                            "Missing name key for log record.");
                                } catch (AsmException e) {
                                    handleException(e, metricTree, metricMap, module);
                                }
                            }
                        }
                    } else if (thisKey.equals(MONITORS_TAG)
                            || thisKey.equals(STATS_TAG)) {
                        // recursively generate metrics for these tags
                        generateMetrics(metricMap,
                                innerJsonArray.getJSONObject(i).toString(), metricTree);
                    } else {
                        generateMetrics(metricMap,
                                innerJsonArray.getJSONObject(i).toString(),
                                metricTree + METRIC_PATH_SEPARATOR + thisKey.replace("|", ""));
                    }
                }
            } else {
                // ignore these tags
                if ((thisKey.equals(CODE_TAG)) || (thisKey.equals(ELAPSED_TAG))
                        || (thisKey.equals(INFO_TAG)) || (thisKey.equals(VERSION_TAG))
                        || (thisKey.equals(ACTIVE_TAG))
                        || (jsonObject.optString(thisKey, EMPTY_STRING).length() == 0)
                        || format.ignoreTagForMonitor(thisKey)) {
                    continue;
                } else if (thisKey.equalsIgnoreCase(OUTPUT_TAG)) {
                    if (null != getSuccessor()) {
                        try {
                            // let successors do the work
                            String thisValue = jsonObject.getString(thisKey);
                            if ((null != thisValue) && (0 < thisValue.length())) {
                                getSuccessor().generateMetrics(outputMap, thisValue, metricTree);
                            } else {
                                EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                                                            AsmMessages.OUTPUT_EMPTY_WARN_705,
                                                            getName(),
                                                            metricTree));
                            }
                        } catch (AsmException e) {
                            handleException(e, metricTree, metricMap, module);
                        } catch (Exception e) {
                            //Don't throw. Some formats are not yet supported
                            EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                                                        AsmMessages.OUTPUT_HANDLE_WARN_702,
                                                        e.getMessage(),
                                                        getName(),
                                                        metricTree,
                                                        jsonString));
                        }
                    }
                    // anyway don't write metric
                    continue;
                }

                // set metric tree if there is no name (we are at the json root)
                if (null == metricTree) {
                    metricTree = MONITOR_METRIC_PREFIX.substring(0, MONITOR_METRIC_PREFIX.length() - 1);
                }

                // automatically converts to string if an integer
                String thisValue = jsonObject.optString(thisKey);

                // only continue if not empty
                if ((null == thisValue)
                        || thisValue.length() == 0) {
                    return metricMap;
                    // TODO: continue instead?
                }

                if (thisKey.equals(COLOR_TAG)) {
                    String rawErrorMetric = metricTree + METRIC_NAME_SEPARATOR
                            + (String) AsmPropertiesImpl.ASM_METRICS.get(COLORS_TAG);
                    if (AsmPropertiesImpl.ASM_COLORS.containsKey(thisValue)) {
                        metricMap.put(rawErrorMetric,
                                      (String) AsmPropertiesImpl.ASM_COLORS.get(thisValue));
                    } else {
                        metricMap.put(rawErrorMetric, ZERO);
                    }

                    // map location
                } else if (thisKey.equals(LOCATION_TAG)) {
                    // use mapped value if existing
                    String location = AsmRequestHelper.getMonitoringStationMap().get(thisValue);
                    if (null != location) {
                        thisValue = location;
                    }
                    // map result code
                } else if (thisKey.equals(RESULT_TAG)) {
                    try {
                        result = Integer.parseInt(thisValue);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    metricMap.put(metricTree + METRIC_NAME_SEPARATOR + STATUS_MESSAGE_VALUE,
                                  format.mapResponseToStatusCode(thisValue));
                    // received data
                    metricMap = setDataReceived(metricMap, metricTree);
                }

                // map metric key
                if (AsmPropertiesImpl.ASM_METRICS.containsKey(thisKey)) {
                    thisKey = AsmPropertiesImpl.ASM_METRICS.get(thisKey);
                }

                // put metric into map
                String rawMetric = metricTree + METRIC_NAME_SEPARATOR + thisKey;
                if ((null == rawMetric) || (null == thisValue)) {
                    EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                                                AsmMessages.METRIC_NULL_WARN_703,
                                                rawMetric,
                                                thisValue));
                } else {
                    metricMap.put(rawMetric, thisValue);
                }
            }
        }

        // if monitor result is timeout set step status message value to timeout, too
        if (EpaUtils.getBooleanProperty(TIMEOUT_REPORT_ALWAYS, true) && isTimeout(result)) {
            for (Iterator<String> it = outputMap.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                if (key.endsWith(STATUS_MESSAGE_VALUE)) {
                    metricMap.put(key,
                                  format.mapResponseToStatusCode(Integer.toString(result)));
                    metricMap = setDataReceived(metricMap, metricTree);
                } else {
                    metricMap.put(key, outputMap.get(key));
                }
            }
        } else {
            metricMap.putAll(outputMap);
        }

        // add a step node if STEP_FORMAT_ALWAYS is true
        if (EpaUtils.getBooleanProperty(STEP_FORMAT_ALWAYS, false)
                && (null != name)  
                && (!metricMap.isEmpty())) {
            metricMap = addStep(metricMap, monitor, metricTree);
        } else {
            if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                if (null != monitor) {
                    EpaUtils.getFeedback().verbose(module, "not adding a step for monitor "
                                                   + metricTree
                                                   + " of type " + monitor.getType());
                } else {
                    EpaUtils.getFeedback().verbose(module, "not adding a step for monitor "
                                                   + metricTree);
                }
            }
        }

        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module, "BaseMonitor returning "
                                           + (metricMap.size() - originalMapSize)
                                           + " metrics for monitor " + getName()
                                           + " in metric tree " + metricTree);
        }

        // save the most recent UUID
        String lastUuid = metricMap.get(UUID_TAG);
        String currentUuid = jsonObject.optString(UUID_TAG, null);
        if (currentUuid != null
                && (lastUuid == null 
                || UUID.fromString(currentUuid).compareTo(UUID.fromString(lastUuid)) > 0)) {
            metricMap.put(UUID_TAG, currentUuid);            
        }

        return metricMap;
    }

    /**
     * Handle an exception thrown by generateMetrics().
     * Ignore warnings if the http result code already indicates an error.
     * @param metricTree metric tree prefix
     * @param metricMap map containing the metrics
     * @param module log module
     * @param exception the exception thrown 
     */
    private void handleException(AsmException exception,
                                 String metricTree,
                                 Map<String, String> metricMap,
                                 Module module) {

        if (ERROR_900 > exception.getErrorCode()) {

            // this is a warning, get http result code
            int resultCode = 0;
            String resultString = metricMap.get(metricTree
                                                + METRIC_NAME_SEPARATOR + RESULT_CODE);
            try {
                resultCode = Integer.parseInt(resultString);
            } catch (NumberFormatException ex) {
                resultCode = 99999; // assume error
            }

            // check result code 
            if (400 <= resultCode) {
                // we already have a http error => only log if verbose
                if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                    EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
                                                   AsmMessages.GENERATE_METRICS_ERROR_710,
                                                   exception.getMessage(),
                                                   resultString));
                }
            } else {
                // no http error, log as warning
                EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                                            AsmMessages.GENERATE_METRICS_ERROR_710,
                                            exception.getMessage(),
                                            resultString));
            }
        } else {
            // this is an error so log it
            EpaUtils.getFeedback().error(module, exception.getMessage());
        }
    }

    /**
     * Add a step node to the metric tree copying select metrics.
     * @param metricMap original metric map
     * @param monitor the current monitor
     * @param metricTree the metric tree for the monitor
     * @return the metric map with the added step node
     */
    protected Map<String, String> addStep(Map<String, String> metricMap,
                                          Monitor monitor,
                                          String metricTree) {
        Module module = new Module(Thread.currentThread().getName());

        if (null != monitor) {
            // removed && (!SCRIPT_MONITOR.equals(monitor.getType()))
            // because we want the step also if we have an error!
            String stepMetricTree = metricTree + METRIC_PATH_SEPARATOR
                    + format.formatStep(1, EMPTY_STRING);

            if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                EpaUtils.getFeedback().verbose(module, "adding a step for monitor " + metricTree
                                               + " of type " + monitor.getType());
            }

            // copy these metrics


            for (Iterator<String> it = metricsToAddToStep.iterator(); it.hasNext(); ) {
                String metricName = it.next();
                String key = metricTree + METRIC_NAME_SEPARATOR + metricName;
                String newKey = stepMetricTree + METRIC_NAME_SEPARATOR + metricName;
                
                if (metricMap.containsKey(key)) {
                    if (RESULT_CODE.equals(metricName)) {
                        newKey = stepMetricTree + METRIC_NAME_SEPARATOR + RESPONSE_CODE;
                        if (!metricMap.containsKey(newKey)) {
                            if (metricMap.get(key).equals("0")) {
                                metricMap.put(newKey, Integer.toString(RESULT_OK));
                            } else {
                                metricMap.put(newKey, metricMap.get(key));
                            }
                        }
                    } else {
                        if (!metricMap.containsKey(newKey)) {
                            metricMap.put(newKey, metricMap.get(key));
                        }
                    }

                    if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                        EpaUtils.getFeedback().debug(module, "copied metric " + stepMetricTree
                                                     + METRIC_NAME_SEPARATOR + metricName);
                    }

                    if (key.endsWith(STATUS_MESSAGE_VALUE)) {
                        metricMap = setDataReceived(metricMap, metricTree);
                    }
                }
            }

            // add url from monitor if not there
            String urlMetric = stepMetricTree + METRIC_NAME_SEPARATOR + TEST_URL;
            if (!metricMap.containsKey(urlMetric)) {
                metricMap.put(urlMetric, monitor.getUrl());
            }
        }

        return metricMap;
    }

    /**
     * Check if the supplied result code is a timeout. 
     * @param result the result code to check
     * @return true if it is a timeout, e.g. 7011 or 1042.
     */
    public boolean isTimeout(int result) {
        switch (result) {
          case RESULT_CONNECT_TIMEOUT:
          case RESULT_EXECUTION_TIMEOUT:
          case RESULT_PAGE_LOAD_TIMEOUT:
          case RESULT_OPERATION_TIMEOUT:
              return true;
          default:
              return false;
        }
    }

    /**
     * Put "Data Received" metric.
     * @param metricMap map to insert metrics into
     * @param metricTree metric tree prefix
     * @return map containing the metrics
     */
    protected Map<String, String> setDataReceived(Map<String, String> metricMap,
                                                  String metricTree) {
        String key = metricTree + METRIC_NAME_SEPARATOR + METRIC_NAME_DATA_RECEIVED;
        metricMap.put(key, "1");
        return metricMap;
    }

}
