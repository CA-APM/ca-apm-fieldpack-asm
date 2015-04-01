package com.ca.apm.swat.epaplugins.asm;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.JsonHelper;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Main thread for App Synthetic Monitor EPA plugin.
 * One thread per folder.
 * 
 */
public class AsmReaderThread extends Thread implements AsmProperties {
    private String folder;
    private HashMap<String, String> metricMap = new HashMap<String, String>();
    private boolean keepRunning = true;
    private int numRetriesLeft;
    private CloudMonitorRequestHelper requestHelper;
    private HashMap<String, String[]> folderMap;
    private Properties properties;
    private CloudMonitorMetricReporter metricReporter;
    private final int epaWaitTime;

    /**
     * Main thread for App Synthetic Monitor EPA plugin.
     * @param folderName name of the folder to monitor
     * @param requestHelper the request helper
     * @param folderMap the folder map containing all folders and monitors (rules)
     * @param properties the properties
     * @param metricReporter the metric reporter
     */
    AsmReaderThread(
        String folderName,
        CloudMonitorRequestHelper requestHelper,
        HashMap<String, String[]> folderMap,
        Properties properties,
        CloudMonitorMetricReporter metricReporter) {
        
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
                this.metricMap.putAll(getFolderMetrics());
                metricReporter.printMetrics(this.metricMap);
                // TODO: is putAll redundant?                
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
                if (kJavaNetExceptionRegex.matches(e.toString()) && (this.numRetriesLeft > 0)) {
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
            EpaUtils.getFeedback().error(AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_ERROR));
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

        String[] folderRules = (String[]) this.folderMap.get(folder);

        // TODO: remove or convert to message
        EpaUtils.getFeedback().verbose("getting data for " + folderRules.length + " rules of folder " + folder);
 
        if ((null == folderRules) || (0 == folderRules.length)) {
            return resultMetricMap;
        }

        // prefix for metric name
        String folderPrefix = MONITOR_METRIC_PREFIX + folder;

        if (ROOT_FOLDER.equals(folder)) {
            folder = EMPTY_STRING;
            // remove trailing '|'
            folderPrefix = MONITOR_METRIC_PREFIX.substring(0,
                MONITOR_METRIC_PREFIX.length() - 1);
        }

        if (TRUE.equals(properties.getProperty(METRICS_STATS_FOLDER, FALSE))) {
            // TODO: remove or convert to message
            EpaUtils.getFeedback().verbose("getting stats for all " + folderRules.length + " rules of folder " + folder);
            String statsRequest = requestHelper.getStats(folder, EMPTY_STRING);
            resultMetricMap.putAll(metricReporter.generateMetrics(JsonHelper.unpadJson(statsRequest),
                folderPrefix));
        } else {
            // TODO: remove or convert to message
            EpaUtils.getFeedback().verbose("not getting any stats for folder " + folder);
        }

        if ((folderRules[0].equals(ALL_RULES)) && (!folder.equals(EMPTY_STRING))) {
            if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                String pspRequest = requestHelper.getPsp(folder, EMPTY_STRING);
                resultMetricMap.putAll(metricReporter.generateMetrics(JsonHelper.unpadJson(pspRequest),
                    folderPrefix));
            }
            if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                String logRequest = requestHelper.getLogs(folder, EMPTY_STRING,
                    folderRules.length - 1);
                String unpadded = JsonHelper.unpadJson(logRequest);
                if (unpadded != null) {
                    HashMap<String, String> generatedMetrics =
                            metricReporter.generateMetrics(unpadded, folderPrefix);
                    resultMetricMap.putAll(generatedMetrics);

                    HashMap<String, String> metricMapContent = new HashMap<String, String>();
                    // this is what Andreas changed
                    metricReporter.analyzeContentResults(unpadded, folder, metricMapContent);
                    resultMetricMap.putAll(metricMapContent);
                } else {
                    //TODO error
                }

            }
            //TODO RULE or FOLDER???
            if (properties.getProperty(METRICS_STATS_RULE, FALSE).equals(TRUE)) {
                for (int i = 0; i < folderRules.length; i++) {
                    if (folderRules[i] == ALL_RULES) {
                        continue;
                    }
                    String statsRequest =
                            requestHelper.getStats(folder, folderRules[i]);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(statsRequest), folderPrefix));
                }
            }
        } else {
            for (int j = 0; j < folderRules.length; j++) {
                if (folderRules[j].equals(ALL_RULES)) {
                    continue;
                }
                if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                    String pspRequest = requestHelper.getPsp(folder, folderRules[j]);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(pspRequest), folderPrefix));
                }
                if (properties.getProperty(METRICS_STATS_RULE, FALSE).equals(TRUE)) {
                    String statsRequest =
                        requestHelper.getStats(folder, folderRules[j]);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(statsRequest), folderPrefix));
                }
                if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                    String logRequest = requestHelper.getLogs(folder, folderRules[j], 1);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(logRequest), folderPrefix));
                }
            }
        }

        return resultMetricMap;
    }
}
