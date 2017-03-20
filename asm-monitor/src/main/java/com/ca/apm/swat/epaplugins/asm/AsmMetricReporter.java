package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.agent.AgentNotAvailableException;
import com.wily.introscope.agent.AgentShim;
import com.wily.introscope.agent.recording.MetricRecordingAdministrator;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.metric.MetricTypes;
import com.wily.util.feedback.Module;

/**
 * Report metrics to APM via {@link MetricWriter}.
 *
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmMetricReporter implements AsmProperties, Runnable {

    private MetricWriter metricWriter;
    private HashMap<String, String> metricMap = null;
    private boolean turnOn = false;
    private Module module;

    protected static final String SEPARATOR = "\\.";

    /**
     * Report metrics to APM via metric writer.
     * @param metricWriter the metric writer
     * @param metricMap metrics to write
     * @param turnOn turn metrics on
     */
    public AsmMetricReporter(MetricWriter metricWriter,
                             HashMap<String, String> metricMap,
                             boolean turnOn) {
        this.metricWriter = metricWriter;
        this.metricMap = metricMap;
        this.turnOn = turnOn;
        this.module = new Module("Asm.AsmMetricReporter");
    }

    /**
     * Write the metrics to the {@link MetricWriter}.
     */
    public void run() {
        Iterator<Map.Entry<String, String>> metricIt = metricMap.entrySet().iterator();
        while (metricIt.hasNext()) {
            Map.Entry<String, String> metricPair = metricIt.next();

            if (((String) metricPair.getValue()).length() == 0) {
                continue;
            }

            String thisMetricType = returnMetricType(metricPair.getKey(), metricPair.getValue());

            String metricPath = metricPair.getKey();
            String metricValue = metricPair.getValue();

            if (MetricWriter.kFloat.equals(thisMetricType)) {
                 metricPair.setValue(((String) metricPair.getValue()).split(SEPARATOR)[0]);
                 thisMetricType = MetricWriter.kIntAverage;
            }
            
            if (EpaUtils.getBooleanProperty(PRINT_ASM_NODE, true)) {
                metricPath = METRIC_TREE + METRIC_PATH_SEPARATOR + metricPair.getKey();
            }

            metricPath = EpaUtils.fixMetricName(metricPath);
            if (MetricWriter.kStringEvent.equals(thisMetricType)) {
                metricValue = EpaUtils.fixMetricValue(metricValue);

//                Module module = new Module(Thread.currentThread().getName());
//                if (EpaUtils.getFeedback().isDebugEnabled(module)) {
//                    EpaUtils.getFeedback().debug(module, "writing metric of type "
//                            + thisMetricType + " '" + metricPath + "' = " + metricValue);
//                }
            }

            if (turnOn) {
                try {
                    MetricRecordingAdministrator admin =
                            AgentShim.getAgent().IAgent_getMetricRecordingAdministrator();
                    AgentMetric agentMetric =
                            AgentMetric.getAgentMetric(metricPath, mapMetricType(thisMetricType));
                    admin.turnMetricOn(agentMetric);
                } catch (AgentNotAvailableException e) {
                    EpaUtils.getFeedback().error(module, AsmMessages.getMessage(
                         AsmMessages.METRIC_TURN_ON_ERROR_923, metricPath), e);
                } catch (BadlyFormedNameException e) {
                    EpaUtils.getFeedback().error(module, AsmMessages.getMessage(
                        AsmMessages.METRIC_TURN_ON_ERROR_923, metricPath), e);
                }

            }

            metricWriter.writeMetric(thisMetricType, metricPath, metricValue);
        }
    }

    /**
     * Get metric data type. 
     * @param metricName input metric name
     * @param metricValue input metric value
     * @return metric type, one of {@link MetricWriter.kStringEvent},
     * {@link MetricWriter.kIntCounter}, {@link MetricWriter.kLongCounter} or
     * {@link MetricWriter.kFloat}
     */
    public static String returnMetricType(String metricName, String metricValue) {
        String metricType = MetricWriter.kStringEvent;

        // determine type from metric name
        if (metricName.endsWith(METRIC_NAME_PROBE_ERRORS)
                || metricName.endsWith(METRIC_NAME_PROBES)
                || metricName.endsWith(METRIC_NAME_CHECK_ERRORS)
                || metricName.endsWith(METRIC_NAME_CHECKS)
                || metricName.endsWith(METRIC_NAME_REPEAT)
                || metricName.endsWith(METRIC_NAME_CONSECUTIVE_ERRORS)
                || metricName.endsWith(METRIC_NAME_ALERTS_PER_INTERVAL)
                || metricName.endsWith(METRIC_NAME_ERRORS_PER_INTERVAL)) {

            metricType = MetricWriter.kPerIntervalCounter;

        } else if (metricName.endsWith("Time (ms)")
                || metricName.endsWith("Speed (kB/s)")
                || metricName.endsWith("Size (kB)")) {
        
            metricType = MetricWriter.kIntAverage;

        } else {
            // determine type from metric value

            if (metricValue.matches("^[+-]?[0-9]+$")) {
                try {
                    new Integer(metricValue);
                    metricType = MetricWriter.kIntCounter;
                } catch (NumberFormatException e) {
                    metricType = MetricWriter.kLongCounter;
                }
            } else if (metricValue.matches("^[+-]?[0-9]*\\.[0-9]+$")) {
                // float, cannot convert
                metricType = MetricWriter.kFloat;
            } else {
                metricType = MetricWriter.kStringEvent;
            }
        }

        return metricType;
    }

    /**
     * Map the metric type to the internal integer constant.
     * @param thisMetric metric type
     * @return corresponding integer constant
     */
    private int mapMetricType(String thisMetric) {
        if (thisMetric.equals(MetricWriter.kIntCounter)) {
            return MetricTypes.kIntegerFluctuatingCounter;
        } else if (thisMetric.equals(MetricWriter.kLongCounter)) {
            return MetricTypes.kLongFluctuatingCounter;
        } else if (thisMetric.equals(MetricWriter.kStringEvent)) {
            return MetricTypes.kStringIndividualEvents;
        } else if (thisMetric.equals(MetricWriter.kFloat)) {
            return MetricTypes.kFloatCounter;
        }
        return 0;
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
                Map.Entry<String, String> metricPair = (Map.Entry<String, String>) metricIt.next();

                if (!returnMetricType(metricPair.getKey(), metricPair.getValue())
                        .equals(MetricWriter.kStringEvent)) {
                    metricMap.put((String) metricPair.getKey(), ZERO);
                } else {
                    metricMap.put((String) metricPair.getKey(), EMPTY_STRING);
                }
            }
        }

        return metricMap;
    }
}
