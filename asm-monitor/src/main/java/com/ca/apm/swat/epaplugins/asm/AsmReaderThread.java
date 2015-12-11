package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.ErrorUtils;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;

/**
 * Worker threads for App Synthetic Monitor EPA plugin.
 * One thread per folder.
 * 
 */
public class AsmReaderThread implements AsmProperties, Runnable {
    private String folder;
    private AsmRequestHelper requestHelper;
    private HashMap<String, List<Monitor>> folderMap;
    private MetricWriter metricWriter;
    private ExecutorService reporterService;
    private Module module;

    /**
     * Worker thread for App Synthetic Monitor EPA plugin.
     * @param folderName name of the folder to monitor
     * @param requestHelper the request helper
     * @param folderMap the folder map containing all folders and monitors
     * @param metricWriter the metric writer
     * @param reporterService the metric reporter service
     */
    public AsmReaderThread(String folderName,
                           AsmRequestHelper requestHelper,
                           HashMap<String, List<Monitor>> folderMap,
                           MetricWriter metricWriter,
                           ExecutorService reporterService) {

        this.folder = folderName;
        this.requestHelper = requestHelper;
        this.folderMap = folderMap;
        this.metricWriter = metricWriter;
        this.reporterService = reporterService;
        this.module = new Module("Asm.Folder." + folderName);
    }


    /**
     * Run the main loop.
     */
    public void run() {
        Thread.currentThread().setName(module.getName());

        EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
            AsmMessages.THREAD_STARTED_312, this.folder));

        try {
            // get the metrics for this folder and all its monitors
            HashMap<String, String> metricMap = getFolderMetrics();

            // send the metrics to Enterprise Manager
            reporterService.execute(new AsmMetricReporter(metricWriter, metricMap));

        } catch (InterruptedException e) {
            // We've been interrupted: exit
            return;
        } catch (Exception e) {
            EpaUtils.getFeedback().error(module, AsmMessages.getMessage(
                AsmMessages.FOLDER_THREAD_ERROR_906,
                ASM_PRODUCT_NAME, this.folder,
                e.getMessage() == null ? e.toString() : e.getMessage()));
            EpaUtils.getFeedback().error(module, this.folder + ": " + ErrorUtils.getStackTrace(e));
        }
    }

    /**
     * Get the current metrics for the folder.
     * @return map of metrics
     * @throws Exception errors
     */
    public HashMap<String, String> getFolderMetrics() throws Exception {
        MetricMap resultMetricMap = new MetricMap();

        List<Monitor> folderMonitors = this.folderMap.get(folder);

        if ((null == folderMonitors) || (0 == folderMonitors.size())) {
            return resultMetricMap;
        }

        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module, 
                AsmMessages.getMessage(AsmMessages.GET_FOLDER_DATA_301,
                    folderMonitors.size(), folder));
        }

        // prefix for metric name
        String folderPrefix = MONITOR_METRIC_PREFIX + folder;

        if (ROOT_FOLDER.equals(folder)) {
            folder = EMPTY_STRING;
            // remove trailing '|'
            folderPrefix = MONITOR_METRIC_PREFIX.substring(0, MONITOR_METRIC_PREFIX.length() - 1);
        }

        // get stats for folder
        try {
            if (EpaUtils.getBooleanProperty(METRICS_STATS_FOLDER, false)) {
                if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                    EpaUtils.getFeedback().verbose(module, 
                        AsmMessages.getMessage(AsmMessages.GET_STATS_DATA_302,
                            folderMonitors.size(), folder));
                }

                // get aggregated folder stats
                resultMetricMap.putAll(requestHelper.getStats(folder, folderPrefix, true));
            } else {
                EpaUtils.getFeedback().verbose(module, 
                    AsmMessages.getMessage(AsmMessages.GET_NO_STATS_DATA_303, folder));
            }
        } catch (Exception e) {
            EpaUtils.getFeedback().warn(module, e.getMessage());
        }

        // don't get aggregate metrics for root folder
        if (!EMPTY_STRING.equals(folder)) {
            // get stats for all monitors of this folder
            try {
                if (EpaUtils.getBooleanProperty(METRICS_STATS_MONITOR, false)) {
                    resultMetricMap.putAll(requestHelper.getStats(folder, folderPrefix, false));
                }
            } catch (Exception e) {
                EpaUtils.getFeedback().warn(module, e.getMessage());
            }

            // get logs for all monitors of this folder
            try {
                if (EpaUtils.getBooleanProperty(METRICS_LOGS, false)) {
                    resultMetricMap.putAll(
                        requestHelper.getLogs(folder, folderMonitors.size(), folderPrefix));
                }
            } catch (Exception e) {
                EpaUtils.getFeedback().warn(module, e.getMessage());
            }

            // get public metrics for all monitors of this folder
            try {
                if (EpaUtils.getBooleanProperty(METRICS_PUBLIC, false)) {
                    resultMetricMap.putAll(requestHelper.getPsp(folder, folderPrefix));
                }
            } catch (Exception e) {
                EpaUtils.getFeedback().warn(module, e.getMessage());
            }
        }

        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module, 
                AsmMessages.getMessage(AsmMessages.GET_FOLDER_METRICS_304,
                    folder, resultMetricMap.size()));
        }
        return resultMetricMap;
    }
}
