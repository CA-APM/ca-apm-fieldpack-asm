package com.ca.apm.swat.epaplugins.asm;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.monitor.MonitorFactory;
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
    private boolean keepRunning = true;
    private int numRetriesLeft;
    private AsmRequestHelper requestHelper;
    private HashMap<String, List<Monitor>> folderMap;
    private Properties properties;
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
        Properties properties,
        AsmMetricReporter metricReporter) {

        this.folder = folderName;
        this.requestHelper = requestHelper;
        this.folderMap = folderMap;
        this.properties = properties;
        this.metricReporter = metricReporter;
        this.numRetriesLeft = 10;
        this.epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));
    }


    /**
     * Run the main loop.
     */
    public void run() {

        EpaUtils.getFeedback().verbose(AsmMessages.getMessage(
            AsmMessages.THREAD_STARTED, this.folder));

        while (this.keepRunning) {
            try {
                final Date startTime = new Date();
                
                // get the metrics for this folder and all its monitors
                this.metricMap.putAll(getFolderMetrics());
                
                // send the metrics to Enterprise Manager
                metricReporter.printMetrics(this.metricMap);
               
                // TODO: is putAll redundant? why reset and keep all the old stuff?                
                this.metricMap.putAll(metricReporter.resetMetrics(this.metricMap));
                final Date endTime = new Date();

                long timeElapsed = endTime.getTime() - startTime.getTime();
                long timeToSleep = epaWaitTime - timeElapsed;
                if (timeToSleep > 0L) {
                    Thread.sleep(timeToSleep);
                } else {
                    EpaUtils.getFeedback().error(AsmMessages.getMessage(
                        AsmMessages.FOLDER_THREAD_TIMEOUT,
                        this.folder, new Long(epaWaitTime)));
                    Thread.sleep(60000L);
                }
            } catch (Exception e) {
                if (JAVA_NET_EXCEPTION_REGEX.matches(e.toString()) && (this.numRetriesLeft > 0)) {
                    this.numRetriesLeft = retryConnection(this.numRetriesLeft, this.folder);
                } else {
                    EpaUtils.getFeedback().error(AsmMessages.getMessage(
                        AsmMessages.FOLDER_THREAD_ERROR,
                        ASM_PRODUCT_NAME, this.folder, e.getMessage()));
                    //TODO: remove
                    //e.printStackTrace();
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
        EpaUtils.getFeedback().error(AsmMessages.getMessage(AsmMessages.CONNECTION_ERROR,
            ASM_PRODUCT_NAME,apmcmInfo));

        if (numRetriesLeft > 0) {
            EpaUtils.getFeedback().debug(AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY,
                numRetriesLeft));

            numRetriesLeft--;
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_ERROR));
        }
        return numRetriesLeft;
    }

    /**
     * Get the current metrics for the folder.
     * @return map of metrics
     * @throws Exception errors
     */
    public HashMap<String, String> getFolderMetrics() throws Exception {
        HashMap<String, String> resultMetricMap = new HashMap<String, String>();

        List<Monitor> folderMonitors = this.folderMap.get(folder);
        final Monitor allMonitorsMonitor = MonitorFactory.getAllMonitorsMonitor();
        Monitor monitor = null;
        
        EpaUtils.getFeedback().verbose(
            AsmMessages.getMessage(AsmMessages.GET_FOLDER_DATA, folderMonitors.size(), folder));

        if ((null == folderMonitors) || (0 == folderMonitors.size())) {
            return resultMetricMap;
        }

        // prefix for metric name
        String folderPrefix = MONITOR_METRIC_PREFIX + folder;

        if (ROOT_FOLDER.equals(folder)) {
            folder = EMPTY_STRING;
            // remove trailing '|'
            folderPrefix = MONITOR_METRIC_PREFIX.substring(0, MONITOR_METRIC_PREFIX.length() - 1);
        }
        
        // get stats for folder
        if (TRUE.equals(properties.getProperty(METRICS_STATS_FOLDER, FALSE))) {
            EpaUtils.getFeedback().verbose(
                AsmMessages.getMessage(AsmMessages.GET_STATS_DATA, folderMonitors.size(), folder));

            resultMetricMap.putAll(requestHelper.getStats(folder, null, folderPrefix));
        } else {
            EpaUtils.getFeedback().verbose(
                AsmMessages.getMessage(AsmMessages.GET_NO_STATS_DATA, folder));
        }

        // get stats for all monitors
        if ((folderMonitors.get(0).equals(allMonitorsMonitor))
                && (!folder.equals(EMPTY_STRING))) {
            
            if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                resultMetricMap.putAll(requestHelper.getPsp(folder, null, folderPrefix));
            }
            
            if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                resultMetricMap.putAll(
                    requestHelper.getLogs(folder, null, folderMonitors.size() - 1, folderPrefix));
            }

            if (properties.getProperty(METRICS_STATS_MONITOR, FALSE).equals(TRUE)) {
                for (Iterator<Monitor> it = folderMonitors.iterator(); it.hasNext(); ) {
                    monitor = it.next();
                    if (monitor.equals(allMonitorsMonitor)) {
                        continue;
                    }
//                    EpaUtils.getFeedback().verbose(
//                        AsmMessages.getMessage(AsmMessages.GET_STATS_DATA, 1,
//                            folderPrefix + METRIC_PATH_SEPARATOR + monitor.getName()));

                    resultMetricMap.putAll(requestHelper.getStats(folder, monitor, folderPrefix));
                }
            }
        } else {
            for (Iterator<Monitor> it = folderMonitors.iterator(); it.hasNext(); ) {
                monitor = it.next();
                if (monitor.equals(ALL_MONITORS)) {
                    continue;
                }
                if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                    resultMetricMap.putAll(requestHelper.getPsp(folder, monitor, folderPrefix));
                }
                if (properties.getProperty(METRICS_STATS_MONITOR, FALSE).equals(TRUE)) {
//                    EpaUtils.getFeedback().verbose(
//                        AsmMessages.getMessage(AsmMessages.GET_STATS_DATA, -1,
//                            folderPrefix + METRIC_PATH_SEPARATOR + monitor.getName()));

                    resultMetricMap.putAll(requestHelper.getStats(folder, monitor, folderPrefix));
                }
                if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                    resultMetricMap.putAll(requestHelper.getLogs(folder, monitor, 1, folderPrefix));
                }
            }
        }

        EpaUtils.getFeedback().verbose("getFolderMetrics finished for folder " + folder
            + ", returning " + resultMetricMap.size() + " metrics");

        return resultMetricMap;
    }
}
