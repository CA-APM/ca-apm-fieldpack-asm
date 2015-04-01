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
    private String thisFolder;
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
        
        this.thisFolder = folderName;
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
            AsmMessages.THREAD_STARTED, this.thisFolder));
        
        while (this.keepRunning) {
            try {
                final Date startTime = new Date();
                this.metricMap.putAll(getFolderMetrics(this.thisFolder));
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
                            this.thisFolder, new Long(epaWaitTime)));
                    Thread.sleep(60000L);
                }
            } catch (Exception e) {
                if (kJavaNetExceptionRegex.matches(e.toString()) && (this.numRetriesLeft > 0)) {
                    this.numRetriesLeft = retryConnection(this.numRetriesLeft, this.thisFolder);
                } else {
                    EpaUtils.getFeedback().error(AsmMessages.getMessage(
                        AsmMessages.FOLDER_THREAD_ERROR,
                       ASM_PRODUCT_NAME, this.thisFolder, e.getMessage()));
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
     * Get the current metrics for a folder.
     * @param folder folder name
     * @return map of metrics
     * @throws Exception errors
     */
    public HashMap<String, String> getFolderMetrics(String folder) throws Exception {
        HashMap<String, String> resultMetricMap = new HashMap<String, String>();

        String[] thisFolderRules = (String[]) this.folderMap.get(folder);

        if (thisFolderRules.length == 1) {
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
            String statsRequest = requestHelper.getStats(folder, EMPTY_STRING);
            resultMetricMap.putAll(metricReporter.generateMetrics(JsonHelper.unpadJson(statsRequest),
                folderPrefix));
        }

        if ((thisFolderRules[0].equals(ALL_RULES)) && (!folder.equals(EMPTY_STRING))) {
            if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                String pspRequest = requestHelper.getPsp(folder, EMPTY_STRING);
                resultMetricMap.putAll(metricReporter.generateMetrics(JsonHelper.unpadJson(pspRequest),
                    folderPrefix));
            }
            if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                String logRequest = requestHelper.getLogs(folder, EMPTY_STRING,
                    thisFolderRules.length - 1);
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
                for (int i = 0; i < thisFolderRules.length; i++) {
                    if (thisFolderRules[i] == ALL_RULES) {
                        continue;
                    }
                    String statsRequest =
                            requestHelper.getStats(folder, thisFolderRules[i]);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(statsRequest), folderPrefix));
                }
            }
        } else {
            for (int j = 0; j < thisFolderRules.length; j++) {
                if (thisFolderRules[j].equals(ALL_RULES)) {
                    continue;
                }
                if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                    String pspRequest = requestHelper.getPsp(folder, thisFolderRules[j]);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(pspRequest), folderPrefix));
                }
                if (properties.getProperty(METRICS_STATS_RULE, FALSE).equals(TRUE)) {
                    String statsRequest =
                        requestHelper.getStats(folder, thisFolderRules[j]);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(statsRequest), folderPrefix));
                }
                if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                    String logRequest = requestHelper.getLogs(folder, thisFolderRules[j], 1);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(logRequest), folderPrefix));
                }
            }
        }

        return resultMetricMap;
    }
}
