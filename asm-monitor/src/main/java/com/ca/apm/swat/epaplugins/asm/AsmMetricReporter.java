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
