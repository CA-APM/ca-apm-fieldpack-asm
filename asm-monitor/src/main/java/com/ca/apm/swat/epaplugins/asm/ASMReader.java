package com.ca.apm.swat.epaplugins.asm;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.TextMetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.XMLMetricWriter;
import com.ca.apm.swat.epaplugins.utils.ASMMessages;
import com.ca.apm.swat.epaplugins.utils.ASMProperties;
import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.ca.apm.swat.epaplugins.utils.PropertiesUtils;
import com.wily.introscope.epagent.PropertiesReader;



/**
 * Main class for EPA plugin. 
 */
public class ASMReader {

	private HashMap<String, String> creditsMap = new HashMap<String, String>();
	private boolean keepRunning;
	private int numRetriesLeft;

	/**
	 * Called by EPAgent.
	 * @param args arguments
	 * @param psEPA interface to EPAgent, write metrics here
	 * @throws Exception thrown if unrecoverable errors occur
	 */
	public static void main(String[] args, PrintStream psEPA) throws Exception {
		try {
			PropertiesUtils apmcmProperties;
			if (args.length != 0)
				apmcmProperties = new PropertiesUtils(args[0]);
			else {
				apmcmProperties = new PropertiesUtils(ASMProperties.PROPERTY_FILE_NAME);
			}

			String apmcmLocale = apmcmProperties.getProperty(ASMProperties.LOCALE, ASMProperties.DEFAULT_LOCALE);
			ASMMessages.setLocale(new Locale(apmcmLocale.substring(0, 2), apmcmLocale.substring(3,5)));

			ASMReader thisReader = new ASMReader();
			int apmcmEPAWaitTime = Integer.parseInt(apmcmProperties.getProperty(ASMProperties.WAIT_TIME));

			thisReader.workProduction(psEPA, apmcmEPAWaitTime, apmcmProperties);

		} catch (Exception e) {
			System.err.println(ASMMessages.getMessage(ASMMessages.initializationError, new Object []{EPAConstants.apmcmProductName, e.getMessage()}));
			e.printStackTrace();
			System.exit(1);
		}

	}

	public static void main(String[] args) {

		try {
			PropertiesUtils apmcmProperties;
			if (args.length != 0) {
				PropertiesReader.getFeedback().info(ASMMessages.getMessage(ASMMessages.readingProperties, args[0]));
				apmcmProperties = new PropertiesUtils(args[0]);
			} else {
				PropertiesReader.getFeedback().info(ASMMessages.getMessage(ASMMessages.readingProperties, ASMProperties.PROPERTY_FILE_NAME));
				apmcmProperties = new PropertiesUtils(ASMProperties.PROPERTY_FILE_NAME);
			}


			ASMReader thisReader = new ASMReader();
			int apmcmEPAWaitTime = Integer.parseInt(apmcmProperties.getProperty(ASMProperties.WAIT_TIME));

			thisReader.workTest(apmcmEPAWaitTime, apmcmProperties);

		} catch (Exception e) {
			System.err.println(ASMMessages.getMessage(ASMMessages.initializationError, new Object []{EPAConstants.apmcmProductName, e.getMessage()}));
			e.printStackTrace();
			System.exit(1);
		}


	}

	private void workTest(int apmcmEPAWaitTime, PropertiesUtils apmcmProperties) {

		MetricWriter metricWriter = new TextMetricWriter();
		work(apmcmEPAWaitTime, apmcmProperties, metricWriter);

	}

	private void workProduction(PrintStream psEPA, int apmcmEPAWaitTime, PropertiesUtils apmcmProperties)
			throws Exception {

		MetricWriter metricWriter = new XMLMetricWriter(psEPA);
		work(apmcmEPAWaitTime, apmcmProperties, metricWriter);

	}

	private void work(int apmcmEPAWaitTime, PropertiesUtils apmcmProperties, MetricWriter metricWriter) {
		boolean apmcmDisplayMonitor = Boolean.valueOf(Boolean.parseBoolean(apmcmProperties.getProperty(
				ASMProperties.DISPLAY_MONITOR,
				ASMProperties.TRUE)));

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
				if ((e.toString().matches(EPAConstants.kJavaNetExceptionRegex))
						&& (initNumRetriesLeft > 0)) {
					initNumRetriesLeft = retryConnection(initNumRetriesLeft, ASMMessages.getMessage(ASMMessages.agentInitialization));
				} else {
					System.err.println(ASMMessages.getMessage(ASMMessages.initializationError, new Object []{EPAConstants.apmcmProductName, e.getMessage()}));
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
			ASMReaderThread rt = new ASMReaderThread(
					apmcmFolders[i],
					requestHelper,
					folderMap,
					apmcmProperties,
					cloudMonitorMetricReporter);
			rt.start();
		}

		while (keepRunning)
			try {
				if (apmcmProperties.getProperty(ASMProperties.METRICS_CREDITS, ASMProperties.FALSE).equals(ASMProperties.TRUE)) {
					creditsMap.putAll(requestHelper.getCredits());
					cloudMonitorMetricReporter.printMetrics(creditsMap);
					creditsMap.putAll(cloudMonitorMetricReporter.resetMetrics(creditsMap));
				}
				Thread.sleep(apmcmEPAWaitTime);
			} catch (Exception e) {
				if ((e.toString().matches(EPAConstants.kJavaNetExceptionRegex))
						&& (numRetriesLeft > 0)) {
					numRetriesLeft = retryConnection(numRetriesLeft, ASMMessages.getMessage(ASMMessages.parentThread));
				} else {
					System.err.println(ASMMessages.getMessage(ASMMessages.runError, new Object []{EPAConstants.apmcmProductName, ASMMessages.parentThread, e.getMessage()}));
					e.printStackTrace();
					keepRunning = Boolean.valueOf(false);
					System.exit(2);
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

}
