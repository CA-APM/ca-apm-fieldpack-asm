package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.IOException;
import java.util.Date;

/**
 * BaseMetricFilter combines the {@link MetricFilter} and {@link MetricWriter} interfaces.
 *   For every metric that is written using the <code>MetricWriter</code> interface
 *   the corresponding <code>testMetric</code> function is called in the
 *   <code>MetricFilter</code> and the metric is only written if the test succeeds.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public abstract class MetricFilterBase implements MetricWriter, MetricFilter {

    protected MetricWriter writer = null;

    /**
     * Create a new MetricFilter.
     * @param writer MetricWriter to print to
     */
    public MetricFilterBase(MetricWriter writer) {
        this.writer = writer;
    }

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
