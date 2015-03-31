package com.ca.apm.swat.epaplugins.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.TextMetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.XMLMetricWriter;
import com.ca.apm.swat.epaplugins.utils.ASMMessages;
import com.ca.apm.swat.epaplugins.utils.ASMProperties;
import com.wily.introscope.epagent.EpaUtils;


/**
 * Main thread for App Synthetic Monitor EPA plugin. 
 */
public class ASMReader implements ASMProperties {

    private HashMap<String, String> creditsMap = new HashMap<String, String>();
    private boolean keepRunning;
    private int numRetriesLeft;

    /**
     * Called by EPAgent.
     * @param args arguments
     * @param psEpa interface to EPAgent, write metrics here
     * @throws Exception thrown if unrecoverable errors occur
     */
    public static void main(String[] args, PrintStream psEpa) throws Exception {
        try {
            Properties apmcmProperties = getPropertiesFromFile((args.length != 0) ? args[0] :
                PROPERTY_FILE_NAME);

            String apmcmLocale = apmcmProperties.getProperty(LOCALE, DEFAULT_LOCALE);
            ASMMessages.setLocale(new Locale(apmcmLocale.substring(0, 2),
                apmcmLocale.substring(3,5)));

            ASMReader thisReader = new ASMReader();
            int apmcmEpaWaitTime = Integer.parseInt(
                apmcmProperties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new XMLMetricWriter(psEpa);
            thisReader.work(apmcmEpaWaitTime, apmcmProperties, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                ASMMessages.getMessage(ASMMessages.initializationError, APMCM_PRODUCT_NAME, e.getMessage()));
            //e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Called when testing.
     * @param args arguments
     */
    public static void main(String[] args) {

        try {
            Properties apmcmProperties = getPropertiesFromFile(args.length != 0 ? args[0] :
                PROPERTY_FILE_NAME);

            ASMReader thisReader = new ASMReader();
            int apmcmEpaWaitTime = Integer.parseInt(
                apmcmProperties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new TextMetricWriter();
            thisReader.work(apmcmEpaWaitTime, apmcmProperties, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                ASMMessages.getMessage(ASMMessages.initializationError,
                    APMCM_PRODUCT_NAME, e.getMessage()));
            // e.printStackTrace();
            System.exit(1);
        }


    }

    /**
     * Main method of ASMReader.
     * @param apmcmEpaWaitTime sleep time in main loop
     * @param apmcmProperties properties read from config file
     * @param metricWriter interface to EPAgent, write metrics here
     */
    private void work(int apmcmEpaWaitTime, Properties apmcmProperties, MetricWriter metricWriter) {
        final boolean apmcmDisplayMonitor = Boolean.valueOf(
            Boolean.parseBoolean(apmcmProperties.getProperty(DISPLAY_CHECKPOINTS, TRUE)));

        CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(apmcmProperties);
        CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(
            cloudMonitorAccessor, apmcmProperties);


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
                if ((e.toString().matches(kJavaNetExceptionRegex))
                        && (initNumRetriesLeft > 0)) {
                    initNumRetriesLeft = retryConnection(initNumRetriesLeft,
                        ASMMessages.getMessage(ASMMessages.agentInitialization));
                } else {
                    EpaUtils.getFeedback().error(
                        ASMMessages.getMessage(ASMMessages.initializationError,
                            APMCM_PRODUCT_NAME, e.getMessage()));
                    // e.printStackTrace();
                    keepTrying = false;
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

        while (keepRunning) {
            try {
                if (apmcmProperties.getProperty(METRICS_CREDITS,
                    FALSE).equals(TRUE)) {
                    creditsMap.putAll(requestHelper.getCredits());
                    cloudMonitorMetricReporter.printMetrics(creditsMap);
                    creditsMap.putAll(cloudMonitorMetricReporter.resetMetrics(creditsMap));
                }
                Thread.sleep(apmcmEpaWaitTime);
            } catch (Exception e) {
                if ((e.toString().matches(kJavaNetExceptionRegex))
                        && (numRetriesLeft > 0)) {
                    numRetriesLeft = retryConnection(numRetriesLeft,
                        ASMMessages.getMessage(ASMMessages.parentThread));
                } else {
                    EpaUtils.getFeedback().error(
                        ASMMessages.getMessage(ASMMessages.runError,
                            APMCM_PRODUCT_NAME,
                            ASMMessages.parentThread,
                            e.getMessage()));
                    e.printStackTrace();
                    keepRunning = Boolean.valueOf(false);
                    System.exit(2);
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
        EpaUtils.getFeedback().error(ASMMessages.getMessage(ASMMessages.connectionError,
            APMCM_PRODUCT_NAME, apmcmInfo));
        if (numRetriesLeft > 0) {
            EpaUtils.getFeedback().info(ASMMessages.getMessage(ASMMessages.connectionRetry,
                numRetriesLeft));
            numRetriesLeft--;
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EpaUtils.getFeedback().error(
                ASMMessages.getMessage(ASMMessages.connectionRetryError));
        }
        return numRetriesLeft;
    }

    /**
     * Read properties from file.
     * @param filename file name
     * @return the properties read
     * @throws IOException error reading the file
     */
    public static Properties getPropertiesFromFile(String filename) throws IOException {
        FileInputStream inStream = new FileInputStream(new File(filename));

        Properties properties = new Properties();

        try {
            properties.load(inStream);
        } catch (IOException e) {
            EpaUtils.getFeedback().error(ASMMessages.getMessage(ASMMessages.readingPropertiesError,
                filename, e.getMessage()));
            throw e;
        }
        inStream.close();

        if (EpaUtils.getFeedback().isDebugEnabled()) {
            EpaUtils.getFeedback().debug(ASMMessages.getMessage(ASMMessages.readingProperties,
                filename));
            for (Iterator<Object> it = properties.keySet().iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                EpaUtils.getFeedback().debug(key + "=" + properties.getProperty(key));
            }
        }

        EpaUtils.getFeedback().info(ASMMessages.getMessage(ASMMessages.readingPropertiesFinished));
        return properties;
    }
}
