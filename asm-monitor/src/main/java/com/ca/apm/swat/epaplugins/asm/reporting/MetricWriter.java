package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.Date;


/**
 * Wrapper for writing the Introscope data to the EPA framework
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
public interface MetricWriter {

  void writeMetric(String type, String name, int metric);

  void writeMetric(String type, String name, String metric);

  void writeErrorMessage(String message);

  void writeMetric(String type, String name, float metric);

  void writeErrorDetectorEntry(String text, String resource);

  void writeStringMetric(String string, String string2);
  
  public void writeIntCounter(String name, int metric);
  
  public void writeTimestamp(String string, Date date);

  void writeLongAverage(String string, long l);

  void writeIntCounterForceExist(String summarymetricName, int i);
}
