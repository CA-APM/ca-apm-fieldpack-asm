package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.IOException;
import java.util.Date;


/**
 * Wrapper for writing the Introscope data to the EPA framework
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface MetricWriter {

  public static final String kPerIntervalCounter = "PerIntervalCounter";
  public static final String kIntCounter = "IntCounter";
  public static final String kIntAverage = "IntAverage";
  public static final String kIntRate = "IntRate";
  public static final String kLongCounter = "LongCounter";
  public static final String kLongAverage = "LongAverage";
  public static final String kStringEvent = "StringEvent";
  public static final String kTimestamp = "Timestamp";
  public static final String kFloat = "Float";

  public void writeMetric(String type, String name, int metric);

  public void writeMetric(String type, String name, String metric);

  // TODO: really write generic error message?
  public void writeErrorMessage(String message);

  public void writeMetric(String type, String name, float metric);

  public void writeErrorDetectorEntry(String text, String resource);

  public void writeStringMetric(String name, String metric);
  
  public void writeIntCounter(String name, int metric);
  
  public void writeTimestamp(String name, Date date);

  public void writeLongAverage(String name, long l);

  public void writeIntCounterForceExist(String name, int i);
  
  public void flushMetrics() throws IOException;
}
