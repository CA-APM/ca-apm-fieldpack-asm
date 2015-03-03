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

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;

public class XMLAnalysisAdapter {

  //Todo, better to use the Metric Writer in future with all functions here.
  private MetricWriter metricWriter;

  public XMLAnalysisAdapter(MetricWriter metricWriter) {
    this.metricWriter = metricWriter;
  }

  public void analyzeXML(String returnValue, String folder, String name, HashMap<String, String> metric_map) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(returnValue));
      Document document = builder.parse(is);

      NodeList testResults = document.getElementsByTagName("testResults");
      int step = 0;
      if (testResults.getLength() > 0) {
        //We have Jmeter scripts
        org.w3c.dom.Node httpTestNode = testResults.item(0);

        //Working through the steps
        NodeList stepNodes = httpTestNode.getChildNodes();
        for (int i = 0; i < stepNodes.getLength(); i++) {

          org.w3c.dom.Node stepNode = stepNodes.item(i);
          if (stepNode.getNodeType() == Node.ELEMENT_NODE) {
            step = reportJMeterStep(folder, name, step, stepNode, metric_map);
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      //don't throw, simply quit. In future we have to implement it for multiple different XML formats.
    }

  }

  private int reportJMeterStep(String folder, String name, int step, org.w3c.dom.Node stepNode,
    HashMap<String, String> metric_map) {
    int assertionFailures = 0;
    int assertionErrors = 0;
    step++;
    //First the attributes
    NamedNodeMap attributes = stepNode.getAttributes();
    int responsecode = Integer.parseInt(attributes.getNamedItem("rc").getNodeValue());
    String responsemessage = attributes.getNamedItem("rm").getNodeValue();
    String successFlag = attributes.getNamedItem("s").getNodeValue();
    int errorCount = Integer.parseInt(attributes.getNamedItem("ec").getNodeValue());
    String url = attributes.getNamedItem("lb").getNodeValue();
    boolean assertionAvailable = false;
    boolean assertionFailure = false;
    boolean assertionError = false;
    String assertionName = "Undefined Assertion";

    NodeList stepChildren = stepNode.getChildNodes();
    //Walk Through the elements of one step
    for (int j = 0; j < stepChildren.getLength(); j++) {
      org.w3c.dom.Node stepChild = stepChildren.item(j);
      if (stepChild.getNodeType() == Node.ELEMENT_NODE && stepChild.getNodeName().equals("assertionResult")) {
        assertionAvailable = true;
        NodeList assertionResultEntries = stepChild.getChildNodes();
        for (int l = 0; l < assertionResultEntries.getLength(); l++) {
          org.w3c.dom.Node assertionResultEntry = assertionResultEntries.item(l);
          if (assertionResultEntry.getNodeName().equals("name")) {
            assertionName = assertionResultEntry.getFirstChild().getNodeValue();
          }
          if (assertionResultEntry.getNodeName().equals("failure")) {
            assertionFailure = Boolean.parseBoolean(assertionResultEntry.getFirstChild().getNodeValue());
          }
          if (assertionResultEntry.getNodeName().equals("error")) {
            assertionError = Boolean.parseBoolean(assertionResultEntry.getFirstChild().getNodeValue());
          }
        }
      }
    }
    url = url.replace(":", "-");
    url = url.replace("|", "-");
    String metric = "Status Monitoring|" + folder + "|" + name + "|" + "Step " + step + " " + url;
    //Collect results
    String statusMessage = null;
    int statusCode = 1;
    boolean assertionFailed = false;
    if (assertionAvailable) {
      if (assertionFailure) {
        assertionFailed = true;
        statusMessage = name + " - Assertion Failure";
        statusCode = 3;
        assertionFailures++;
      }
      if (assertionError) {
        assertionFailed = true;
        statusMessage = name + " - Assertion Error";
        statusCode = 3;
        assertionErrors++;
      }
    }
    if (!assertionFailed) {
      statusMessage = responsecode + " - " + responsemessage;
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
      }
    }

    //Report metrics
    metric_map.put(metric + ":Status Message", statusMessage);
    metric_map.put(metric + ":Status Message Value", Integer.toString(statusCode));
    metric_map.put(metric + ":Response Code", Integer.toString(responsecode));
    metric_map.put(metric + ":Error Count", Integer.toString(errorCount));
    metric_map.put(metric + ":Assertion Failures", Integer.toString(assertionFailures));
    metric_map.put(metric + ":Assertion Errors", Integer.toString(assertionErrors));
    metric_map.put(metric + ":URL", CloudMonitorRequestHelper.fixMetric(url));
    return step;
  }
}
