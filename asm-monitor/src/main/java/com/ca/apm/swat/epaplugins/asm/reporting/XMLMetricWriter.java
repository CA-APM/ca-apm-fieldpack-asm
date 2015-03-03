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
 * Implementation for writing the Introscope data in XML format to the EPA
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
public class XMLMetricWriter implements MetricWriter {

  private final PrintStream printStream;
  private IAgent agent;

  public XMLMetricWriter(PrintStream printStream) {
    this.printStream = printStream;
    if (agent == null) {
      try {
        agent = AgentShim.getAgent();
      } catch (AgentNotAvailableException e) {
        e.printStackTrace();
      }
    }
  }

  public void writeIntCounter(String name, int metric) {
    com.wily.introscope.epagent.api.PerIntervalCounterDataRecorder recorder;
    try {
      recorder = DataRecorderFactory.createPerIntervalCounterDataRecorder(name);
      recorder.recordMultipleIncidents(metric);
    } catch (Exception e) {
      PropertiesReader.getFeedback().error(e);
    }
  }

  public void writeMetric(String type, String name, String metric) {
    printStream.println(" <metric type=\"" + type + "\" name=\"" + name + "\" value=\"" + metric + "\" />");
  }

  public void writeMetric(String type, String name, int metric) {
    printStream.println(" <metric type=\"" + type + "\" name=\"" + name + "\" value=\"" + metric + "\" />");
  }

  public void writeErrorMessage(String message) {
    writeMetric("StringEvent", EPAConstants.apmcmProductNameShort + " Integration:Error Message", message);
  }

  public void writeMetric(String type, String name, float metric) {
    printStream.println(" <metric type=\"" + type + "\" name=\"" + name + "\" value=\""
      + new Integer(Math.round(metric)).toString() + "\" />");
  }

  public void writeErrorDetectorEntry(String text, String resource) {
    printStream.println("<event  resource=\"" + EPAConstants.apmcmProductNameShort + " Integration\"> <param name=\"Trace Type\" value=\"ErrorSnapshot\"/> <calledComponent  resource=\""
      + resource + "\"><param name=\"Error Message\" value=\"" + text + "\"/>  </calledComponent> </event>");
  }

  public void writeStringMetric(String string, String string2) {
    printStream.println(" <metric type=\"StringEvent\" name=\"" + string + "\" value=\"" + string2 + "\" />");
  }

  public void writeTimestamp(String string, Date date) {
    TimestampDataRecorder recorder;
    try {
      recorder = DataRecorderFactory.createTimestampDataRecorder(string);
      recorder.recordTimestamp(date.getTime());
    } catch (Exception e) {
      PropertiesReader.getFeedback().error(e);
    }
  }

  public void writeLongAverage(String string, long l) {
    LongAverageDataRecorder recorder;
    try {
      recorder = DataRecorderFactory.createLongAverageDataRecorder(string);
      recorder.recordDataPoint(l);
    } catch (Exception e) {
      PropertiesReader.getFeedback().error(e);
    }
  }
  public void writeLongCounter(String string, long l) {
    LongCounterDataRecorder recorder;
    try {
      recorder = DataRecorderFactory.createLongCounterDataRecorder(string);
      recorder.add(l);
    } catch (Exception e) {
      PropertiesReader.getFeedback().error(e);
    }
  }

  public void writeIntCounterForceExist(String metricname, int metric) {
    try {
      ILongIntervalCounterDataAccumulator counterDataAccumulator = agent.IAgent_getDataAccumulatorFactory().safeGetLongIntervalCounterDataAccumulator(
        metricname);
      counterDataAccumulator.forceMetricToExist(null);
      counterDataAccumulator.ILongIntervalCounterDataAccumulator_addBatchIncidents(metric);
    } catch (Exception ex) {
      PropertiesReader.getFeedback().error(ex);
    }
  }


}
