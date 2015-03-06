package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.Date;


/**
 * Wrapper for writing the Introscope data to the EPA framework
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface MetricWriter {

  void writeMetric(String type, String name, int metric);

  void writeMetric(String type, String name, String metric);

  void writeErrorMessage(String message);

  void writeMetric(String type, String name, float metric);

  void writeErrorDetectorEntry(String text, String resource);

  void writeStringMetric(String name, String metric);
  
  public void writeIntCounter(String name, int metric);
  
  public void writeTimestamp(String name, Date date);

  void writeLongAverage(String name, long l);

  void writeIntCounterForceExist(String name, int i);
  
  void flushMetrics();
}
