package com.ca.apm.swat.epaplugins.asm;

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
import com.wily.util.feedback.IModuleFeedbackChannel;
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
    private boolean firstRun;
    private String lastId;


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
        this.firstRun = true;
        this.lastId = null;
    }


    /**
     * Run the main loop.
     */
    public void run() {
        try {
            Thread.currentThread().setName(module.getName());

            EpaUtils.getFeedback()
                .verbose(module,
                         AsmMessages.getMessage(AsmMessages.THREAD_STARTED_312,
                                                Thread.currentThread().getId(),
                                                this.folder));

            // get the metrics for this folder and all its monitors
            HashMap<String, String> metricMap = getFolderMetrics();

            // send the metrics to Enterprise Manager
            reporterService.execute(new AsmMetricReporter(metricWriter, metricMap, firstRun));

            firstRun = false;
        } catch (InterruptedException e) {
            // We've been interrupted: exit
            return;
        } catch (Exception e) {
            EpaUtils.getFeedback()
                .error(module, AsmMessages.getMessage(
                                                      AsmMessages.FOLDER_THREAD_ERROR_906,
                                                      ASM_PRODUCT_NAME, this.folder,
                                                      e.getMessage() == null
                                                      ? e.toString()
                                                      : e.getMessage()));
            EpaUtils.getFeedback().error(module, this.folder + ": " + ErrorUtils.getStackTrace(e));
        }
    }

    /**
     * Get the current metrics for the folder.
     * @return map of metrics
     * @throws Exception errors
     */
    public HashMap<String, String> getFolderMetrics() throws Exception {
        IModuleFeedbackChannel log = EpaUtils.getFeedback();
        MetricMap resultMetricMap = new MetricMap();

        String folderPrefix = null;
        int monitorCount = requestHelper.getActiveMonitorCount();

        if (!folder.equals(ALL_FOLDERS)) {
            List<Monitor> folderMonitors = this.folderMap.get(folder);

            if ((null == folderMonitors) || (0 == folderMonitors.size())) {
                return resultMetricMap;
            }

            if (log.isVerboseEnabled(module)) {
                log.verbose(module,
                        AsmMessages.getMessage(AsmMessages.GET_FOLDER_DATA_301,
                                folderMonitors.size(), folder));
            }

            // prefix for metric name
            folderPrefix = MONITOR_METRIC_PREFIX + folder;
            monitorCount = folderMonitors.size();

            if (ROOT_FOLDER.equals(folder)) {
                folder = EMPTY_STRING;
                // remove trailing '|'
                folderPrefix = MONITOR_METRIC_PREFIX.substring(0,
                        MONITOR_METRIC_PREFIX.length() - 1);
            }
        }

        // get stats for folder
        try {
            if (EpaUtils.getBooleanProperty(METRICS_STATS_FOLDER, false)) {
                if (log.isVerboseEnabled(module)) {
                    log
                        .verbose(module, 
                                 AsmMessages.getMessage(AsmMessages.GET_STATS_DATA_302,
                                                        monitorCount, folder));
                }

                // get aggregated folder stats
                resultMetricMap.putAll(requestHelper.getStats(folder, folderPrefix, true));
            } else {
                log.verbose(module,
                        AsmMessages.getMessage(AsmMessages.GET_NO_STATS_DATA_303, folder));
            }
        } catch (Exception e) {
            log.warn(module, AsmMessages
                                        .getMessage(AsmMessages.METRIC_READ_WARN_704,
                                                    folder, STATS_CMD, e.getMessage()));
            if (log.isDebugEnabled(module)) {
                log.debug(module, ErrorUtils.getStackTrace(e));
            }
        }

        if (Thread.currentThread().isInterrupted()) {
            log.verbose(module,
                         "thread interrupted - "
                             + AsmMessages.getMessage(AsmMessages.GET_FOLDER_METRICS_304,
                                                      folder, resultMetricMap.size()));
            return resultMetricMap;
        }

        // don't get aggregate metrics for root folder
        if (!EMPTY_STRING.equals(folder)) {
            // get stats for all monitors of this folder
            // Disabling this for the moment since these statistics are not very useful
            // and they somehow overwrite the values coming from rule_log
            /*try {
                if (EpaUtils.getBooleanProperty(METRICS_STATS_MONITOR, false)) {
                    resultMetricMap.putAll(requestHelper.getStats(folder, folderPrefix, false));
                }
            } catch (Exception e) {
                log.warn(module, AsmMessages.getMessage(AsmMessages.METRIC_READ_WARN_704,
                                                        folder, STATS_CMD, e.getMessage()));
                if (log.isDebugEnabled(module)) {
                    log.debug(module, ErrorUtils.getStackTrace(e));
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                log.verbose(module,
                             "thread interrupted - "
                                 + AsmMessages.getMessage(AsmMessages.GET_FOLDER_METRICS_304,
                                                          folder, resultMetricMap.size()));
                return resultMetricMap;
            }*/

            // get logs for all monitors of this folder
            try {
                if (EpaUtils.getBooleanProperty(METRICS_LOGS, false)) {
                    LogResult result = requestHelper.getLogs(folder,
                                                                 monitorCount,
                                                                 folderPrefix,
                                                                 lastId);
                    if (result.getLastId() != null) {
                        lastId = result.getLastId();
                    }
                    resultMetricMap.putAll(result.getMap());
                }
            } catch (Exception e) {
                log.warn(module, AsmMessages.getMessage(AsmMessages.METRIC_READ_WARN_704,
                                                        folder, LOGS_CMD, e.getMessage()));
                log.debug(module, ErrorUtils.getStackTrace(e));
            }

            if (Thread.currentThread().isInterrupted()) {
                log
                    .verbose(module, 
                             "thread interrupted - "
                                 + AsmMessages.getMessage(AsmMessages.GET_FOLDER_METRICS_304,
                                                          folder, resultMetricMap.size()));
                return resultMetricMap;
            }

            // get public metrics for all monitors of this folder
            try {
                if (EpaUtils.getBooleanProperty(METRICS_PUBLIC, false)) {
                    resultMetricMap.putAll(requestHelper.getPsp(folder, folderPrefix));
                }
            } catch (Exception e) {
                log.warn(module, AsmMessages.getMessage(AsmMessages.METRIC_READ_WARN_704,
                        folder, PSP_CMD, e.getMessage()));
                if (log.isDebugEnabled(module)) {
                    log.debug(module, ErrorUtils.getStackTrace(e));
                }
            }
        }

        if (log.isVerboseEnabled(module)) {
            log.verbose(module,
                    AsmMessages.getMessage(AsmMessages.GET_FOLDER_METRICS_304,
                            folder,
                            resultMetricMap.size()));
        }
        return resultMetricMap;
    }
}
