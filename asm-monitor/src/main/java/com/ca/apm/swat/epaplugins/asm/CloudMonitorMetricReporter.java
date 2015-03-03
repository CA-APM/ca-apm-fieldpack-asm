package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Inflater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import sun.misc.BASE64Decoder;
import org.apache.commons.codec.binary.Base64;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.EPAConstants;

public class CloudMonitorMetricReporter {

  private MetricWriter metricWriter;
  private boolean apmcmDisplayMonitor;
  private HashMap<String, String> cpMap;
  private XMLAnalysisAdapter analysisAdapter;

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

      if (thisMetricType.equals("Float")) {
        metricPairs.setValue(((String) metricPairs.getValue()).split("\\.")[0]);
        thisMetricType = "IntCounter";
      }

      metricWriter.writeMetric(thisMetricType, "APM Cloud Monitor" + "|" + metricPairs.getKey(), metricPairs.getValue());
    }
  }

  private String returnMetricType(String thisMetric) {
    String metricType = "StringEvent";
    try {
      new Integer(thisMetric);
      metricType = "IntCounter";
    } catch (NumberFormatException e) {
      try {
        new Long(thisMetric);
        metricType = "LongCounter";
      } catch (NumberFormatException ee) {
        try {
          new Float(thisMetric);
          metricType = "Float";
        } catch (NumberFormatException eee) {
          metricType = "StringEvent";
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

        if (!returnMetricType((String) metricPairs.getValue()).equals("StringEvent"))
          metric_map.put((String) metricPairs.getKey(), "0");
        else {
          metric_map.put((String) metricPairs.getKey(), "");
        }
      }
    }

    return metric_map;
  }

  protected HashMap<String, String> generateMetrics(String jsonString, String metricTree) throws Exception {
    HashMap<String, String> metric_map = new HashMap<String, String>();

    JSONObject thisJO = new JSONObject(jsonString);

    if (thisJO.optString("name", null) != null) {
      metricTree = metricTree + "|" + thisJO.getString("name");
    }

    if (apmcmDisplayMonitor) {
      if (thisJO.optString("loc", null) != null) {
        metricTree = metricTree + "|" + (String) this.cpMap.get(thisJO.getString("loc"));
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
          if ((thisKey.equals("result")) || (thisKey.equals("monitors")) || (thisKey.equals("stats")))
            metric_map.putAll(generateMetrics(innerJA.getJSONObject(i).toString(), metricTree));
          else {
            metric_map.putAll(generateMetrics(innerJA.getJSONObject(i).toString(), metricTree + "|" + thisKey));
          }
        }
      } else {
        if ((thisKey.equals("code")) || (thisKey.equals("elapsed")) || (thisKey.equals("info"))
          || (thisKey.equals("version")) || (thisJO.optString(thisKey, "").length() == 0))
          continue;
        String thisValue = thisJO.getString(thisKey);

        if (thisKey.equals("descr")) {
          String rawErrorMetric = metricTree + ":" + (String) EPAConstants.apmcmMetrics.get("errors");
          metric_map.put(CloudMonitorRequestHelper.fixMetric(rawErrorMetric), "1");
        }

        if (thisKey.equals("color")) {
          String rawErrorMetric = metricTree + ":" + (String) EPAConstants.apmcmMetrics.get("colors");
          if (EPAConstants.apmcmColors.containsKey(thisValue))
            metric_map.put(
              CloudMonitorRequestHelper.fixMetric(rawErrorMetric),
              (String) EPAConstants.apmcmColors.get(thisValue));
          else {
            metric_map.put(CloudMonitorRequestHelper.fixMetric(rawErrorMetric), "0");
          }

        }

        if (EPAConstants.apmcmMetrics.containsKey(thisKey)) {
          thisKey = ((String) EPAConstants.apmcmMetrics.get(thisKey)).toString();
        }

        if (thisKey.equalsIgnoreCase("output")) {

          //Handled different
          continue;
        }

        String rawMetric = metricTree + ":" + thisKey;
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
      return new InputSource(new StringReader(""));
    }
  }

  public void analyzeContentResults(String jsonString, String folder, HashMap<String, String> metric_map)
      throws JSONException {

    JSONObject thisJO = new JSONObject(jsonString);

    String name = "Undefined";
    if (thisJO.optString("name", null) != null) {
      name = thisJO.getString("name");
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
        if ((thisKey.equals("code")) || (thisKey.equals("elapsed")) || (thisKey.equals("info"))
          || (thisKey.equals("version")) || (thisJO.optString(thisKey, "").length() == 0))
          continue;
        String thisValue = thisJO.getString(thisKey);

        // TODO: separate into different components - chain of responsibility discover, decode, handle
        if (thisKey.equalsIgnoreCase("output")) {
          try {
            String originalString = thisValue;
            byte[] decoded = Base64.decodeBase64(originalString);
            if (decoded != null) {
              byte[] bytesDecompressed = decompress(decoded);
              if (bytesDecompressed != null) {
                String returnValue = new String(bytesDecompressed, 0, bytesDecompressed.length, "UTF-8");
                if (returnValue.startsWith("<?xml")) {
                  analysisAdapter.analyzeXML(returnValue, folder, name, metric_map);
                } else {
                  if (returnValue.startsWith("{\"har\": {\"log\"")) {
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