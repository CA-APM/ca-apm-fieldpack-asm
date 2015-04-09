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
            metricMap = super.generateMetrics(jsonString, metricTree);

            // remove MONITOR_METRIC_PREFIX from metric tree for step metrics
            StringBuffer statusMetricTree = new StringBuffer(STATUS_METRIC_PREFIX);

            // if it is MONITOR_METRIC_PREFIX|<folder>
            if (metricTree.length() > MONITOR_METRIC_PREFIX.length()) {
                statusMetricTree.append(metricTree.substring(MONITOR_METRIC_PREFIX.length()))
                .append(METRIC_PATH_SEPARATOR);
            }
            // append rule name
            statusMetricTree.append(getName());
            metricMap.putAll(analyzeContentResults(jsonString, statusMetricTree.toString()));

            EpaUtils.getFeedback().verbose("ScriptRule returning " + metricMap.size()
                + " metrics from super() for rule " + getName() + " in metric tree "
                + statusMetricTree);
        } catch (JSONException e) {
            EpaUtils.getFeedback().error(e.getMessage());
            EpaUtils.getFeedback().error("jsonString = " + jsonString);
            EpaUtils.getFeedback().error("rule " + getName() + ", metric tree  =" + metricTree);
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

                // TODO: separate into different components -
                // chain of responsibility discover, decode, handle
                if (thisKey.equalsIgnoreCase(OUTPUT_TAG)) {
                    try {
/*
                        byte[] decoded = Base64.decodeBase64(thisValue);
                        if (decoded != null) {
                            byte[] bytesDecompressed = decompress(decoded);
                            if (bytesDecompressed != null) {
                                String returnValue = new String(bytesDecompressed, 0,
                                    bytesDecompressed.length, EpaUtils.getEncoding());

                                if (EpaUtils.getFeedback().isVerboseEnabled()) {
                                    EpaUtils.getFeedback().verbose(
                                        "calling JMeterScriptHandler directly");
                                }
                                if (returnValue.startsWith(XML_PREFIX)) {
                                    metricMap.putAll(
                                        successor.generateMetrics(thisValue, metricTree));

                                } else {
                                    if (returnValue.startsWith(HAR_OR_LOG_TAG)) {
                                        System.out.println(returnValue);
                                        // Do nothing - already have seen it.
                                        // and we don't need this log
                                    }
                                }
                                continue;
                            }
                        }
 */

                        metricMap.putAll(successor.generateMetrics(thisValue, metricTree));

                    } catch (Exception uee) {
                        uee.printStackTrace();
                        //Don't throw. Some formats are not yet supported
                    }
                }
            }
        }

        return metricMap;
    }
}
