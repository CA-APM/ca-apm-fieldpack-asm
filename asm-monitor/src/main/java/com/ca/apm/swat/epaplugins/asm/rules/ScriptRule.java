package com.ca.apm.swat.epaplugins.asm.rules;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.wily.introscope.epagent.EpaUtils;


/**
 * {@link Rule} implementation for script monitors.
 * A ScriptRule generates additional metrics per JMeter step.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
@SuppressWarnings("restriction")
public class ScriptRule extends BaseRule {


    protected ScriptRule(String name, String folder, String[] tags) {
        super(name, SCRIPT_RULE, folder, tags);

        // build chain of responsibility
        Handler jmeterHandler = new JMeterScriptHandler();
        // setSuccessor(jmeterHandler);
        Handler decoder = new InflatingBase64Decoder();
        decoder.setSuccessor(jmeterHandler);
        setSuccessor(decoder);
    }

    @Override
    public HashMap<String, String> generateMetrics(String jsonString,
        String metricTree,
        Properties properties,
        HashMap<String, String> checkpointMap) {

        HashMap<String, String> metricMap = null;

        try {
            metricMap = super.generateMetrics(jsonString, metricTree, properties, checkpointMap);

            // remove MONITOR_METRIC_PREFIX from metric tree for step metrics
            String statusMetricTree = STATUS_METRIC_PREFIX
                    + metricTree.substring(MONITOR_METRIC_PREFIX.length())
                    + METRIC_PATH_SEPARATOR + getName();
            metricMap.putAll(analyzeContentResults(jsonString, statusMetricTree, properties,
                checkpointMap));

            EpaUtils.getFeedback().verbose("ScriptRule returning " + metricMap.size()
                + " metrics from super() for rule " + getName() + " in metric tree " + metricTree);
        } catch (JSONException e) {
            EpaUtils.getFeedback().error(e.getMessage());
            EpaUtils.getFeedback().error("jsonString = " + jsonString);
            EpaUtils.getFeedback().error("rule " + getName() + ", metric tree  =" + metricTree);
            throw e;
        }
        return metricMap;
    }

    /**
     * Recursively analyze the content.
     * @param jsonString API call result.
     * @param folder folder name
     * @return metric map
     * @throws JSONException errors
     */
//    protected HashMap<String, String> analyzeContentResults(String jsonString, String folder)
    protected HashMap<String, String> analyzeContentResults(String jsonString,
        String metricTree,
        Properties properties,
        HashMap<String, String> checkpointMap) 
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
                metricMap.putAll(analyzeContentResults(innerJsonObject.toString(),
                    metricTree, properties, checkpointMap));
            } else if (jsonObject.optJSONArray(thisKey) != null) {
                JSONArray innerJsonArray = jsonObject.optJSONArray(thisKey);
                for (int i = 0; i < innerJsonArray.length(); i++) {
                    metricMap.putAll(
                        analyzeContentResults(innerJsonArray.getJSONObject(i).toString(),
                            metricTree, properties, checkpointMap));
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

                        byte[] decoded = Base64.decodeBase64(thisValue);
                        if (decoded != null) {
                            byte[] bytesDecompressed = decompress(decoded);
                            if (bytesDecompressed != null) {
                                String returnValue = new String(bytesDecompressed, 0,
                                    bytesDecompressed.length, UTF8);

                                if (EpaUtils.getFeedback().isVerboseEnabled()) {
                                    EpaUtils.getFeedback().verbose(
                                        "calling JMeterScriptHandler directly");
                                }
                                if (returnValue.startsWith(XML_PREFIX)) {
                                    /*
                                    Handler jmeterHandler = new JMeterScriptHandler();
                                    setSuccessor(jmeterHandler);
                                    metricMap.putAll(
                                        successor.generateMetrics(returnValue,
                                            metricTree, properties, checkpointMap));

                                    if (EpaUtils.getFeedback().isVerboseEnabled()) {
                                        EpaUtils.getFeedback().verbose(
                                            "calling JMeterScriptHandler via handlers");
                                    }
                                     */
                                    metricMap.putAll(
                                        successor.generateMetrics(thisValue, metricTree,
                                            properties, checkpointMap));

                                } else {
                                    if (returnValue.startsWith(HAR_OR_LOG_TAG)) {
                                        // Do nothing - already have seen it.
                                        // and we don't need this log
                                    }
                                }
                                continue;
                            }
                        }

                        /*
                        metricMap.putAll(
                            successor.generateMetrics(thisValue,
                                metricTree, properties, checkpointMap));
                         */
                    } catch (Exception uee) {
                        uee.printStackTrace();
                        //Don't throw. Some formats are not yet supported
                    }
                }
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
            java.util.zip.Inflater inflater = new java.util.zip.Inflater();
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
     * Analyze the XML returned by the get logs API. 
     * @param returnValue API call result
     * @param folder folder name
     * @param rule rule name
     * @return metric map containing the metrics
     */
    protected HashMap<String, String> analyzeXml(String returnValue, String folder, String rule) {
        HashMap<String, String> metricMap = new HashMap<String, String>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(returnValue));
            Document document = builder.parse(is);

            NodeList testResults = document.getElementsByTagName(TEST_RESULTS);
            int step = 0;
            if (testResults.getLength() > 0) {
                //We have Jmeter scripts
                org.w3c.dom.Node httpTestNode = testResults.item(0);

                //Working through the steps
                NodeList stepNodes = httpTestNode.getChildNodes();
                for (int i = 0; i < stepNodes.getLength(); i++) {

                    org.w3c.dom.Node stepNode = stepNodes.item(i);
                    if (stepNode.getNodeType() == Node.ELEMENT_NODE) {
                        step = reportJMeterStep(folder, rule, step, stepNode, metricMap);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // don't throw, simply quit.
            // TODO In future we have to implement it for multiple different XML formats.
        }
        return metricMap;
    }

    /**
     * Report JMeter steps as individual metrics. 
     * @param folder folde name
     * @param rule rule name
     * @param step step number
     * @param stepNode step node
     * @param metricMap map containing the metrics
     * @return the step number
     */
    private int reportJMeterStep(String folder, String rule, int step, org.w3c.dom.Node stepNode,
                                 HashMap<String, String> metricMap) {
        int assertionFailures = 0;
        int assertionErrors = 0;
        step++;
        //First the attributes
        NamedNodeMap attributes = stepNode.getAttributes();
        int responsecode = Integer.parseInt(
            attributes.getNamedItem(RESPONSE_CODE_TAG).getNodeValue());
        String responsemessage = attributes.getNamedItem(RESPONSE_MESSAGE_TAG).getNodeValue();
        String successFlag = attributes.getNamedItem(SUCCESS_FLAG_TAG).getNodeValue();
        final int errorCount = Integer.parseInt(attributes.getNamedItem(ERROR_COUNT_TAG)
            .getNodeValue());
        String url = attributes.getNamedItem(TEST_URL_TAG).getNodeValue();
        boolean assertionAvailable = false;
        boolean assertionFailure = false;
        boolean assertionError = false;
        String assertionName = UNDEFINED_ASSERTION;

        NodeList stepChildren = stepNode.getChildNodes();
        //Walk Through the elements of one step
        for (int j = 0; j < stepChildren.getLength(); j++) {
            org.w3c.dom.Node stepChild = stepChildren.item(j);
            if (stepChild.getNodeType() == Node.ELEMENT_NODE && stepChild.getNodeName()
                    .equals(ASSERTION_RESULT)) {
                assertionAvailable = true;
                NodeList assertionResultEntries = stepChild.getChildNodes();
                for (int l = 0; l < assertionResultEntries.getLength(); l++) {
                    org.w3c.dom.Node assertionResultEntry = assertionResultEntries.item(l);
                    if (assertionResultEntry.getNodeName().equals(NAME_TAG)) {
                        assertionName = assertionResultEntry.getFirstChild().getNodeValue();
                    }
                    if (assertionResultEntry.getNodeName().equals(FAILURE)) {
                        assertionFailure = Boolean.parseBoolean(
                            assertionResultEntry.getFirstChild().getNodeValue());
                    }
                    if (assertionResultEntry.getNodeName().equals(ERROR_TAG)) {
                        assertionError = Boolean.parseBoolean(
                            assertionResultEntry.getFirstChild().getNodeValue());
                    }
                }
            }
        }
        url = url.replace(":", "-");
        url = url.replace("|", "-");
        String metric = STATUS_METRIC_PREFIX + folder + METRIC_PATH_SEPARATOR + rule
                + METRIC_PATH_SEPARATOR + STEP + step + " " + url;
        //Collect results
        String statusMessage = null;
        int statusCode = 1;
        boolean assertionFailed = false;
        if (assertionAvailable) {
            if (assertionFailure) {
                assertionFailed = true;
                statusMessage = rule + ASSERTION_FAILURE;
                statusCode = 3;
                assertionFailures++;
            }
            if (assertionError) {
                assertionFailed = true;
                statusMessage = rule + ASSERTION_ERROR;
                statusCode = 3;
                assertionErrors++;
            }
        }
        if (!assertionFailed) {
            statusMessage = responsecode + " - " + responsemessage;
            statusCode = mapResponseToStatusCode(responsecode);
        }

        //Report metrics
        metricMap.put(metric + METRIC_NAME_SEPARATOR + STATUS_MESSAGE,
            statusMessage);
        metricMap.put(metric + METRIC_NAME_SEPARATOR + STATUS_MESSAGE_VALUE,
            Integer.toString(statusCode));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + RESPONSE_CODE,
            Integer.toString(responsecode));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + ERROR_COUNT,
            Integer.toString(errorCount));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + ASSERTION_FAILURES,
            Integer.toString(assertionFailures));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + ASSERTION_ERRORS,
            Integer.toString(assertionErrors));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + TEST_URL,
            EpaUtils.fixMetric(url));
        return step;
    }

    /**
     * Map status values.
     * @param responsecode input value
     * @return mapped output status code
     */
    private int mapResponseToStatusCode(int responsecode) {
        // TODO: map status values according to config
        int statusCode = 0;
        switch (responsecode) {
        case 403 :
            statusCode = 401;
            break;

        case 404 :
            statusCode = 404;
            break;

        case 500 :
            statusCode = 500;
            break;

        case 503 :
            statusCode = 500;
            break;

        default:
            break;
        }
        return statusCode;
    }

}
