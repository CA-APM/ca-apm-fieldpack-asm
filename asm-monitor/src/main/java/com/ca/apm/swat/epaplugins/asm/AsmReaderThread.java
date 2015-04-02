package com.ca.apm.swat.epaplugins.asm;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.ca.apm.swat.epaplugins.asm.rules.Rule;
import com.ca.apm.swat.epaplugins.asm.rules.RuleFactory;
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
    private HashMap<String, List<Rule>> folderMap;
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
        HashMap<String, List<Rule>> folderMap,
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

        List<Rule> folderRules = this.folderMap.get(folder);
        Rule allRulesRule = RuleFactory.getAllRulesRule();
        Rule rule = null;
        
        EpaUtils.getFeedback().verbose(
            AsmMessages.getMessage(AsmMessages.GET_FOLDER_DATA, folderRules.size(), folder));

        if ((null == folderRules) || (0 == folderRules.size())) {
            return resultMetricMap;
        }

        // prefix for metric name
        String folderPrefix = MONITOR_METRIC_PREFIX + folder;

        if (ROOT_FOLDER.equals(folder)) {
            folder = EMPTY_STRING;
            // remove trailing '|'
            folderPrefix = MONITOR_METRIC_PREFIX.substring(0, MONITOR_METRIC_PREFIX.length() - 1);
        }

        if (TRUE.equals(properties.getProperty(METRICS_STATS_FOLDER, FALSE))) {
            EpaUtils.getFeedback().verbose(
                AsmMessages.getMessage(AsmMessages.GET_STATS_DATA, folderRules.size(), folder));

            String statsRequest = requestHelper.getStats(folder, null);
            resultMetricMap.putAll(metricReporter.generateMetrics(
                JsonHelper.unpadJson(statsRequest), folderPrefix));
        } else {
            EpaUtils.getFeedback().verbose(
                AsmMessages.getMessage(AsmMessages.GET_NO_STATS_DATA, folder));
        }

        if ((folderRules.get(0).equals(allRulesRule))
                && (!folder.equals(EMPTY_STRING))) {
            
            if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                String pspRequest = requestHelper.getPsp(folder, null);
                resultMetricMap.putAll(metricReporter.generateMetrics(
                    JsonHelper.unpadJson(pspRequest), folderPrefix));
            }
            
            if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                String logRequest = requestHelper.getLogs(folder, null,
                    folderRules.size() - 1);
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
                for (Iterator<Rule> it = folderRules.iterator(); it.hasNext(); ) {
                    rule = it.next();
                    if (rule.equals(allRulesRule)) {
                        continue;
                    }
                    String statsRequest =
                            requestHelper.getStats(folder, rule);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(statsRequest), folderPrefix));
                }
            }
        } else {
            for (Iterator<Rule> it = folderRules.iterator(); it.hasNext(); ) {
                rule = it.next();
                if (rule.equals(ALL_RULES)) {
                    continue;
                }
                if (properties.getProperty(METRICS_PUBLIC, FALSE).equals(TRUE)) {
                    String pspRequest = requestHelper.getPsp(folder, rule);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(pspRequest), folderPrefix));
                }
                if (properties.getProperty(METRICS_STATS_RULE, FALSE).equals(TRUE)) {
                    String statsRequest =
                            requestHelper.getStats(folder, rule);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(statsRequest), folderPrefix));
                }
                if (properties.getProperty(METRICS_LOGS, FALSE).equals(TRUE)) {
                    String logRequest = requestHelper.getLogs(folder,rule, 1);
                    resultMetricMap.putAll(metricReporter.generateMetrics(
                        JsonHelper.unpadJson(logRequest), folderPrefix));
                }
            }
        }

        return resultMetricMap;
    }
}
