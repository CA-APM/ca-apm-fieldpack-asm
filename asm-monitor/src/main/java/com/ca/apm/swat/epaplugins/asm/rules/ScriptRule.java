package com.ca.apm.swat.epaplugins.asm.rules;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wily.introscope.epagent.EpaUtils;


/**
 * {@link Rule} implementation for script monitors.
 * A ScriptRule generates additional metrics per JMeter step.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class ScriptRule extends BaseRule {


    protected ScriptRule(String name, String folder, String[] tags) {
        super(name, SCRIPT_RULE, folder, tags);

        // build chain of responsibility
        Handler jmeterHandler = new JMeterScriptHandler();
        Handler decoder = new InflatingBase64Decoder();
        decoder.setSuccessor(jmeterHandler);
        setSuccessor(decoder);
    }

    @Override
    public HashMap<String, String> generateMetrics(String jsonString, String metricTree) {

        HashMap<String, String> metricMap = null;

        try {
            // generate basic metrics
            metricMap = super.generateMetrics(jsonString, metricTree);

            // create metric tree
            StringBuffer statusMetricTree =
                    new StringBuffer(metricTree).append(METRIC_PATH_SEPARATOR).append(getName());
            metricMap.putAll(analyzeContentResults(jsonString, statusMetricTree.toString()));

            EpaUtils.getFeedback().verbose("ScriptRule returning " + metricMap.size()
                + " metrics from super() for rule " + getName() + " in metric tree "
                + statusMetricTree);
        } catch (JSONException e) {
            EpaUtils.getFeedback().error(e.getMessage() + "\n rule " + getName()
                + ", metric tree  =" + metricTree + "\njsonString = " + jsonString);
            throw e;
        }

        EpaUtils.getFeedback().verbose("ScriptRule returning " + metricMap.size()
            + " metrics for rule " + getName() + " in metric tree " + metricTree);

        return metricMap;
    }

    /**
     * Recursively analyze the content.
     * @param jsonString API call result.
     * @param folder folder name
     * @return metric map
     * @throws JSONException errors
     */
    protected HashMap<String, String> analyzeContentResults(String jsonString, String metricTree)
            throws JSONException {

        HashMap<String, String> metricMap = new HashMap<String, String>();
        JSONObject jsonObject = new JSONObject(jsonString);

        String name = UNDEFINED;
        if (jsonObject.optString(NAME_TAG, null) != null) {
            name = jsonObject.getString(NAME_TAG);
        }
        Iterator jsonObjectKeys = jsonObject.keys();
        while (jsonObjectKeys.hasNext()) {
            String thisKey = jsonObjectKeys.next().toString();

            if (jsonObject.optJSONObject(thisKey) != null) {
                JSONObject innerJsonObject = jsonObject.getJSONObject(thisKey);
                metricMap.putAll(analyzeContentResults(innerJsonObject.toString(), metricTree));
            } else if (jsonObject.optJSONArray(thisKey) != null) {
                JSONArray innerJsonArray = jsonObject.optJSONArray(thisKey);
                for (int i = 0; i < innerJsonArray.length(); i++) {
                    metricMap.putAll(
                        analyzeContentResults(innerJsonArray.getJSONObject(i).toString(),
                            metricTree));
                }
            } else {
                if ((thisKey.equals(COLOR_TAG)) || (thisKey.equals(ELAPSED_TAG))
                        || (thisKey.equals(INFO_TAG)) || (thisKey.equals(VERSION_TAG))
                        || (thisKey.equals(CODE_TAG))
                        || (jsonObject.optString(thisKey, EMPTY_STRING).length() == 0)) {
                    continue;
                }
                String thisValue = jsonObject.getString(thisKey);

                if (thisKey.equalsIgnoreCase(OUTPUT_TAG)) {
                    try {
                        // let successors do the work
                        metricMap.putAll(successor.generateMetrics(thisValue, metricTree));
                    } catch (Exception e) {
                        //Don't throw. Some formats are not yet supported
                        EpaUtils.getFeedback().warn(e.getMessage() + "\n rule " + getName()
                            + ", metric tree  =" + metricTree + "\njsonString = " + jsonString);
                    }
                }
            }
        }

        return metricMap;
    }
}
