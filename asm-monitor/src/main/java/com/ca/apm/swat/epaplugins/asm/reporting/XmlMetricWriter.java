package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.PrintStream;
import java.util.Date;

import com.wily.introscope.agent.AgentNotAvailableException;
import com.wily.introscope.agent.AgentShim;
import com.wily.introscope.agent.IAgent;
import com.wily.introscope.agent.stat.ILongIntervalCounterDataAccumulator;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.introscope.epagent.api.DataRecorderFactory;
import com.wily.introscope.epagent.api.LongAverageDataRecorder;
import com.wily.introscope.epagent.api.LongCounterDataRecorder;
import com.wily.introscope.epagent.api.TimestampDataRecorder;

/**
 * Implementation for writing the Introscope data in XML format to the EPA.
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class XmlMetricWriter implements MetricWriter {

    private final PrintStream printStream;
    private IAgent agent;

    /**
     * Create new XmlMetricWriter with PrintStream to write xml to.
     * @param printStream the output stream that is read by EPAgent
     */
    public XmlMetricWriter(PrintStream printStream) {
        this.printStream = printStream;
        if (agent == null) {
            try {
                agent = AgentShim.getAgent();
            } catch (AgentNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write an IntCounter metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntCounter(String name, int metric) {
        com.wily.introscope.epagent.api.PerIntervalCounterDataRecorder recorder;
        try {
            recorder = DataRecorderFactory.createPerIntervalCounterDataRecorder(name);
            recorder.recordMultipleIncidents(metric);
        } catch (Exception e) {
            EpaUtils.getFeedback().error(e);
        }
    }

    public void writeMetric(String type, String name, String metric) {
        printStream.println(" <metric type=\"" + type + "\" name=\""
                + name + "\" value=\"" + metric + "\" />");
    }

    public void writeMetric(String type, String name, int metric) {
        printStream.println(" <metric type=\"" + type + "\" name=\""
                + name + "\" value=\"" + metric + "\" />");
    }

    public void writeMetric(String type, String name, long metric) {
        printStream.println(" <metric type=\"" + type + "\" name=\""
                + name + "\" value=\"" + metric + "\" />");
    }

    /**
     * Write a float metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, float metric) {
        printStream.println(" <metric type=\"" + type + "\" name=\"" + name + "\" value=\""
                + new Integer(Math.round(metric)).toString() + "\" />");
    }

    public void writeErrorMessage(String message) {
        writeMetric(MetricWriter.kStringEvent, "Error Message", message);
    }

    /**
     * Write an error detector entry (error snapshot).
     * @param text error text
     * @param resource resource
     */
    public void writeErrorDetectorEntry(String text, String resource) {
        printStream.println("<event  resource=\"" + resource + ">"
                + "<param name=\"Trace Type\" value=\"ErrorSnapshot\"/>"
                + "<calledComponent resource=\"" + resource + "\">"
                + "<param name=\"Error Message\" value=\"" + text + "\"/>"
                + "</calledComponent> </event>");
    }

    public void writeStringMetric(String name, String metric) {
        printStream.println(" <metric type=\"" + MetricWriter.kStringEvent
            + "\" name=\"" + MetricWriter.kStringEvent + "\" value=\"" + metric + "\" />");
    }

    /**
     * Write a Timestamp metric value.
     * @param name metric name
     * @param date timestamp value
     */
    public void writeTimestamp(String name, Date date) {
        TimestampDataRecorder recorder;
        try {
            recorder = DataRecorderFactory.createTimestampDataRecorder(name);
            recorder.recordTimestamp(date.getTime());
        } catch (Exception e) {
            EpaUtils.getFeedback().error(e);
        }
    }

    /**
     * Write a LongAverage metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeLongAverage(String name, long metric) {
        LongAverageDataRecorder recorder;
        try {
            recorder = DataRecorderFactory.createLongAverageDataRecorder(name);
            recorder.recordDataPoint(metric);
        } catch (Exception e) {
            EpaUtils.getFeedback().error(e);
        }
    }

    /**
     * Write a LongCounter metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeLongCounter(String name, long metric) {
        LongCounterDataRecorder recorder;
        try {
            recorder = DataRecorderFactory.createLongCounterDataRecorder(name);
            recorder.add(metric);
        } catch (Exception e) {
            EpaUtils.getFeedback().error(e);
        }
    }

    /**
     * Write anIntCounter metric value and force it to exist.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntCounterForceExist(String name, int metric) {
        try {
            ILongIntervalCounterDataAccumulator counterDataAccumulator =
                    agent.IAgent_getDataAccumulatorFactory()
                    .safeGetLongIntervalCounterDataAccumulator(name);
            counterDataAccumulator.forceMetricToExist(null);
            counterDataAccumulator.ILongIntervalCounterDataAccumulator_addBatchIncidents(metric);
        } catch (Exception ex) {
            EpaUtils.getFeedback().error(ex);
        }
    }

    public void flushMetrics() {
        // do nothing
    }

    public void writeIntAverage(String name, int metric) {
        writeMetric(MetricWriter.kIntAverage, name, metric);
    }

    public void writeIntRate(String name, int metric) {
        writeMetric(MetricWriter.kIntRate, name, metric);
    }

    public void writePerIntervalCounter(String name, int metric) {
        writeMetric(MetricWriter.kPerIntervalCounter, name, metric);
    }


}
