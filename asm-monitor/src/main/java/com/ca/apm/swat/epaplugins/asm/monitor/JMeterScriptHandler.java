package com.ca.apm.swat.epaplugins.asm.monitor;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;
import java.util.Date;
import java.util.Map;

public class JMeterScriptHandler implements Handler, AsmProperties {

    private static Module module = new Module("Asm.monitor.JMeterScriptHandler");

    protected Handler successor = null;  

    public JMeterScriptHandler() {

    }

    /**
     * Generate metrics from API call result. 
     * @param metricMap map to insert metrics into
     * @param xmlString JMeter script data
     * @param metricTree metric tree prefix
     * @return map containing the metrics
     */
    public Map<String, String> generateMetrics(Map<String, String> metricMap,
                                               String xmlString,
                                               String metricTree) {
        
        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module,
                "JMeterScriptHandler - xmlString = " + xmlString);
        }

        if (!xmlString.startsWith(XML_PREFIX)) {
            if (xmlString.startsWith(HAR_OR_LOG_TAG)) {
                //System.out.println(xmlString);
                // Do nothing - already have seen it.
                // and we don't need this log
            }
            return metricMap;
        }

        int step = 1; // start from 1, business not engineering
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            Document document = builder.parse(is);

            NodeList testResults = document.getElementsByTagName(TEST_RESULTS);

            if (testResults.getLength() > 0) {

                if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                    EpaUtils.getFeedback().debug(module, "JMeterScriptHandler: "
                            + testResults.toString());
                }

                //We have Jmeter scripts
                Node httpTestNode = testResults.item(0);

                //Working through the steps
                NodeList stepNodes = httpTestNode.getChildNodes();
                for (int i = 0; i < stepNodes.getLength(); i++) {

                    Node stepNode = stepNodes.item(i);
                    if (stepNode.getNodeType() == Node.ELEMENT_NODE) {
                        metricMap.putAll(reportJMeterStep(metricTree, step, stepNode));
                        ++step;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // don't throw, simply quit.
            // TODO In future we have to implement it for multiple different XML formats.
        }
        
        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module,
                    "JMeterScriptHandler.generateMetrics for metricTree " + metricTree + " generated " + metricMap.size() + " metrics, steps created: " + (step-1));
        }
        
        return metricMap;
    }

    /**
     * Report JMeter steps as individual metrics. 
     * @param metricTree metric tree prefix
     * @param step step number
     * @param stepNode step node
     * @return metricMap map containing the metrics
     */
    private MetricMap reportJMeterStep(String metricTree,
                                       int step,
                                       Node stepNode) {
        Formatter format = Formatter.getInstance();
        MetricMap metricMap = new MetricMap();

        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module,
                "reportJMeterStep " + step + ": " + stepNode.toString());
        }

        //First the attributes
        NamedNodeMap attributes = stepNode.getAttributes();

        int responseCode = RESULT_OK; // HTTP OK
        try {
            responseCode = Integer.parseInt(
                attributes.getNamedItem(RESPONSE_CODE_TAG).getNodeValue());
        } catch (NumberFormatException e) {
            String responseCodeText = attributes.getNamedItem(RESPONSE_CODE_TAG).getNodeValue();
            if (responseCodeText.contains(RESPONSE_CODE_EXCEPTION)
                    || responseCodeText.contains(RESPONSE_CODE_NON_HTTP)) {
                responseCode = RESULT_NOT_FOUND; // HTTP not found indicating an error
            }
        }

        // return if we should suppress this response code
        if (format.suppressResponseCode(responseCode)) {
            return metricMap;
        }

        String responseMessage = attributes.getNamedItem(RESPONSE_MESSAGE_TAG).getNodeValue();
        if (responseMessage.contains(RESPONSE_MESSAGE_TIMEOUT)) {
            // timeout
            responseCode = RESULT_OPERATION_TIMEOUT;
        } else if (responseMessage.contains(RESPONSE_MESSAGE_NON_HTTP)) {
            // other error
            responseCode = RESULT_NOT_FOUND; // HTTP not found indicating an error
        }
        
        //String successFlag = attributes.getNamedItem(SUCCESS_FLAG_TAG).getNodeValue();
        final int errorCount = Integer.parseInt(attributes.getNamedItem(ERROR_COUNT_TAG)
            .getNodeValue());
        String url = attributes.getNamedItem(TEST_URL_TAG).getNodeValue();
        
        String label = null;
        String normalizedURL = null;
        if (EpaUtils.getBooleanProperty(REPORT_LABELS_IN_PATH, false)) {
            label = "|" + url;//this pipe char is removed below, I am not sure why it was added
            label = normalizeURL(label);
        } else {
            normalizedURL = normalizeURL(url);
        }
        
        // report metrics
        String metric = EpaUtils.fixMetricName(metricTree + METRIC_PATH_SEPARATOR
                + format.formatStep(step, (label == null ? normalizedURL.replace("|", "") : label.replace("|", ""))) + METRIC_NAME_SEPARATOR);
        
        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "METRIC: " + metric);
        }
        
        NodeList stepChildren = stepNode.getChildNodes();
        
        //new logic for showing subtree for all steps
        if (EpaUtils.getBooleanProperty(REPORT_JTL_SUBTREE, false)) {
            String urlForMetric = new String(url);
            
            int innerStep = 1;
            int assertionStep = 1;
            
            //Walk through the elements of one step
            for (int j = 0; j < stepChildren.getLength(); j++) {
                Node stepChild = stepChildren.item(j);
                if (stepChild.getNodeType() == Node.ELEMENT_NODE && (stepChild.getNodeName()
                    .equals(HTTP_SAMPLE) || stepChild.getNodeName().equals(SAMPLE))) {
                    metricMap.putAll(reportJMeterStep(metric.substring(0, metric.length() - 1), innerStep, stepChild));
                    ++innerStep;
                } else if (stepChild.getNodeType() == Node.ELEMENT_NODE
                    && stepChild.getNodeName().equals(ASSERTION_RESULT)) {
                    NodeList assertionResultEntries = stepChild.getChildNodes();
                    
                    String failureMessage = UNDEFINED_ASSERTION;
                    boolean assertionFailure = false;
                    boolean assertionError = false;
                    String assertionName = "Response Assertion";
                    
                    for (int l = 0; l < assertionResultEntries.getLength(); l++) {
                        Node assertionResultEntry = assertionResultEntries.item(l);
                        
                        if (assertionResultEntry.getNodeName().equals(NAME_TAG)) {
                            assertionName = assertionResultEntry.getFirstChild().getNodeValue();
                        } else if (assertionResultEntry.getNodeName().equals(FAILURE_TAG)) {
                            assertionFailure = Boolean
                                .parseBoolean(assertionResultEntry.getFirstChild().getNodeValue());
                        } else if (assertionResultEntry.getNodeName().equals(ERROR_TAG)) {
                            assertionError = Boolean
                                .parseBoolean(assertionResultEntry.getFirstChild().getNodeValue());
                        } else if (assertionResultEntry.getNodeName().equals(FAILURE__MESSAGE_TAG)) {
                            failureMessage = assertionResultEntry.getFirstChild().getNodeValue();
                        }
                    }
                    
                    // report metrics
                    String metricAssert = EpaUtils.fixMetricName(metric.substring(0, metric.length() - 1) + METRIC_PATH_SEPARATOR
                            + format.formatStep(assertionStep, assertionName.replace("|", "")) + METRIC_NAME_SEPARATOR);
                    
                    metricMap.put(metricAssert + ASSERTION_NAME,            assertionName);
                    metricMap.put(metricAssert + ASSERTION_FAILURE,         Boolean.toString(assertionFailure));
                    metricMap.put(metricAssert + ASSERTION_ERROR,           Boolean.toString(assertionError));
                    
                    if (!failureMessage.equals(UNDEFINED_ASSERTION)) {
                        metricMap.put(metricAssert + ASSERTION_FAILURE_MSG, failureMessage);
                    }
                    ++assertionStep;
                } else if (stepChild.getNodeType() == Node.ELEMENT_NODE
                    && stepChild.getNodeName().equals(JAVA_NET_URL)) {
                    String text = stepChild.getTextContent();

                    //do not normalize here, since it is the metric value (original URL)
                    urlForMetric = text;

                }
            }

            int statusCode = format.mapResponseToStatusCode(responseCode);

            Date timeStamp = new java.util.Date(Long.parseLong(attributes.getNamedItem(TIMESTAMP_TAG).getNodeValue()));
            Long totalTime = Long.parseLong(attributes.getNamedItem(TOTAL_TIME_TAG).getNodeValue());
            Long resolveTime = Long.parseLong(attributes.getNamedItem(RESOLVE_TIME_TAG).getNodeValue());
            Long processingTime = Long.parseLong(attributes.getNamedItem(PROCESSING_TIME_TAG).getNodeValue());
            Long sizeInBytes = Long.parseLong(attributes.getNamedItem(SIZE_IN_BYTES_TAG).getNodeValue());
            Long sentBytes = Long.parseLong(attributes.getNamedItem(SENT_BYTES_TAG).getNodeValue());
            Boolean success = Boolean.parseBoolean(attributes.getNamedItem(SUCCESS_FLAG_TAG).getNodeValue());
            Integer sampleCount = Integer.parseInt(attributes.getNamedItem(SAMPLE_COUNT_TAG).getNodeValue());
            
            metricMap.put(metric + RESPONSE_MESSAGE,        responseMessage);
            metricMap.put(metric + RESPONSE_CODE,           Integer.toString(statusCode));
            metricMap.put(metric + SUCCESS,                 Boolean.toString(success));
            metricMap.put(metric + SAMPLE_COUNT,            Integer.toString(sampleCount));
            metricMap.put(metric + ERROR_COUNT,             Integer.toString(errorCount));
            metricMap.put(metric + TIMESTAMP,               timeStamp.toString());
            metricMap.put(metric + TOTAL_TIME,              Long.toString(totalTime));
            metricMap.put(metric + RESOLVE_TIME,            Long.toString(resolveTime));
            metricMap.put(metric + PROCESSING_TIME,         Long.toString(processingTime));
            metricMap.put(metric + SIZE_IN_BYTES,           Long.toString(sizeInBytes));
            metricMap.put(metric + SENT_BYTES,              Long.toString(sentBytes));
            metricMap.put(metric + TEST_URL,                urlForMetric);

            return metricMap;
        } else {

            int assertionFailures = 0;
            int assertionErrors = 0;
            
            boolean assertionAvailable = false;
            boolean assertionFailure = false;
            boolean assertionError = false;

            String failureMessage = UNDEFINED_ASSERTION;

//        int subStep = 0;

            // Walk through the elements of one step
            for (int j = 0; j < stepChildren.getLength(); j++) {
                Node stepChild = stepChildren.item(j);
                if (stepChild.getNodeType() == Node.ELEMENT_NODE
                    && stepChild.getNodeName().equals(ASSERTION_RESULT)) {
                    assertionAvailable = true;
                    NodeList assertionResultEntries = stepChild.getChildNodes();
                    for (int l = 0; l < assertionResultEntries.getLength(); l++) {
                        Node assertionResultEntry = assertionResultEntries.item(l);
//                    if (assertionResultEntry.getNodeName().equals(NAME_TAG)) {
//                        assertionName = assertionResultEntry.getFirstChild().getNodeValue();
//                    }
                        if (assertionResultEntry.getNodeName().equals(FAILURE_TAG)) {
                            assertionFailure = Boolean
                                .parseBoolean(assertionResultEntry.getFirstChild().getNodeValue());
                            if (assertionFailure) {
                                assertionFailures++;
                            }
                        } else if (assertionResultEntry.getNodeName().equals(ERROR_TAG)) {
                            assertionError = Boolean
                                .parseBoolean(assertionResultEntry.getFirstChild().getNodeValue());
                            if (assertionError) {
                                assertionErrors++;
                            }
                        } else if (assertionResultEntry.getNodeName()
                            .equals(FAILURE__MESSAGE_TAG)) {
                            failureMessage = assertionResultEntry.getFirstChild().getNodeValue();
                        }
                    }
                } else if (stepChild.getNodeType() == Node.ELEMENT_NODE
                    && stepChild.getNodeName().equals(JAVA_NET_URL)) {
                    String text = stepChild.getTextContent();

                    url = normalizeURL(text);
                }
            }

            // first map responseCode
            int statusCode = format.mapResponseToStatusCode(responseCode);
            String statusMessage = responseCode + " - " + responseMessage;

            // don't report assertion if we already have a http error
            if (assertionAvailable && (responseCode < RESULT_HTTP_CLIENT_ERROR)) {
                if (assertionFailure) {
                    statusMessage = failureMessage /* monitor + ASSERTION_FAILURE */;
                    statusCode = STATUS_CODE_ASSERTION_ERROR;
                }
                if (assertionError) {
                    statusMessage = failureMessage /* monitor + ASSERTION_ERROR */;
                    statusCode = STATUS_CODE_ASSERTION_ERROR;
                }

                // set status code for assertion failure
                if (STATUS_CODE_ASSERTION_ERROR == statusCode) {

                    String reportAs
                        = EpaUtils.getProperty(REPORT_ASSERTION_FAILURES_AS, EMPTY_STRING);

                    if (!EMPTY_STRING.equals(reportAs)) {
                        try {
                            statusCode = Integer.parseInt(reportAs);
                        } catch (NumberFormatException e) {
                            EpaUtils.getFeedback()
                                .warn(AsmMessages.getMessage(AsmMessages.NON_INT_PROPERTY_WARN_701,
                                    REPORT_ASSERTION_FAILURES_AS, reportAs));
                        }
                    }
                }
            }

            metricMap.put(metric + STATUS_MESSAGE_VALUE, Integer.toString(statusCode));
            metricMap.put(metric + RESPONSE_CODE, Integer.toString(responseCode));
            metricMap.put(metric + ERROR_COUNT, Integer.toString(errorCount));
            metricMap.put(metric + ASSERTION_FAILURES, Integer.toString(assertionFailures));
            metricMap.put(metric + ASSERTION_ERRORS, Integer.toString(assertionErrors));
            metricMap.put(metric + TEST_URL, url);

            if (EpaUtils.getBooleanProperty(REPORT_STRING_RESULTS, true)) {
                metricMap.put(metric + STATUS_MESSAGE, statusMessage);
            }

            return metricMap;
        }
    }
    
    private String normalizeURL(String url) {
        if ((null != url) && (0 < url.length())) {
            // lopal05: now normalize URL, we dont't want anything behind '?' as that
            // may result in metric tree explosion. Each new request creating new
            // path / element.
            int indexOfChar = url.indexOf(";");
            if (indexOfChar > 0) {
                url = url.substring(0, indexOfChar);
            }
            indexOfChar = url.indexOf("?");
            if (indexOfChar > 0) {
                url = url.substring(0, indexOfChar);
            }

            String normalizedURL = url;
            if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                EpaUtils.getFeedback().debug(module,
                    "replaced URL '" + url + "' with text '" + normalizedURL + "'");
            }
            return normalizedURL;
        }
        
        return url;
    }

    @Override
    public Handler getSuccessor() {
        return null;
    }
}
