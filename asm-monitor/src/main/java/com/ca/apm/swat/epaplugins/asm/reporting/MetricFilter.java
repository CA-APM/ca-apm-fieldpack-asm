package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.Date;

/**
 * MetricFilters can be used to filter metrics, e.g. by metric name, resource, value, etc.
 *   For an example on hot to filter by metric name see {@link MetricNameFilter}.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface MetricFilter {

    /**
     * Tests a metric and returns true if the test passed.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    public abstract boolean testMetric(String metricName, String metricValue);

    /**
     * Tests a metric and returns true if the test passed.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    public abstract boolean testMetric(String metricName, int metricValue);

    /**
     * Tests a metric and returns true if the test passed.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    public abstract boolean testMetric(String metricName, long metricValue);

    /**
     * Tests a metric and returns true if the test passed.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    public abstract boolean testMetric(String metricName, float metricValue);

    /**
     * Tests a metric and returns true if the test passed.
     * @param metricName metric name to test
     * @param metricValue metric value to test
     * @return true if the metric can be printed
     */
    public abstract boolean testMetric(String metricName, Date metricValue);

}