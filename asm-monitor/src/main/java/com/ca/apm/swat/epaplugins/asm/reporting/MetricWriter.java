package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.IOException;
import java.util.Date;


/**
 * Wrapper for writing the Introscope data to the EPA framework.
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface MetricWriter {

    // metric data type constants
    public static final String kPerIntervalCounter = "PerIntervalCounter";
    public static final String kIntCounter = "IntCounter";
    public static final String kIntAverage = "IntAverage";
    public static final String kIntRate = "IntRate";
    public static final String kLongCounter = "LongCounter";
    public static final String kLongAverage = "LongAverage";
    public static final String kStringEvent = "StringEvent";
    public static final String kTimestamp = "Timestamp";
    public static final String kFloat = "Float";

    /**
     * Write an integer metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, int metric);

    /**
     * Write a long metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, long metric);

    /**
     * Write a float metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, float metric);

    /**
     * Write a String metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, String metric);

    /**
     * Write an error message.
     * @param message error message
     */
    // TODO: really write generic error message?
    public void writeErrorMessage(String message);

    /**
     * Write an error detector entry (error snapshot).
     * @param text error text
     * @param resource resource
     */
    public void writeErrorDetectorEntry(String text, String resource);

    /**
     * Write a String metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeStringMetric(String name, String metric);

    /**
     * Write an IntCounter metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntCounter(String name, int metric);

    /**
     * Write an IntAverage metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntAverage(String name, int metric);

    /**
     * Write an IntRate metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntRate(String name, int metric);

    /**
     * Write a PerIntervalCounter metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writePerIntervalCounter(String name, int metric);

    /**
     * Write a Timestamp metric value.
     * @param name metric name
     * @param date timestamp
     */
    public void writeTimestamp(String name, Date date);

    /**
     * Write an LongAverage metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeLongAverage(String name, long metric);

    /**
     * Write an LongCounter metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeLongCounter(String name, long metric);

    /**
     * Write an IntCounter metric value and force it to exist.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntCounterForceExist(String name, int metric);

    /**
     * If metrics are buffered flush them (e.g. send to agent).
     */
    public void flushMetrics() throws IOException;
}
