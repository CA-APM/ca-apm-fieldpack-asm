package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.IOException;
import java.util.Date;

/**
 * MetricFilter prints only metrics that pass a test.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public abstract class MetricFilter implements MetricWriter {

    protected MetricWriter writer = null;

    /**
     * Create a new MetricFilter.
     * @param writer MetricWriter to print to
     */
    public MetricFilter(MetricWriter writer) {
        this.writer = writer;
    }

    /**
     * Tests a metric name against the supplied list of metric names.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    protected abstract boolean testMetric(String metricName, String metricValue);

    /**
     * Tests a metric name against the supplied list of metric names.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    protected abstract boolean testMetric(String metricName, int metricValue);

    /**
     * Tests a metric name against the supplied list of metric names.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    protected abstract boolean testMetric(String metricName, long metricValue);

    /**
     * Tests a metric name against the supplied list of metric names.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    protected abstract boolean testMetric(String metricName, float metricValue);

    /**
     * Tests a metric name against the supplied list of metric names.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    protected abstract boolean testMetric(String metricName, Date metricValue);

    /**
     * Write a metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, int metric) {
        if (testMetric(name, metric)) {
            writer.writeMetric(type, name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, long metric) {
        if (testMetric(name, metric)) {
            writer.writeMetric(type, name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, float metric) {
        if (testMetric(name, metric)) {
            writer.writeMetric(type, name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param type metric data type
     * @param name metric name
     * @param metric metric value
     */
    public void writeMetric(String type, String name, String metric) {
        if (testMetric(name, metric)) {
            writer.writeMetric(type, name, metric);
        }
    }

    public void writeErrorMessage(String message) {
        writer.writeErrorMessage(message);
    }

    public void writeErrorDetectorEntry(String text, String resource) {
        writer.writeErrorDetectorEntry(text, resource);
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeStringMetric(String name, String metric) {
        if (testMetric(name, metric)) {
            writer.writeStringMetric(name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntCounter(String name, int metric) {
        if (testMetric(name, metric)) {
            writer.writeIntCounter(name, metric);
        }
        
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntAverage(String name, int metric) {
        if (testMetric(name, metric)) {
            writer.writeIntAverage(name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntRate(String name, int metric) {
        if (testMetric(name, metric)) {
            writer.writeIntRate(name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writePerIntervalCounter(String name, int metric) {
        if (testMetric(name, metric)) {
            writer.writePerIntervalCounter(name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param date metric value
     */
    public void writeTimestamp(String name, Date date) {
        if (testMetric(name, date)) {
            writer.writeTimestamp(name, date);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeLongAverage(String name, long metric) {
        if (testMetric(name, metric)) {
            writer.writeLongAverage(name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeLongCounter(String name, long metric) {
        if (testMetric(name, metric)) {
            writer.writeLongCounter(name, metric);
        }
    }

    /**
     * Write a metric value.
     * @param name metric name
     * @param metric metric value
     */
    public void writeIntCounterForceExist(String name, int metric) {
        if (testMetric(name, metric)) {
            writer.writeIntCounterForceExist(name, metric);
        }
    }

    public void flushMetrics() throws IOException {
        writer.flushMetrics();   
    }
}
