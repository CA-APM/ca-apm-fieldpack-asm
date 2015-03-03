package com.ca.apm.swat.epaplugins.asm;

import java.util.Date;
import java.util.HashMap;

import com.ca.apm.swat.epaplugins.utils.JSONHelper;
import com.ca.apm.swat.epaplugins.utils.PropertiesUtils;

public class APMCMReaderThread extends Thread {
  String thisFolder;
  private HashMap<String, String> thisMetricMap = new HashMap<String, String>();
  boolean keepRunning = true;
  int numRetriesLeft;
  private CloudMonitorRequestHelper requestHelper;
  private HashMap<String, String[]> folderMap;
  private String apmcmUser;
  private PropertiesUtils apmcmProperties;
  private CloudMonitorMetricReporter metricReporter;
  private final int apmcmEPAWaitTime;

  APMCMReaderThread(
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
    apmcmUser = apmcmProperties.getProperty("apmcm.user");
    apmcmEPAWaitTime = Integer.parseInt(apmcmProperties.getProperty("apmcm.waittime"));
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
          System.err.println("Folder thread " + this.thisFolder + " took longer than " + apmcmEPAWaitTime
            + " msecs to report.");
          Thread.sleep(60000L);
        }
      } catch (Exception e) {
        if ((e.toString().matches(".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*"))
          && (this.numRetriesLeft > 0)) {
          this.numRetriesLeft = retryConnection(this.numRetriesLeft, this.thisFolder);
        } else {
          System.err.println("Error running APM Cloud Monitor Agent - Folder Thread" + this.thisFolder);
          e.printStackTrace();
          this.keepRunning = Boolean.valueOf(false);
        }
      }
  }

  public int retryConnection(int numRetriesLeft, String apmcmInfo) {
    System.err.println("Error connecting to Watchmouse for " + apmcmInfo);
    if (numRetriesLeft > 0) {
      System.err.println("Will retry connection " + numRetriesLeft + " more times.");
      numRetriesLeft--;
      try {
        Thread.sleep(60000L);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("Retried connection 10 times.");
    }
    return numRetriesLeft;
  }

  public HashMap<String, String> getFolderMetrics(String folder) throws Exception {
    HashMap<String, String> metric_map = new HashMap<String, String>();

    String[] thisFolderRules = (String[]) this.folderMap.get(folder);

    if (thisFolderRules.length == 1) {
      return metric_map;
    }

    if (folder.equals("root_folder")) {
      folder = "";
    }

    if (apmcmProperties.getProperty("apmcm.metrics.stats.folder", "false").equals("true")) {

      String statsRequest = requestHelper.getStats(folder, "", apmcmUser);
      metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(statsRequest), "Monitors|" + folder));
    }

    if ((thisFolderRules[0].equals("all_rules")) && (!folder.equals(""))) {
      if (apmcmProperties.getProperty("apmcm.metrics.public", "false").equals("true")) {
        String pspRequest = requestHelper.getPSP(folder, "", apmcmUser);
        metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(pspRequest), "Monitors|" + folder));
      }
      if (apmcmProperties.getProperty("apmcm.metrics.logs", "false").equals("true")) {
        String logRequest = requestHelper.getLogs(folder, "", thisFolderRules.length - 1);
        String unpadded = JSONHelper.unpadJSON(logRequest);
        if (unpadded != null) {
          HashMap<String, String> generatedMetrics = metricReporter.generateMetrics(unpadded, "Monitors|" + folder);
          metric_map.putAll(generatedMetrics);

          HashMap<String, String> metric_map_Content = new HashMap<String, String>();
          // this is what Andreas changed
          metricReporter.analyzeContentResults(unpadded, folder, metric_map_Content);
          metric_map.putAll(metric_map_Content);
        } else {
          //Todo error
        }

      }
      if (apmcmProperties.getProperty("apmcm.metrics.stats.rule", "false").equals("true"))
        for (int i = 0; i < thisFolderRules.length; i++) {
          if (thisFolderRules[i] == "all_rules") {
            continue;
          }
          String statsRequest = requestHelper.getStats(folder, thisFolderRules[i], apmcmUser);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(statsRequest), "Monitors|" + folder));
        }
    } else {
      for (int j = 0; j < thisFolderRules.length; j++) {
        if (thisFolderRules[j].equals("all_rules")) {
          continue;
        }
        if (apmcmProperties.getProperty("apmcm.metrics.public", "false").equals("true")) {
          String pspRequest = requestHelper.getPSP(folder, thisFolderRules[j], apmcmUser);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(pspRequest), "Monitors|" + folder));
        }
        if (apmcmProperties.getProperty("apmcm.metrics.stats.rule", "false").equals("true")) {
          String statsRequest = requestHelper.getStats(folder, thisFolderRules[j], apmcmUser);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(statsRequest), "Monitors|" + folder));
        }
        if (apmcmProperties.getProperty("apmcm.metrics.logs", "false").equals("true")) {
          String logRequest = requestHelper.getLogs(folder, thisFolderRules[j], 1);
          metric_map.putAll(metricReporter.generateMetrics(JSONHelper.unpadJSON(logRequest), "Monitors|" + folder));
        }
      }
    }

    return metric_map;
  }
}
