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
import com.ca.apm.swat.epaplugins.utils.EPAConstants;

public class CloudMonitorMetricReporter {

  private MetricWriter metricWriter;
  private boolean apmcmDisplayMonitor;
  private HashMap<String, String> cpMap;
  private XMLAnalysisAdapter analysisAdapter;

  protected final static String SEPARATOR = "\\.";
  
  public CloudMonitorMetricReporter(
    MetricWriter metricWriter,
    boolean apmcmDisplayMonitor,
    HashMap<String, String> cpMap) {
    this.metricWriter = metricWriter;
    this.apmcmDisplayMonitor = apmcmDisplayMonitor;
    this.cpMap = cpMap;
    analysisAdapter = new XMLAnalysisAdapter(metricWriter);
  }

  public void printMetrics(HashMap<String, String> metric_map) throws Exception {
    Iterator<Map.Entry<String, String>> metricIt = metric_map.entrySet().iterator();
    while (metricIt.hasNext()) {
      Map.Entry<String, String> metricPairs = (Map.Entry<String, String>) metricIt.next();

      if (((String) metricPairs.getValue()).length() == 0)
        continue;
      String thisMetricType = returnMetricType((String) metricPairs.getValue());

      if (thisMetricType.equals(MetricWriter.kFloat)) {
        metricPairs.setValue(((String) metricPairs.getValue()).split(SEPARATOR)[0]);
        thisMetricType = MetricWriter.kIntCounter;
      }

      metricWriter.writeMetric(thisMetricType, EPAConstants.apmcmMetricTree + EPAConstants.kMetricPathSeparator + metricPairs.getKey(), metricPairs.getValue());
    }
  }

  private String returnMetricType(String thisMetric) {
    String metricType = MetricWriter.kStringEvent;
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
    return metricType;
  }

  public HashMap<String, String> resetMetrics(HashMap<String, String> metric_map) throws Exception {
    if (metric_map.size() != 0) {
      Iterator<Map.Entry<String, String>> metricIt = metric_map.entrySet().iterator();
      while (metricIt.hasNext()) {
        Map.Entry<String, String> metricPairs = (Map.Entry<String, String>) metricIt.next();

        if (!returnMetricType((String) metricPairs.getValue()).equals(MetricWriter.kStringEvent))
          metric_map.put((String) metricPairs.getKey(), EPAConstants.ZERO);
        else {
          metric_map.put((String) metricPairs.getKey(), EPAConstants.EMPTY_STRING);
        }
      }
    }

    return metric_map;
  }

  protected HashMap<String, String> generateMetrics(String jsonString, String metricTree) throws Exception {
    HashMap<String, String> metric_map = new HashMap<String, String>();

    JSONObject thisJO = new JSONObject(jsonString);

    if (thisJO.optString(EPAConstants.kAPMCMName, null) != null) {
      metricTree = metricTree + EPAConstants.kMetricPathSeparator + thisJO.getString(EPAConstants.kAPMCMName);
    }

    if (apmcmDisplayMonitor) {
      if (thisJO.optString(EPAConstants.kAPMCMLoc, null) != null) {
        metricTree = metricTree + EPAConstants.kMetricPathSeparator + (String) this.cpMap.get(thisJO.getString(EPAConstants.kAPMCMLoc));
      }
    }

    Iterator thisJOKeys = thisJO.keys();
    while (thisJOKeys.hasNext()) {
      String thisKey = thisJOKeys.next().toString();

      if (thisJO.optJSONObject(thisKey) != null) {
        JSONObject innerJO = thisJO.getJSONObject(thisKey);
        metric_map.putAll(generateMetrics(innerJO.toString(), metricTree));
      } else if (thisJO.optJSONArray(thisKey) != null) {
        JSONArray innerJA = thisJO.optJSONArray(thisKey);
        for (int i = 0; i < innerJA.length(); i++) {
          if ((thisKey.equals(EPAConstants.kAPMCMResult)) || (thisKey.equals(EPAConstants.kAPMCMMonitors)) || (thisKey.equals(EPAConstants.kAPMCMStats)))
            metric_map.putAll(generateMetrics(innerJA.getJSONObject(i).toString(), metricTree));
          else {
            metric_map.putAll(generateMetrics(innerJA.getJSONObject(i).toString(), metricTree + EPAConstants.kMetricPathSeparator + thisKey));
          }
        }
      } else {
        if ((thisKey.equals(EPAConstants.kAPMCMCode)) || (thisKey.equals(EPAConstants.kAPMCMElapsed)) || (thisKey.equals(EPAConstants.kAPMCMInfo))
          || (thisKey.equals(EPAConstants.kAPMCMVersion)) || (thisJO.optString(thisKey, EPAConstants.EMPTY_STRING).length() == 0))
          continue;
        String thisValue = thisJO.getString(thisKey);

        if (thisKey.equals(EPAConstants.kAPMCMDescr)) {
          String rawErrorMetric = metricTree + EPAConstants.kMetricNameSeparator + (String) EPAConstants.apmcmMetrics.get(EPAConstants.kAPMCMErrors);
          metric_map.put(CloudMonitorRequestHelper.fixMetric(rawErrorMetric), EPAConstants.ONE);
        }

        if (thisKey.equals(EPAConstants.kAPMCMColor)) {
          String rawErrorMetric = metricTree + EPAConstants.kMetricNameSeparator + (String) EPAConstants.apmcmMetrics.get(EPAConstants.kAPMCMColors);
          if (EPAConstants.apmcmColors.containsKey(thisValue))
            metric_map.put(
              CloudMonitorRequestHelper.fixMetric(rawErrorMetric),
              (String) EPAConstants.apmcmColors.get(thisValue));
          else {
            metric_map.put(CloudMonitorRequestHelper.fixMetric(rawErrorMetric), EPAConstants.ZERO);
          }

        }

        if (EPAConstants.apmcmMetrics.containsKey(thisKey)) {
          thisKey = ((String) EPAConstants.apmcmMetrics.get(thisKey)).toString();
        }

        if (thisKey.equalsIgnoreCase(EPAConstants.kAPMCMOutput)) {

          //Handled different
          continue;
        }

        String rawMetric = metricTree + EPAConstants.kMetricNameSeparator + thisKey;
        metric_map.put(CloudMonitorRequestHelper.fixMetric(rawMetric), CloudMonitorRequestHelper.fixMetric(thisValue));
      }
    }

    return metric_map;
  }

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

  class NullResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      return new InputSource(new StringReader(EPAConstants.EMPTY_STRING));
    }
  }

  public void analyzeContentResults(String jsonString, String folder, HashMap<String, String> metric_map)
      throws JSONException {

    JSONObject thisJO = new JSONObject(jsonString);

    String name = EPAConstants.kAPMCMUndefined;
    if (thisJO.optString(EPAConstants.kAPMCMName, null) != null) {
      name = thisJO.getString(EPAConstants.kAPMCMName);
    }
    Iterator thisJOKeys = thisJO.keys();
    while (thisJOKeys.hasNext()) {
      String thisKey = thisJOKeys.next().toString();

      if (thisJO.optJSONObject(thisKey) != null) {
        JSONObject innerJO = thisJO.getJSONObject(thisKey);
        analyzeContentResults(innerJO.toString(), folder, metric_map);
      } else if (thisJO.optJSONArray(thisKey) != null) {
        JSONArray innerJA = thisJO.optJSONArray(thisKey);
        for (int i = 0; i < innerJA.length(); i++) {
          analyzeContentResults(innerJA.getJSONObject(i).toString(), folder, metric_map);
        }
      } else {
        if ((thisKey.equals(EPAConstants.kAPMCMColor)) || (thisKey.equals(EPAConstants.kAPMCMElapsed)) || (thisKey.equals(EPAConstants.kAPMCMInfo))
          || (thisKey.equals(EPAConstants.kAPMCMVersion)) || (thisJO.optString(thisKey, EPAConstants.EMPTY_STRING).length() == 0))
          continue;
        String thisValue = thisJO.getString(thisKey);

        // TODO: separate into different components - chain of responsibility discover, decode, handle
        if (thisKey.equalsIgnoreCase(EPAConstants.kAPMCMOutput)) {
          try {
            String originalString = thisValue;
            byte[] decoded = Base64.decodeBase64(originalString);
            if (decoded != null) {
              byte[] bytesDecompressed = decompress(decoded);
              if (bytesDecompressed != null) {
                String returnValue = new String(bytesDecompressed, 0, bytesDecompressed.length, EPAConstants.UTF8);
                if (returnValue.startsWith(EPAConstants.kXMLPrefix)) {
                  analysisAdapter.analyzeXML(returnValue, folder, name, metric_map);
                } else {
                  if (returnValue.startsWith(EPAConstants.kAPMCMHarOrLog)) {
                    //Do nothing - already have seen it. and we don't need this log
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
