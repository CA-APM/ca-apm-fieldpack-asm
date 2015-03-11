package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Implementation for writing the Introscope data in REST format to the EPA
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class RestfulMetricWriter implements MetricWriter {

  private StringBuilder buf = null;
  private URL url = null;

  public final static String CONTENT_TYPE_HEADER = "content-type";
  public final static String CONTENT_TYPE_JSON = "application/json";

  public RestfulMetricWriter(URL url) {
    this.url = url;
  }
  
  protected StringBuilder getBuffer() {
    if (null == buf) {
      buf = new StringBuilder("{\"metrics\" : [");
    }
    return buf;
  }

  public void writeMetric(String type, String name, String metric) {
    getBuffer().append("{ \"type\" : \"" + type + "\", \"name\" : \"" + name + "\", \"value\" :\"" + metric + "\"},");
  }


  public void writeMetric(String type, String name, int metric) {
    getBuffer().append("{ \"type\" : \"" + type + "\", \"name\" : \"" + name + "\", \"value\" :\"" + metric + "\"},");
  }

  public void writeErrorMessage(String message) {
    writeMetric(MetricWriter.kStringEvent, "Error Message", message);
  }

  public void writeMetric(String type, String name, float metric) {
    getBuffer().append("{ \"type\" : \"" + type + "\", \"name\" : \"" + name + "\", \"value\" :\"" + new Integer(Math.round(metric)).toString() + "\"},");
  }

  public void writeErrorDetectorEntry(String text, String resource) {
    // TODO printStream.println("<event  resource=\"" + MetricWriter.apmcmProductNameShort + " Integration\"> <param name=\"Trace Type\" value=\"ErrorSnapshot\"/> <calledComponent  resource=\""
    //    + resource + "\"><param name=\"Error Message\" value=\"" + text + "\"/>  </calledComponent> </event>");
  }

  public void writeStringMetric(String string, String string2) {
    writeMetric(MetricWriter.kStringEvent, string, string2);
  }

  public void writeTimestamp(String string, Date date) {
    writeMetric(MetricWriter.kTimestamp, string, Long.toString(date.getTime()));
  }

  public void writeLongAverage(String string, long l) {
    writeMetric(MetricWriter.kLongAverage, string, Long.toString(l));
  }

  public void writeLongCounter(String string, long l) {
    writeMetric(MetricWriter.kLongCounter, string, Long.toString(l));
  }

  public void writeIntCounter(String name, int metric) {
    writeMetric(MetricWriter.kIntCounter, name, Integer.toString(metric));
  }

  public void writeIntCounterForceExist(String metricname, int metric) {
    writeMetric(MetricWriter.kIntCounter, metricname, Integer.toString(metric));
  }

  public void flushMetrics() throws IOException {
    if (null == buf) return;
    
    // remove last ',' and close brackets
    if (',' == buf.charAt(buf.length()-1)) {
      buf.deleteCharAt(buf.length()-1);
    }
    
    buf.append("] }");
    
    // send to EP agent
    URLConnection connection = url.openConnection();
    connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
    connection.setDoOutput(true);
    
    PrintStream outStream = new PrintStream(connection.getOutputStream());
    outStream.println(buf.toString());
    outStream.close();

    // reset buffer
    buf = null;

    DataInputStream inStream = new DataInputStream(connection.getInputStream());
    String inputLine;

    while ((inputLine = inStream.readLine()) != null) {
      // TODO: error handling
      // System.out.println(inputLine);
    }
    inStream.close();
  }

  public void writeIntAverage(String name, int metric) {
    writeMetric(MetricWriter.kIntAverage, name, Integer.toString(metric));
  }

  public void writeIntRate(String name, int metric) {
    writeMetric(MetricWriter.kIntRate, name, Integer.toString(metric));
  }

  public void writePerIntervalCounter(String name, int metric) {
    writeMetric(MetricWriter.kPerIntervalCounter, name, Integer.toString(metric));
  }
}
