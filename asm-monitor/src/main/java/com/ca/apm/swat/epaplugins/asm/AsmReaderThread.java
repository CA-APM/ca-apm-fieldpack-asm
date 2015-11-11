package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Worker threads for App Synthetic Monitor EPA plugin.
 * One thread per folder.
 * 
 */
public class AsmReaderThread extends Thread implements AsmProperties {
    private String folder;
    private HashMap<String, String> metricMap = new HashMap<String, String>();
    private volatile boolean keepRunning = true;
    private int numRetriesLeft;
    private AsmRequestHelper requestHelper;
    private HashMap<String, List<Monitor>> folderMap;
    private AsmMetricReporter metricReporter;
    private final int epaWaitTime;

    /**
     * Worker thread for App Synthetic Monitor EPA plugin.
     * @param folderName name of the folder to monitor
     * @param requestHelper the request helper
     * @param folderMap the folder map containing all folders and monitors
     * @param metricReporter the metric reporter
     */
    public AsmReaderThread(
        String folderName,
        AsmRequestHelper requestHelper,
        HashMap<String, List<Monitor>> folderMap,
        AsmMetricReporter metricReporter) {

        this.folder = folderName;
        this.requestHelper = requestHelper;
        this.folderMap = folderMap;
        this.metricReporter = metricReporter;
        this.numRetriesLeft = 10;
        this.epaWaitTime = Integer.parseInt(EpaUtils.getProperty(WAIT_TIME));
        this.setName(folderName);
    }


    /**
     * Run the main loop.
     */
    public void run() {

        EpaUtils.getFeedback().verbose(AsmMessages.getMessage(
            AsmMessages.THREAD_STARTED_312, this.folder));

        while (this.keepRunning) {
            try {
                final Date startTime = new Date();
                
                // get the metrics for this folder and all its monitors
                this.metricMap = getFolderMetrics();
                
                // send the metrics to Enterprise Manager
                metricReporter.printMetrics(this.metricMap);
               
                // TODO: is putAll redundant? why reset and keep all the old stuff?                
                //this.metricMap.putAll(metricReporter.resetMetrics(this.metricMap));
                
                final Date endTime = new Date();

                long timeElapsed = endTime.getTime() - startTime.getTime();
                long timeToSleep = epaWaitTime - timeElapsed;
                if (timeToSleep > 0L) {
                    Thread.sleep(timeToSleep);
                } else {
                    EpaUtils.getFeedback().error(AsmMessages.getMessage(
                        AsmMessages.FOLDER_THREAD_TIMEOUT_905,
                        this.folder, new Long(epaWaitTime)));
                    Thread.sleep(60000L);
                }
            } catch (InterruptedException e) {
                // We've been interrupted: exit run loop
                return;
            } catch (Exception e) {
                if (JAVA_NET_EXCEPTION_REGEX.matches(e.toString()) && (this.numRetriesLeft > 0)) {
                    this.numRetriesLeft = retryConnection(this.numRetriesLeft, this.folder);
                } else {
                    EpaUtils.getFeedback().error(AsmMessages.getMessage(
                        AsmMessages.FOLDER_THREAD_ERROR_906,
                        ASM_PRODUCT_NAME, this.folder,
                        e.getMessage() == null ? e.toString() : e.getMessage()));
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        PrintStream stream = new PrintStream(out);
                        e.printStackTrace(stream);
                        EpaUtils.getFeedback().error(this.folder + ": " + out.toString());
                    } catch (Exception ex) {
                        EpaUtils.getFeedback().error("error in " + this.folder + ":"
                                + ex.getMessage());
                        EpaUtils.getFeedback().error("error in " + this.folder + ": " + ex);
                    }
                    this.keepRunning = Boolean.valueOf(false);
                }
            }
        }
    }

    /**
     * Retry to connect.
     * @param numRetriesLeft retries left
     * @param apmcmInfo message to log
     * @return number of retries left
     */
    public int retryConnection(int numRetriesLeft, String apmcmInfo) {
        EpaUtils.getFeedback().error(AsmMessages.getMessage(AsmMessages.CONNECTION_ERROR_902,
            ASM_PRODUCT_NAME,apmcmInfo));

        if (numRetriesLeft > 0) {
            EpaUtils.getFeedback().debug(AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_501,
                numRetriesLeft));

            numRetriesLeft--;
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_ERROR_903));
        }
        return numRetriesLeft;
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

        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            EpaUtils.getFeedback().verbose(
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
        if (EpaUtils.getBooleanProperty(METRICS_STATS_FOLDER, false)) {
            if (EpaUtils.getFeedback().isVerboseEnabled()) {
                EpaUtils.getFeedback().verbose(
                    AsmMessages.getMessage(AsmMessages.GET_STATS_DATA_302,
                        folderMonitors.size(), folder));
            }
            
            // get aggregated folder stats
            resultMetricMap.putAll(requestHelper.getStats(folder, folderPrefix, true));
        } else {
            EpaUtils.getFeedback().verbose(
                AsmMessages.getMessage(AsmMessages.GET_NO_STATS_DATA_303, folder));
        }

        // don't get aggregate metrics for root folder
        if (!EMPTY_STRING.equals(folder)) {

            // get stats for all monitors of this folder
            if (EpaUtils.getBooleanProperty(METRICS_STATS_MONITOR, false)) {
                resultMetricMap.putAll(requestHelper.getStats(folder, folderPrefix, false));
            }
            
            // get logs for all monitors of this folder
            if (EpaUtils.getBooleanProperty(METRICS_LOGS, false)) {
                resultMetricMap.putAll(
                    requestHelper.getLogs(folder, folderMonitors.size(), folderPrefix));
            }
            // get public metrics for all monitors of this folder
            if (EpaUtils.getBooleanProperty(METRICS_PUBLIC, false)) {
                resultMetricMap.putAll(requestHelper.getPsp(folder, folderPrefix));
            }
        }
        
        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            EpaUtils.getFeedback().verbose(
                AsmMessages.getMessage(AsmMessages.GET_FOLDER_METRICS_304,
                    folder, resultMetricMap.size()));
        }
        return resultMetricMap;
    }
    
    public void stopThread() {
        this.keepRunning = false;
    }
}
