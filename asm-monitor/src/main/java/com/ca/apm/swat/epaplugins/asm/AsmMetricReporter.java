package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Report metrics to APM via {@link MetricWriter}.
 *
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmMetricReporter implements AsmProperties {

    private MetricWriter metricWriter;
    protected static final String SEPARATOR = "\\.";

    /**
     * Report metrics to APM via metric writer.
     * @param metricWriter the metric writer
     */
    public AsmMetricReporter(MetricWriter metricWriter) {
        this.metricWriter = metricWriter;
    }

    /**
     * Write the metrics to the {@link MetricWriter}.
     * @param metricMap map containing the metrics
     * @throws Exception errors
     */
    public void printMetrics(HashMap<String, String> metricMap) throws Exception {
        Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
        while (metricIt.hasNext()) {
            Map.Entry<String, String> metricPair = (Map.Entry<String, String>) metricIt.next();

            if (((String) metricPair.getValue()).length() == 0) {
                continue;
            }

            String thisMetricType = returnMetricType((String) metricPair.getValue());

            if (thisMetricType.equals(MetricWriter.kFloat)) {
                metricPair.setValue(((String) metricPair.getValue()).split(SEPARATOR)[0]);
                thisMetricType = MetricWriter.kIntCounter;
            }

            String metricPath = metricPair.getKey();
            String metricValue = metricPair.getValue();
            
            if (EpaUtils.getBooleanProperty(PRINT_ASM_NODE, true)) {
                metricPath = METRIC_TREE + METRIC_PATH_SEPARATOR + metricPair.getKey();
            }

            metricPath = EpaUtils.fixMetricName(metricPath);
            if (MetricWriter.kStringEvent.equals(thisMetricType)) {
                metricValue = EpaUtils.fixMetricValue(metricValue);
            
//                if (EpaUtils.getFeedback().isDebugEnabled()) {
//                    EpaUtils.getFeedback().debug("writing metric of type " + thisMetricType + " '"
//                            + metricPath + "' = " + metricValue);
//                }
            }
            
            metricWriter.writeMetric(thisMetricType, metricPath, metricValue);
        }
    }

    /**
     * Get metric data type. 
     * @param thisMetric input metric data
     * @return metric type, one of {@link MetricWriter.kStringEvent},
     * {@link MetricWriter.kIntCounter}, {@link MetricWriter.kLongCounter} or
     * {@link MetricWriter.kFloat}
     */
    private String returnMetricType(String thisMetric) {
        String metricType = MetricWriter.kStringEvent;

        if (thisMetric.matches("^[+-]?[0-9]+$")) {
            try {
                new Integer(thisMetric);
                metricType = MetricWriter.kIntCounter;
            } catch (NumberFormatException e) {
                metricType = MetricWriter.kLongCounter;
            }
        } else if (thisMetric.matches("^[+-]?[0-9]*\\.[0-9]+$")) {
            metricType = MetricWriter.kFloat;
        } else {
            metricType = MetricWriter.kStringEvent;
        }
        /*
        try {
            new Integer(thisMetric);
            metricType = MetricWriter.kIntCounter;
        } catch (NumberFormatException e) {
            try {
                new Long(thisMetric);
                metricType = MetricWriter.kLongCounter;
            } catch (NumberFormatException ee) {
                try {
                    new Float(thisMetric);
                    metricType = MetricWriter.kFloat;
                } catch (NumberFormatException eee) {
                    metricType = MetricWriter.kStringEvent;
                }
            }
        }
         */
        return metricType;
    }

    /**
     * Reset all metrics in <code>metricMap</code> to 0 or "".
     * @param metricMap map containing the metrics
     * @return the reset map
     * @throws Exception errors
     */
    public HashMap<String, String> resetMetrics(HashMap<String, String> metricMap)
            throws Exception {
        if (metricMap.size() != 0) {
            Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
            while (metricIt.hasNext()) {
                Map.Entry<String, String> metricPairs = (Map.Entry<String, String>) metricIt.next();

                if (!returnMetricType((String) metricPairs.getValue()).equals(
                    MetricWriter.kStringEvent)) {
                    metricMap.put((String) metricPairs.getKey(), ZERO);
                } else {
                    metricMap.put((String) metricPairs.getKey(), EMPTY_STRING);
                }
            }
        }

        return metricMap;
    }
}
