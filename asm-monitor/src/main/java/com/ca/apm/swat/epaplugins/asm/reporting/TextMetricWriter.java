package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.PrintStream;
import java.util.Date;

/**
 * Dummy and Debug implementation for a defect writer.
 * Simply outputs the content to standard out.
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
public class TextMetricWriter implements MetricWriter {

    private PrintStream ps = null;
    
    /**
     * Create a TextMetricWriter that writes to {@link System#out}.
     */
    public TextMetricWriter() {
        this.ps = System.out;
    }

    /**
     * Create a TextMetricWriter that writes to {@link PrintStream}.
     * @param ps stream to write to
     */
    public TextMetricWriter(PrintStream ps) {
        this.ps = ps;
    }
    
    
    private void fillTestValues() {
        //do nothing

    }

    public void writeMetric(String type, String name, String metric) {
        fillTestValues();
        ps.println("Type:" + type + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeMetric(String type, String name, int metric) {
        fillTestValues();
        ps.println("Type:" + type + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeMetric(String type, String name, long metric) {
        fillTestValues();
        ps.println("Type:" + type + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeMetric(String type, String name, float metric) {
        fillTestValues();
        ps.println("Type:" + type + ", Name:" + name + ", MetricValue:" + metric);
    }

    /**
     * Write an error detector entry (error snapshot).
     * @param text error text
     * @param resource resource
     */
    public void writeErrorDetectorEntry(String text, String resource) {
        fillTestValues();
        ps.println("Type: ErrorDetector" + ", Name:" + resource + " Error"
                + ", MetricValue:" + text);
    }

    public void writeErrorMessage(String message) {
        ps.println(message);
    }

    public void writeLongSteadyAverageMetric(String name, long metric) {
        fillTestValues();
        ps.println("Type:" + "LongAverage" + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeStringMetric(String string, String string2) {
        ps.println("Type:" + "String" + ", Name:" + string + ", MetricValue:" + string2);
    }

    public void writeIntCounter(String name, int metric) {
        fillTestValues();
        ps.println("Type:" + "IntCounter" + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeTimestamp(String string, Date date) {
        ps.println("Type:" + "Timestamp" + ", Name:" + string + ", MetricValue:" + date);
    }

    public void writeLongAverage(String name, long metric) {
        ps.println("Type:" + "LongAverage" + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeIntCounterForceExist(String name, int metric) {
        fillTestValues();
        ps.println("Type:" + "IntCounter" + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void flushMetrics() {
        ps.flush();
    }

    public void writeIntAverage(String name, int metric) {
        ps.println("Type:" + "IntAverage" + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writeIntRate(String name, int metric) {
        ps.println("Type:" + "IntRate" + ", Name:" + name + ", MetricValue:" + metric);
    }

    public void writePerIntervalCounter(String name, int metric) {
        ps.println("Type:" + "PerIntervalCounter" + ", Name:" + name
            + ", MetricValue:" + metric);
    }

    public void writeLongCounter(String name, long metric) {
        ps.println("Type:" + "LongCounter" + ", Name:" + name + ", MetricValue:" + metric);
    }

}
