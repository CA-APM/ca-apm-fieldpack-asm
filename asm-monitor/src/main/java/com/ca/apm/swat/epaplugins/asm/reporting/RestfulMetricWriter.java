package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.PrintStream;
import java.util.Date;

import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.wily.introscope.agent.AgentNotAvailableException;
import com.wily.introscope.agent.AgentShim;
import com.wily.introscope.agent.IAgent;
import com.wily.introscope.agent.stat.ILongIntervalCounterDataAccumulator;
import com.wily.introscope.epagent.PropertiesReader;
import com.wily.introscope.epagent.api.DataRecorderFactory;
import com.wily.introscope.epagent.api.LongAverageDataRecorder;
import com.wily.introscope.epagent.api.LongCounterDataRecorder;
import com.wily.introscope.epagent.api.TimestampDataRecorder;

/**
 * Implementation for writing the Introscope data in REST format to the EPA
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class RestfulMetricWriter implements MetricWriter {

  private StringBuffer buf = null;

  protected StringBuffer getBuffer() {
    if (null == buf) {
      buf = new StringBuffer("{\"metrics\" : [");
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
    writeMetric(EPAConstants.kStringEvent, EPAConstants.apmcmProductNameShort + " Integration:Error Message", message);
  }

  public void writeMetric(String type, String name, float metric) {
    getBuffer().append("{ \"type\" : \"" + type + "\", \"name\" : \"" + name + "\", \"value\" :\"" + new Integer(Math.round(metric)).toString() + "\"},");
  }

  public void writeErrorDetectorEntry(String text, String resource) {
    // TODO printStream.println("<event  resource=\"" + EPAConstants.apmcmProductNameShort + " Integration\"> <param name=\"Trace Type\" value=\"ErrorSnapshot\"/> <calledComponent  resource=\""
    //    + resource + "\"><param name=\"Error Message\" value=\"" + text + "\"/>  </calledComponent> </event>");
  }

  public void writeStringMetric(String string, String string2) {
    writeMetric(EPAConstants.kStringEvent, string, string2);
  }

  public void writeTimestamp(String string, Date date) {
    writeMetric(EPAConstants.kTimestamp, string, date.getTime());
  }

  public void writeLongAverage(String string, long l) {
    writeMetric(EPAConstants.kLongAverage, string, Long.toString(l));
  }

  public void writeLongCounter(String string, long l) {
    writeMetric(EPAConstants.kLongCounter, string, Long.toString(l));
  }

  public void writeIntCounterForceExist(String metricname, int metric) {
    writeMetric(EPAConstants.kIntCounter, metricname, Integer.toString(metric));
  }

  public void flushMetrics() {
    // TODO: remove last ',' and close brackets
    // TODO: send to agent
    // TODO: error handling
    // TODO: reset buffer
  }

  public void writeIntCounter(String name, int metric) {
    writeMetric(EPAConstants.kIntCounter, name, Integer.toString(metric));
  }


}
