package com.wily.fieldext.epaplugins.apmcloudmonitor;

import java.util.Date;
import java.util.HashMap;

public class APMCMReaderThread extends Thread
{
  APMCMReader thisReader;
  String thisFolder;
  private HashMap<String, String> thisMetricMap = new HashMap<String,String>();
  Boolean keepRunning = Boolean.valueOf(true);
  int numRetriesLeft;

  APMCMReaderThread(APMCMReader parentReader, String folderName)
  {
    this.thisReader = parentReader;
    this.thisFolder = folderName;
    this.numRetriesLeft = 10;
  }

  public void run()
  {
    while (this.keepRunning.booleanValue())
      try {
        Date startTime = new Date();
        this.thisMetricMap.putAll(this.thisReader.getFolderMetrics(this.thisFolder));
        this.thisReader.printMetrics(this.thisMetricMap);
        this.thisMetricMap.putAll(this.thisReader.resetMetrics(this.thisMetricMap));
        Date endTime = new Date();

        long timeElapsed = endTime.getTime() - startTime.getTime();
        long timeToSleep = this.thisReader.apmcmEPAWaitTime - timeElapsed;
        if (timeToSleep > 0L) {
          Thread.sleep(timeToSleep);
        } else {
          System.err.println("Folder thread " + this.thisFolder + " took longer than " + 
            this.thisReader.apmcmEPAWaitTime + " msecs to report.");
          Thread.sleep(60000L);
        }
      } catch (Exception e) {
        if ((e.toString().matches(".*(BindException|ConnectException|HttpRetryException|NoRouteToHostException|ProtocolException|SocketException|SocketTimeoutException|UnknownHostException).*")) && 
          (this.numRetriesLeft > 0)) {
          this.numRetriesLeft = this.thisReader.retryConnection(this.numRetriesLeft, this.thisFolder);
        } else {
          System.err.println("Error running APM Cloud Monitor Agent - Folder Thread" + this.thisFolder);
          e.printStackTrace();
          this.keepRunning = Boolean.valueOf(false);
        }
      }
  }
}

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.apmcloudmonitor.APMCMReaderThread
 * JD-Core Version:    0.6.0
 */