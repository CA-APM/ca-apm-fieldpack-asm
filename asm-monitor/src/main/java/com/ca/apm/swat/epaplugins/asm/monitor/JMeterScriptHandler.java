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
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

public class JMeterScriptHandler implements Handler, AsmProperties {

    protected Handler successor = null;  

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    /**
     * Generate metrics from API call result. 
     * @param xmlString JMeter script data
     * @param metricTree metric tree prefix
     * @return map containing the metrics
     */
    public MetricMap generateMetrics(String xmlString, String metricTree) {

        MetricMap metricMap = new MetricMap();

        if (EpaUtils.getFeedback().isDebugEnabled()) {
            EpaUtils.getFeedback().debug("JMeterScriptHandler - xmlString = " + xmlString);
        }

        if (!xmlString.startsWith(XML_PREFIX)) {
            if (xmlString.startsWith(HAR_OR_LOG_TAG)) {
                //System.out.println(xmlString);
                // Do nothing - already have seen it.
                // and we don't need this log
            }
            return metricMap;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            Document document = builder.parse(is);

            NodeList testResults = document.getElementsByTagName(TEST_RESULTS);
            
            int step = 1; // start from 1, business not engineering
            if (testResults.getLength() > 0) {

                if (EpaUtils.getFeedback().isDebugEnabled()) {
                    EpaUtils.getFeedback().debug("JMeterScriptHandler: "
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
        int assertionFailures = 0;
        int assertionErrors = 0;
        Formatter format = Formatter.getInstance();
        MetricMap metricMap = new MetricMap();

        if (EpaUtils.getFeedback().isDebugEnabled()) {
            EpaUtils.getFeedback().debug("reportJMeterStep " + step + ": "
                    + stepNode.toString());
        }

        //First the attributes
        NamedNodeMap attributes = stepNode.getAttributes();
        
        int responseCode = Integer.parseInt(
            attributes.getNamedItem(RESPONSE_CODE_TAG).getNodeValue());

        // return if we should suppress this response code
        if (format.suppressResponseCode(responseCode)) {
            return metricMap;
        }
        
        String responseMessage = attributes.getNamedItem(RESPONSE_MESSAGE_TAG).getNodeValue();
        //String successFlag = attributes.getNamedItem(SUCCESS_FLAG_TAG).getNodeValue();
        final int errorCount = Integer.parseInt(attributes.getNamedItem(ERROR_COUNT_TAG)
            .getNodeValue());
        String url = attributes.getNamedItem(TEST_URL_TAG).getNodeValue();
        boolean assertionAvailable = false;
        boolean assertionFailure = false;
        boolean assertionError = false;
        // String assertionName = UNDEFINED_ASSERTION;

        NodeList stepChildren = stepNode.getChildNodes();

        //Walk through the elements of one step
        for (int j = 0; j < stepChildren.getLength(); j++) {
            Node stepChild = stepChildren.item(j);
            if (stepChild.getNodeType() == Node.ELEMENT_NODE && stepChild.getNodeName()
                    .equals(ASSERTION_RESULT)) {
                assertionAvailable = true;
                NodeList assertionResultEntries = stepChild.getChildNodes();
                for (int l = 0; l < assertionResultEntries.getLength(); l++) {
                    Node assertionResultEntry = assertionResultEntries.item(l);
//                    if (assertionResultEntry.getNodeName().equals(NAME_TAG)) {
//                        assertionName = assertionResultEntry.getFirstChild().getNodeValue();
//                    }
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
        
        url = EpaUtils.fixMetric(url);

        //Collect results
        String statusMessage = null;
        int statusCode = STATUS_CODE_OK;
        boolean assertionFailed = false;
        if (assertionAvailable) {
            if (assertionFailure) {
                assertionFailed = true;
                statusMessage = /* monitor + */ ASSERTION_FAILURE;
                statusCode = STATUS_CODE_ASSERTION_ERROR;
                assertionFailures++;
            }
            if (assertionError) {
                assertionFailed = true;
                statusMessage = /* monitor + */ ASSERTION_ERROR;
                statusCode = STATUS_CODE_ASSERTION_ERROR;
                assertionErrors++;
            }
        }
        
        // set status message
        if (!assertionFailed) {
            statusMessage = responseCode + " - " + responseMessage;
        }

        // set status code for assertion failure 
        if (STATUS_CODE_ASSERTION_ERROR == statusCode) {
            String reportAs = EpaUtils.getProperty(REPORT_ASSERTION_FAILURES_AS, EMPTY_STRING);

            if (!EMPTY_STRING.equals(reportAs)) {
                try {
                    statusCode = Integer.parseInt(reportAs);
                } catch (NumberFormatException e) {
                    EpaUtils.getFeedback().warn("non-integer value found in "
                            + REPORT_ASSERTION_FAILURES_AS + ": " + reportAs);
                }
            }
        } else {
            // map responseCode
            statusCode = format.mapResponseToStatusCode(responseCode);
        }
        
        // report metrics
        String metric = EpaUtils.fixMetric(metricTree + METRIC_PATH_SEPARATOR
            + EpaUtils.fixMetric(format.formatStep(step, url)));
        
        metricMap.put(metric + METRIC_NAME_SEPARATOR + STATUS_MESSAGE_VALUE,
            Integer.toString(statusCode));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + RESPONSE_CODE,
            Integer.toString(responseCode));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + ERROR_COUNT,
            Integer.toString(errorCount));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + ASSERTION_FAILURES,
            Integer.toString(assertionFailures));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + ASSERTION_ERRORS,
            Integer.toString(assertionErrors));
        metricMap.put(metric + METRIC_NAME_SEPARATOR + TEST_URL,
            EpaUtils.fixMetric(url));

        if (TRUE.equals(EpaUtils.getProperty(REPORT_STRING_RESULTS, TRUE))) {
            metricMap.put(metric + METRIC_NAME_SEPARATOR + STATUS_MESSAGE,
                statusMessage);
        }

        return metricMap;
    }

}
