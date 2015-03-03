package com.ca.apm.swat.epaplugins.asm;

import java.util.Date;
import java.util.HashMap;

import com.ca.apm.swat.epaplugins.utils.ASMMessages;
import com.ca.apm.swat.epaplugins.utils.ASMProperties;
import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.ca.apm.swat.epaplugins.utils.JSONHelper;
import com.ca.apm.swat.epaplugins.utils.PropertiesUtils;

/**
 * TODO: add javadoc
 *
 */
public class ASMReaderThread extends Thread {
private String thisFolder;
  private HashMap<String, String> thisMetricMap = new HashMap<String, String>();
  private boolean keepRunning = true;
  private int numRetriesLeft;
  private CloudMonitorRequestHelper requestHelper;
  private HashMap<String, String[]> folderMap;
  private String apmcmUser;
  private PropertiesUtils apmcmProperties;
  private CloudMonitorMetricReporter metricReporter;
  private final int apmcmEPAWaitTime;

  ASMReaderThread(
    String folderName,
    CloudMonitorRequestHelper requestHelper,
    HashMap<String, String[]> folderMap,
    PropertiesUtils apmcmProperties,
    CloudMonitorMetricReporter metricReporter) {
    this.thisFolder = folderName;
    this.requestHelper = requestHelper;
    this.folderMap = folderMap;
    this.apmcmProperties = apmcmProperties;
    this.metricReporter = metricReporter;
    this.numRetriesLeft = 10;
    apmcmUser = apmcmProperties.getProperty(ASMProperties.USER);
    apmcmEPAWaitTime = Integer.parseInt(apmcmProperties.getProperty(ASMProperties.WAIT_TIME));
  }

  public void run() {
    while (this.keepRunning)
      try {
        Date startTime = new Date();
        this.thisMetricMap.putAll(getFolderMetrics(this.thisFolder));
        metricReporter.printMetrics(this.thisMetricMap);
        this.thisMetricMap.putAll(metricReporter.resetMetrics(this.thisMetricMap));
        Date endTime = new Date();

        long timeElapsed = endTime.getTime() - startTime.getTime();
        long timeToSleep = apmcmEPAWaitTime - timeElapsed;
        if (timeToSleep > 0L) {
          Thread.sleep(timeToSleep);
        } else {
          System.err.println(ASMMessages.getMessage(ASMMessages.folderThreadTimeout, new Object[]{this.thisFolder, new Long(apmcmEPAWaitTime)}));
          Thread.sleep(60000L);
        }
      } catch (Exception e) {
        if ((e.toString().matches(EPAConstants.kJavaNetExceptionRegex))
          && (this.numRetriesLeft > 0)) {
          this.numRetriesLeft = retryConnection(this.numRetriesLeft, this.thisFolder);
        } else {
          System.err.println(ASMMessages.getMessage(ASMMessages.folderThreadError, new Object[]{EPAConstants.apmcmProductName, this.thisFolder, e.getMessage()}));
          e.printStackTrace();
          this.keepRunning = Boolean.valueOf(false);
        }
      }
  }

  public int retryConnection(int numRetriesLeft, String apmcmInfo) {
    System.err.println(ASMMessages.getMessage(ASMMessages.connectionError, new Object[]{EPAConstants.apmcmProductName,apmcmInfo}));
    if (numRetriesLeft > 0) {
      System.err.println(ASMMessages.getMessage(ASMMessages.connectionRetry, new Object[]{numRetriesLeft}));
      numRetriesLeft--;
      try {
        Thread.sleep(60000L);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
		System.err.println(ASMMessages.getMessage(ASMMessages.connectionRetryError));
   }
    return numRetriesLeft;
  }

  public HashMap<String, String> getFolderMetrics(String folder) throws Exception {
    HashMap<String, String> metric_map = new HashMap<String, String>();

    String[] thisFolderRules = (String[]) this.folderMap.get(folder);

    if (thisFolderRules.length == 1) {
      return metric_map;
    }

    if (folder.equals(EPAConstants.apmcmRootFolder)) {
      folder = "";
    }

    if (apmcmProperties.getProperty(ASMProperties.METRICS_STATS_FOLDER, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {

      String statsRequest = requestHelper.getStats(folder, "", apmcmUser);
      metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(statsRequest), EPAConstants.apmcmMonitorMetricPrefix + folder));
    }

    if ((thisFolderRules[0].equals(EPAConstants.apmcmAllRules)) && (!folder.equals(""))) {
      if (apmcmProperties.getProperty(ASMProperties.METRICS_PUBLIC, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {
        String pspRequest = requestHelper.getPSP(folder, "", apmcmUser);
        metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(pspRequest), EPAConstants.apmcmMonitorMetricPrefix + folder));
      }
      if (apmcmProperties.getProperty(ASMProperties.METRICS_LOGS, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {
        String logRequest = requestHelper.getLogs(folder, "", thisFolderRules.length - 1);
        String unpadded = JSONHelper.unpadJSON(logRequest);
        if (unpadded != null) {
          HashMap<String, String> generatedMetrics = metricReporter.generateMetrics(unpadded, EPAConstants.apmcmMonitorMetricPrefix + folder);
          metric_map.putAll(generatedMetrics);

          HashMap<String, String> metric_map_Content = new HashMap<String, String>();
          // this is what Andreas changed
          metricReporter.analyzeContentResults(unpadded, folder, metric_map_Content);
          metric_map.putAll(metric_map_Content);
        } else {
          //TODO error
        }

      }
      //TODO RULE or FOLDER???
      if (apmcmProperties.getProperty(ASMProperties.METRICS_STATS_RULE, ASMProperties.FALSE).equals(ASMProperties.TRUE))
        for (int i = 0; i < thisFolderRules.length; i++) {
          if (thisFolderRules[i] == EPAConstants.apmcmAllRules) {
            continue;
          }
          String statsRequest = requestHelper.getStats(folder, thisFolderRules[i], apmcmUser);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(statsRequest), EPAConstants.apmcmMonitorMetricPrefix + folder));
        }
    } else {
      for (int j = 0; j < thisFolderRules.length; j++) {
        if (thisFolderRules[j].equals(EPAConstants.apmcmAllRules)) {
          continue;
        }
        if (apmcmProperties.getProperty(ASMProperties.METRICS_PUBLIC, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {
          String pspRequest = requestHelper.getPSP(folder, thisFolderRules[j], apmcmUser);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(pspRequest), EPAConstants.apmcmMonitorMetricPrefix + folder));
        }
        if (apmcmProperties.getProperty(ASMProperties.METRICS_STATS_RULE, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {
          String statsRequest = requestHelper.getStats(folder, thisFolderRules[j], apmcmUser);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(statsRequest), EPAConstants.apmcmMonitorMetricPrefix + folder));
        }
        if (apmcmProperties.getProperty(ASMProperties.METRICS_LOGS, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {
          String logRequest = requestHelper.getLogs(folder, thisFolderRules[j], 1);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(logRequest), EPAConstants.apmcmMonitorMetricPrefix + folder));
        }
      }
    }

    return metric_map;
  }
}
