package com.ca.apm.swat.epaplugins.asm.rules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.AsmPropertiesImpl;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Base class for implementations of the {@link Rule} interface.
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class BaseRule implements Rule, AsmProperties {

    private String name = null;
    private String folder = null;
    private String[] tags = null;
    private String type = null;

    protected Handler successor = null;

    /**
     * Rule base class.
     * @param name name of the rule
     * @param folder folder of the rule
     * @param tags tags of the rule
     */
    protected BaseRule(String name, String type, String folder, String[] tags) {
        this.name = name;
        this.folder = folder;
        this.tags = tags;
        this.type = type;
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

    /**
     * Compare the name of the rule with a string.
     * Needed to include/exclude rules by name.
     * @param anotherName string to compare rule name with
     * @return true if the rule name equals anotherName
     */
    public boolean equals(String anotherName) {
        EpaUtils.getFeedback().debug("equals(String s) called for Rule " + name
            + " with s = anotherName"); 
        return this.name.equals(anotherName);
    }

    /**
     * Compare the names of the rules.
     * @param anotherRule rule to compare with
     * @return true if the rule names are equal
     */
    public boolean equals(Rule anotherRule) {
        return this.name.equals(anotherRule.getName());
    }


    /**
     * Recursively generate metrics from API call result. 
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @return metricMap map containing the metrics
     */
    public HashMap<String, String> generateMetrics(
        String jsonString,
        String metricTree,
        Properties properties,
        HashMap<String, String> checkpointMap) {

        HashMap<String, String> metricMap = new HashMap<String, String>();

        JSONObject jsonObject = new JSONObject(jsonString);

        if (jsonObject.optString(NAME_TAG, null) != null) {
            metricTree = metricTree + METRIC_PATH_SEPARATOR + jsonObject.getString(NAME_TAG);
        }

        if (TRUE.equals(properties.getProperty(DISPLAY_CHECKPOINTS, TRUE))) {
            if (jsonObject.optString(LOCATION_TAG, null) != null) {
                metricTree = metricTree + METRIC_PATH_SEPARATOR
                        + (String) checkpointMap.get(jsonObject.getString(LOCATION_TAG));
            }
        }

        Iterator jsonObjectKeys = jsonObject.keys();
        while (jsonObjectKeys.hasNext()) {
            String thisKey = jsonObjectKeys.next().toString();

            if (jsonObject.optJSONObject(thisKey) != null) {
                JSONObject innerJsonObject = jsonObject.getJSONObject(thisKey);
                metricMap.putAll(generateMetrics(innerJsonObject.toString(),
                    metricTree, properties, checkpointMap));
            } else if (jsonObject.optJSONArray(thisKey) != null) {
                JSONArray innerJsonArray = jsonObject.optJSONArray(thisKey);
                for (int i = 0; i < innerJsonArray.length(); i++) {
                    if ((thisKey.equals(RESULT_TAG)) || (thisKey.equals(MONITORS_TAG))
                            || (thisKey.equals(STATS_TAG))) {
                        metricMap.putAll(generateMetrics(
                            innerJsonArray.getJSONObject(i).toString(),
                            metricTree, properties, checkpointMap));
                    } else {
                        metricMap.putAll(generateMetrics(
                            innerJsonArray.getJSONObject(i).toString(), metricTree 
                            + METRIC_PATH_SEPARATOR + thisKey, properties, checkpointMap));
                    }
                }
            } else {
                if ((thisKey.equals(CODE_TAG)) || (thisKey.equals(ELAPSED_TAG))
                        || (thisKey.equals(INFO_TAG)) || (thisKey.equals(VERSION_TAG))
                        || (jsonObject.optString(thisKey, EMPTY_STRING).length() == 0)) {
                    continue;
                }
                String thisValue = jsonObject.getString(thisKey);

                if (thisKey.equals(DESCR_TAG)) {
                    String rawErrorMetric = metricTree + METRIC_NAME_SEPARATOR
                            + (String) AsmPropertiesImpl.ASM_METRICS.get(ERRORS_TAG);
                    metricMap.put(EpaUtils.fixMetric(rawErrorMetric), ONE);
                }

                if (thisKey.equals(COLOR_TAG)) {
                    String rawErrorMetric = metricTree + METRIC_NAME_SEPARATOR
                            + (String) AsmPropertiesImpl.ASM_METRICS.get(COLORS_TAG);
                    if (AsmPropertiesImpl.ASM_COLORS.containsKey(thisValue)) {
                        metricMap.put(
                            EpaUtils.fixMetric(rawErrorMetric),
                            (String) AsmPropertiesImpl.ASM_COLORS.get(thisValue));
                    } else {
                        metricMap.put(EpaUtils.fixMetric(rawErrorMetric), ZERO);
                    }

                }

                if (AsmPropertiesImpl.ASM_METRICS.containsKey(thisKey)) {
                    thisKey = ((String) AsmPropertiesImpl.ASM_METRICS.get(thisKey)).toString();
                }

                if (thisKey.equalsIgnoreCase(OUTPUT_TAG)) {

                    //Handled different
                    continue;
                }

                String rawMetric = metricTree + METRIC_NAME_SEPARATOR + thisKey;
                metricMap.put(EpaUtils.fixMetric(rawMetric),
                    EpaUtils.fixMetric(thisValue));
            }
        }

        EpaUtils.getFeedback().verbose("BaseRule returning " + metricMap.size()
            + " metrics for rule " + getName() + " in metric tree " + metricTree);
        
        return metricMap;
    }

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }
    
    protected Handler getSuccessor() {
        return this.successor;
    }
}
