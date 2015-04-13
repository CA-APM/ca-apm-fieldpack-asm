package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.Date;


/**
 * MetricNameFilter prints only metrics with names that don't match a predefined list.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class MetricNameFilter extends MetricFilterBase {

    private String[] names = null;

    /**
     * Create a new MetricNameFilter.
     * @param writer MetricWriter to print to
     * @param disallowedMetricNames list of metric names that are filtered out.
     *     "metric name" is the part of the metrics path after ':';
     *     e.g. "Average Response Time (ms)", "Stall Count". Brackets need not be escaped.
     */
    public MetricNameFilter(MetricWriter writer, String[] disallowedMetricNames) {
        super(writer);
        
        // store disallowedMetricNames but prepend ':'
        names = new String[disallowedMetricNames.length];        
        for (int i = 0; i < disallowedMetricNames.length; ++i) {
            names[i] = new String(":" + disallowedMetricNames[i]);
        }
    }

    /**
     * Tests a metric name against the supplied list of metric names.
     * @param metricName metric name to test
     * @return true if the metric can be printed
     */
    private boolean allowMetric(String metricName) {
        for (int i = 0; i < names.length; ++i) {
            if (metricName.endsWith(names[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean testMetric(String metricName, String metricValue) {
        return allowMetric(metricName);
    }

    public boolean testMetric(String metricName, int metricValue) {
        return allowMetric(metricName);
    }

    public boolean testMetric(String metricName, long metricValue) {
        return allowMetric(metricName);
    }

    public boolean testMetric(String metricName, float metricValue) {
        return allowMetric(metricName);
    }

    public boolean testMetric(String metricName, Date metricValue) {
        return allowMetric(metricName);
    }
}
