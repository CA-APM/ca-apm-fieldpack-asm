package com.wily.fieldext.epaplugins.apmcloudmonitor;

import java.io.PrintStream;
import java.util.HashMap;

import com.wily.field.apmcloudminder.reporting.DummyMetricWriter;
import com.wily.field.apmcloudminder.reporting.MetricWriter;
import com.wily.field.apmcloudminder.reporting.MetricWriterISC;
import com.wily.fieldext.epaplugins.utils.PropertiesUtils;
import com.wily.introscope.epagent.PropertiesReader;



public class APMCMReader {

  private HashMap<String, String> creditsMap = new HashMap<String, String>();
  private boolean keepRunning;
  private int numRetriesLeft;

  public static void main(String[] args, PrintStream psEPA) throws Exception {
    try {
      PropertiesUtils apmcmProperties;
      if (args.length != 0)
        apmcmProperties = new PropertiesUtils(args[0]);
      else {
        apmcmProperties = new PropertiesUtils("APMCloudMonitor.properties");
      }


      APMCMReader thisReader = new APMCMReader();
      int apmcmEPAWaitTime = Integer.parseInt(apmcmProperties.getProperty("apmcm.waittime"));

      thisReader.workProduction(psEPA, apmcmEPAWaitTime, apmcmProperties);

    } catch (Exception e) {
      System.err.println("Error initializing Watchmouse EPAgent.");
      e.printStackTrace();
      System.exit(1);
    }

  }

  public static void main(String[] args) {

    try {
      PropertiesUtils apmcmProperties;
      if (args.length != 0) {
        PropertiesReader.getFeedback().info("Reading properties from " + args[0]);
        apmcmProperties = new PropertiesUtils(args[0]);
      } else {
        PropertiesReader.getFeedback().info("Reading properties from APMCloudMonitor.properties");
        apmcmProperties = new PropertiesUtils("APMCloudMonitor.properties");
      }


      APMCMReader thisReader = new APMCMReader();
      int apmcmEPAWaitTime = Integer.parseInt(apmcmProperties.getProperty("apmcm.waittime"));

      thisReader.workTest(apmcmEPAWaitTime, apmcmProperties);

    } catch (Exception e) {
      System.err.println("Error initializing Watchmouse EPAgent.");
      e.printStackTrace();
      System.exit(1);
    }


  }

  private void workTest(int apmcmEPAWaitTime, PropertiesUtils apmcmProperties) {

    MetricWriter metricWriter = new DummyMetricWriter();
    work(apmcmEPAWaitTime, apmcmProperties, metricWriter);

  }

  private void workProduction(PrintStream psEPA, int apmcmEPAWaitTime, PropertiesUtils apmcmProperties)
      throws Exception {

    MetricWriter metricWriter = new MetricWriterISC(psEPA);
    work(apmcmEPAWaitTime, apmcmProperties, metricWriter);

  }

  private void work(int apmcmEPAWaitTime, PropertiesUtils apmcmProperties, MetricWriter metricWriter) {
    boolean apmcmDisplayMonitor = Boolean.valueOf(Boolean.parseBoolean(apmcmProperties.getProperty(
      "apmcm.displaymonitor",
      "true")));

    CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(apmcmProperties);
    CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(cloudMonitorAccessor, apmcmProperties);


    this.keepRunning = true;
    this.numRetriesLeft = 10;

    String[] apmcmFolders = null;
    HashMap<String, String> cpMap = null;
    HashMap<String, String[]> folderMap = null;
    boolean keepTrying = true;
    int initNumRetriesLeft = 10;
    while (keepTrying) {
      try {
        requestHelper.connect();
        apmcmFolders = requestHelper.getFolders();
        folderMap = requestHelper.getFoldersAndRules(apmcmFolders);
        cpMap = requestHelper.getCheckpoints();
        keepTrying = false;
      } catch (Exception e) {
        if ((e.toString().matches(".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*"))
          && (initNumRetriesLeft > 0)) {
          initNumRetriesLeft = retryConnection(initNumRetriesLeft, "Agent Initialization");
        } else {
          System.err.println("Error initializing Watchmouse EPAgent.");
          e.printStackTrace();
          keepTrying = Boolean.valueOf(false);
          System.exit(1);
        }
      }
    }

    CloudMonitorMetricReporter cloudMonitorMetricReporter = new CloudMonitorMetricReporter(
      metricWriter,
      apmcmDisplayMonitor,
      cpMap);


    //Collect folders
    for (int i = 0; i < apmcmFolders.length; i++) {
      APMCMReaderThread rt = new APMCMReaderThread(
        apmcmFolders[i],
        requestHelper,
        folderMap,
        apmcmProperties,
        cloudMonitorMetricReporter);
      rt.start();
    }

    while (keepRunning)
      try {
        if (apmcmProperties.getProperty("apmcm.metrics.credits", "false").equals("true")) {
          creditsMap.putAll(requestHelper.getCredits());
          cloudMonitorMetricReporter.printMetrics(creditsMap);
          creditsMap.putAll(cloudMonitorMetricReporter.resetMetrics(creditsMap));
        }
        Thread.sleep(apmcmEPAWaitTime);
      } catch (Exception e) {
        if ((e.toString().matches(".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*"))
          && (numRetriesLeft > 0)) {
          numRetriesLeft = retryConnection(numRetriesLeft, "Parent Thread");
        } else {
          System.err.println("Error running APM Cloud Monitor Agent - Parent Thread");
          e.printStackTrace();
          keepRunning = Boolean.valueOf(false);
          System.exit(2);
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



}
