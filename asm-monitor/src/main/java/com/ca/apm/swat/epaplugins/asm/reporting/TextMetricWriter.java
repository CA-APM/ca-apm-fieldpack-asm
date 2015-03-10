package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.Date;

/**
 * Dummy and Debug implementation for a defect writer.
 * Simply outputs the content to standard out.
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
public class TextMetricWriter implements MetricWriter {


  public void writeMetric(String type, String name, String metric) {
    fillTestValues();
    System.out.println("Type:" + type + " Name:" + name + " MetricValue:" + metric);
  }

  private void fillTestValues() {
    //do nothing

  }

  public void writeMetric(String type, String name, int metric) {
    fillTestValues();
    System.out.println("Type:" + type + " Name:" + name + " MetricValue:" + metric);
  }

  public void writeErrorMessage(String message) {
    System.out.println(message);
  }


  public void writeMetric(String type, String name, float metric) {
    fillTestValues();
    System.out.println("Type:" + type + " Name:" + name + " MetricValue:" + metric);
  }

  public void writeErrorDetectorEntry(String text, String resource) {
    fillTestValues();
    System.out.println("Type:" + "ErrorDetector" + " Name:" + resource + " Error" + " MetricValue:" + text);
  }

  public void writeLongSteadyAverageMetric(String name, long metric) {
    fillTestValues();
    System.out.println("Type:" + "LongAverage" + " Name:" + name + " MetricValue:" + metric);
  }

  public void writeStringMetric(String string, String string2) {
    System.out.println("Type:" + "String" + " Name:" + string + " MetricValue:" + string2);
  }

  public void writeIntCounter(String name, int metric) {
    fillTestValues();
    System.out.println("Type:" + "IntCounter" + " Name:" + name + " MetricValue:" + metric);
  }

  public void writeTimestamp(String string, Date date) {
    System.out.println("Type:" + "Timestamp" + " Name:" + string + " MetricValue:" + date);
  }

  public void writeLongAverage(String name, long metric) {
    System.out.println("Type:" + "LongAverage" + " Name:" + name + " MetricValue:" + metric);
  }

  public void writeIntCounterForceExist(String name, int metric) {
    fillTestValues();
    System.out.println("Type:" + "IntCounter" + " Name:" + name + " MetricValue:" + metric);
  }

  public void flushMetrics() {
    System.out.flush();
  }

}
