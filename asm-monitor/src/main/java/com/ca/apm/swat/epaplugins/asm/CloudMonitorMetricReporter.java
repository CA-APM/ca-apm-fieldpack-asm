package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Inflater;



//import sun.misc.BASE64Decoder;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.AsmPropertiesImpl;

public class CloudMonitorMetricReporter implements AsmProperties {

    private MetricWriter metricWriter;
    private boolean apmcmDisplayCheckpoint;
    private HashMap<String, String> checkpointMap;
    private XmlAnalysisAdapter analysisAdapter;

    protected  static final String SEPARATOR = "\\.";

    /**
     * Report metrics to APM via metric writer.
     * @param metricWriter the metric writer
     * @param apmcmDisplayCheckpoint display monitor info in metric path?
     * @param checkpointMap map containing all checkpoints
     */
    public CloudMonitorMetricReporter(MetricWriter metricWriter,
                                      boolean apmcmDisplayCheckpoint,
                                      HashMap<String, String> checkpointMap) {
        this.metricWriter = metricWriter;
        this.apmcmDisplayCheckpoint = apmcmDisplayCheckpoint;
        this.checkpointMap = checkpointMap;
        analysisAdapter = new XmlAnalysisAdapter();
    }

    /**
     * Write the metrics to the {@link MetricWriter}.
     * @param metricMap map containing the metrics
     * @throws Exception errors
     */
    public void printMetrics(HashMap<String, String> metricMap) throws Exception {
        Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
        while (metricIt.hasNext()) {
            Map.Entry<String, String> metricPairs = (Map.Entry<String, String>) metricIt.next();

            if (((String) metricPairs.getValue()).length() == 0) {
                continue;
            }

            String thisMetricType = returnMetricType((String) metricPairs.getValue());

            if (thisMetricType.equals(MetricWriter.kFloat)) {
                metricPairs.setValue(((String) metricPairs.getValue()).split(SEPARATOR)[0]);
                thisMetricType = MetricWriter.kIntCounter;
            }

            metricWriter.writeMetric(thisMetricType, METRIC_TREE + METRIC_PATH_SEPARATOR
                + metricPairs.getKey(), metricPairs.getValue());
        }
    }

    /**
     * Get metric data type. 
     * @param thisMetric input metric data
     * @return metric type, one of {@link MetricWriter.kStringEvent},
     * {@link MetricWriter.kIntCounter}, {@link MetricWriter.kLongCounter} or
     * {@link MetricWriter.kFloat}
     */
    private String returnMetricType(String thisMetric) {
        String metricType = MetricWriter.kStringEvent;

        if (thisMetric.matches("^[+-]?[0-9]+$")) {
            try {
                new Integer(thisMetric);
                metricType = MetricWriter.kIntCounter;
            } catch (NumberFormatException e) {
                metricType = MetricWriter.kLongCounter;
            }
        } else if (thisMetric.matches("^[+-]?[0-9]*\\.[0-9]+$")) {
            metricType = MetricWriter.kFloat;
        } else {
            metricType = MetricWriter.kStringEvent;
        }
        /*
        try {
            new Integer(thisMetric);
            metricType = MetricWriter.kIntCounter;
        } catch (NumberFormatException e) {
            try {
                new Long(thisMetric);
                metricType = MetricWriter.kLongCounter;
            } catch (NumberFormatException ee) {
                try {
                    new Float(thisMetric);
                    metricType = MetricWriter.kFloat;
                } catch (NumberFormatException eee) {
                    metricType = MetricWriter.kStringEvent;
                }
            }
        }
         */
        return metricType;
    }

    /**
     * Reset all metrisc in <code>metricMap</code> to 0 or "".
     * @param metricMap map containing the metrics
     * @return the reset map
     * @throws Exception errors
     */
    public HashMap<String, String> resetMetrics(HashMap<String, String> metricMap)
            throws Exception {
        if (metricMap.size() != 0) {
            Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
            while (metricIt.hasNext()) {
                Map.Entry<String, String> metricPairs = (Map.Entry<String, String>) metricIt.next();

                if (!returnMetricType((String) metricPairs.getValue()).equals(
                    MetricWriter.kStringEvent)) {
                    metricMap.put((String) metricPairs.getKey(), ZERO);
                } else {
                    metricMap.put((String) metricPairs.getKey(), EMPTY_STRING);
                }
            }
        }

        return metricMap;
    }

    /**
     * Recursively generate metrics from API call result. 
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @return metricMap map containing the metrics
     * @throws Exception errors
     */
    protected HashMap<String, String> generateMetrics(String jsonString, String metricTree)
            throws Exception {
        HashMap<String, String> metricMap = new HashMap<String, String>();

        JSONObject jsonObject = new JSONObject(jsonString);

        if (jsonObject.optString(kAPMCMName, null) != null) {
            metricTree = metricTree + METRIC_PATH_SEPARATOR + jsonObject.getString(kAPMCMName);
        }

        if (apmcmDisplayCheckpoint) {
            if (jsonObject.optString(kAPMCMLoc, null) != null) {
                metricTree = metricTree + METRIC_PATH_SEPARATOR
                        + (String) this.checkpointMap.get(jsonObject.getString(kAPMCMLoc));
            }
        }

        Iterator jsonObjectKeys = jsonObject.keys();
        while (jsonObjectKeys.hasNext()) {
            String thisKey = jsonObjectKeys.next().toString();

            if (jsonObject.optJSONObject(thisKey) != null) {
                JSONObject innerJsonObject = jsonObject.getJSONObject(thisKey);
                metricMap.putAll(generateMetrics(innerJsonObject.toString(), metricTree));
            } else if (jsonObject.optJSONArray(thisKey) != null) {
                JSONArray innerJsonArray = jsonObject.optJSONArray(thisKey);
                for (int i = 0; i < innerJsonArray.length(); i++) {
                    if ((thisKey.equals(kAPMCMResult)) || (thisKey.equals(kAPMCMMonitors))
                            || (thisKey.equals(kAPMCMStats))) {
                        metricMap.putAll(generateMetrics(
                            innerJsonArray.getJSONObject(i).toString(), metricTree));
                    } else {
                        metricMap.putAll(generateMetrics(
                            innerJsonArray.getJSONObject(i).toString(), metricTree 
                            + METRIC_PATH_SEPARATOR + thisKey));
                    }
                }
            } else {
                if ((thisKey.equals(kAPMCMCode)) || (thisKey.equals(kAPMCMElapsed))
                        || (thisKey.equals(kAPMCMInfo)) || (thisKey.equals(kAPMCMVersion))
                        || (jsonObject.optString(thisKey, EMPTY_STRING).length() == 0)) {
                    continue;
                }
                String thisValue = jsonObject.getString(thisKey);

                if (thisKey.equals(kAPMCMDescr)) {
                    String rawErrorMetric = metricTree + METRIC_NAME_SEPARATOR
                            + (String) AsmPropertiesImpl.APM_CM_METRICS.get(kAPMCMErrors);
                    metricMap.put(CloudMonitorRequestHelper.fixMetric(rawErrorMetric), ONE);
                }

                if (thisKey.equals(kAPMCMColor)) {
                    String rawErrorMetric = metricTree + METRIC_NAME_SEPARATOR
                            + (String) AsmPropertiesImpl.APM_CM_METRICS.get(kAPMCMColors);
                    if (AsmPropertiesImpl.APM_CM_COLORS.containsKey(thisValue)) {
                        metricMap.put(
                            CloudMonitorRequestHelper.fixMetric(rawErrorMetric),
                            (String) AsmPropertiesImpl.APM_CM_COLORS.get(thisValue));
                    } else {
                        metricMap.put(CloudMonitorRequestHelper.fixMetric(rawErrorMetric), ZERO);
                    }

                }

                if (AsmPropertiesImpl.APM_CM_METRICS.containsKey(thisKey)) {
                    thisKey = ((String) AsmPropertiesImpl.APM_CM_METRICS.get(thisKey)).toString();
                }

                if (thisKey.equalsIgnoreCase(kAPMCMOutput)) {

                    //Handled different
                    continue;
                }

                String rawMetric = metricTree + METRIC_NAME_SEPARATOR + thisKey;
                metricMap.put(CloudMonitorRequestHelper.fixMetric(rawMetric),
                    CloudMonitorRequestHelper.fixMetric(thisValue));
            }
        }

        return metricMap;
    }

    /**
     * Decompress compressed data.
     * @param data compressed data
     * @return uncompressed data
     */
    public byte[] decompress(byte[] data) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();

            inflater.end();
            return output;
        } catch (Exception ex) {
            return null;
        }

    }

    /**
     * Recursively analyze the content.
     * @param jsonString API call result.
     * @param folder folder name
     * @param metricMap map containing the metrics
     * @throws JSONException errors
     */
    public void analyzeContentResults(String jsonString,
                                      String folder,
                                      HashMap<String, String> metricMap)
                                              throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);

        String name = kAPMCMUndefined;
        if (jsonObject.optString(kAPMCMName, null) != null) {
            name = jsonObject.getString(kAPMCMName);
        }
        Iterator jsonObjectKeys = jsonObject.keys();
        while (jsonObjectKeys.hasNext()) {
            String thisKey = jsonObjectKeys.next().toString();

            if (jsonObject.optJSONObject(thisKey) != null) {
                JSONObject innerJsonObject = jsonObject.getJSONObject(thisKey);
                analyzeContentResults(innerJsonObject.toString(), folder, metricMap);
            } else if (jsonObject.optJSONArray(thisKey) != null) {
                JSONArray innerJsonArray = jsonObject.optJSONArray(thisKey);
                for (int i = 0; i < innerJsonArray.length(); i++) {
                    analyzeContentResults(innerJsonArray.getJSONObject(i).toString(), folder,
                        metricMap);
                }
            } else {
                if ((thisKey.equals(kAPMCMColor)) || (thisKey.equals(kAPMCMElapsed))
                        || (thisKey.equals(kAPMCMInfo)) || (thisKey.equals(kAPMCMVersion))
                        || (thisKey.equals(kAPMCMCode))
                        || (jsonObject.optString(thisKey, EMPTY_STRING).length() == 0)) {
                    continue;
                }
                String thisValue = jsonObject.getString(thisKey);

                // TODO: separate into different components -
                // chain of responsibility discover, decode, handle
                if (thisKey.equalsIgnoreCase(kAPMCMOutput)) {
                    try {
                        String originalString = thisValue;
                        byte[] decoded = Base64.decodeBase64(originalString);
                        if (decoded != null) {
                            byte[] bytesDecompressed = decompress(decoded);
                            if (bytesDecompressed != null) {
                                String returnValue = new String(bytesDecompressed, 0,
                                    bytesDecompressed.length, UTF8);
                                if (returnValue.startsWith(kXMLPrefix)) {
                                    analysisAdapter.analyzeXml(returnValue, folder, name,
                                        metricMap);
                                } else {
                                    if (returnValue.startsWith(kAPMCMHarOrLog)) {
                                        // Do nothing - already have seen it.
                                        // and we don't need this log
                                    }
                                }
                                continue;
                            }
                        }
                    } catch (Exception uee) {
                        uee.printStackTrace();
                        //Don't throw. Some formats are not yet supported
                    }
                }
            }
        }
    }

}
