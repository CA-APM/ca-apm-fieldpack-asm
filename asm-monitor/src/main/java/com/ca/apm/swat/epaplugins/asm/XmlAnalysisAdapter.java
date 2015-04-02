package com.ca.apm.swat.epaplugins.asm;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;

/**
 * Analyze XML from logs.
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
@SuppressWarnings("restriction")
public class XmlAnalysisAdapter implements AsmProperties {

    /**
     * Analyze the XML returned by the get logs API. 
     * @param returnValue API call result
     * @param folder folder name
     * @param rule rule name
     * @param metricMap map containing the metrics
     */
    public void analyzeXml(String returnValue,
                           String folder,
                           String rule,
                           HashMap<String,
                           String> metricMap) {
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
            CloudMonitorRequestHelper.fixMetric(url));
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
