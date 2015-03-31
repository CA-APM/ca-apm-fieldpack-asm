package com.ca.apm.swat.epaplugins.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.TextMetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.XMLMetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;


/**
 * Main thread for App Synthetic Monitor EPA plugin.
 */
public class AsmReader implements AsmProperties {

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
            AsmMessages.setLocale(new Locale(apmcmLocale.substring(0, 2),
                apmcmLocale.substring(3,5)));

            AsmReader thisReader = new AsmReader();
            int apmcmEpaWaitTime = Integer.parseInt(
                apmcmProperties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new XMLMetricWriter(psEpa);
            thisReader.work(apmcmEpaWaitTime, apmcmProperties, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR,
                    APMCM_PRODUCT_NAME, e.getMessage()));
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

            AsmReader thisReader = new AsmReader();
            int apmcmEpaWaitTime = Integer.parseInt(
                apmcmProperties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new TextMetricWriter();
            thisReader.work(apmcmEpaWaitTime, apmcmProperties, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR,
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
                        AsmMessages.getMessage(AsmMessages.AGENT_INITIALIZATION));
                } else {
                    EpaUtils.getFeedback().error(
                        AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR,
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
            AsmReaderThread rt = new AsmReaderThread(
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
                        AsmMessages.getMessage(AsmMessages.PARENT_THREAD));
                } else {
                    EpaUtils.getFeedback().error(
                        AsmMessages.getMessage(AsmMessages.RUN_ERROR,
                            APMCM_PRODUCT_NAME,
                            AsmMessages.PARENT_THREAD,
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
        EpaUtils.getFeedback().error(AsmMessages.getMessage(AsmMessages.CONNECTION_ERROR,
            APMCM_PRODUCT_NAME, apmcmInfo));
        if (numRetriesLeft > 0) {
            EpaUtils.getFeedback().info(AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY,
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
            EpaUtils.getFeedback().error(AsmMessages.getMessage(
                AsmMessages.READING_PROPERTIES_ERROR, filename, e.getMessage()));
            throw e;
        }
        inStream.close();

        if (EpaUtils.getFeedback().isDebugEnabled()) {
            EpaUtils.getFeedback().debug(AsmMessages.getMessage(
                AsmMessages.READING_PROPERTIES, filename));
            // use TreeSet so we get output alphabetically sorted
            Set<Object> copy = new TreeSet<Object>(properties.keySet());
            for (Iterator<Object> it = copy.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                EpaUtils.getFeedback().debug(key + "=" + properties.getProperty(key));
            }
        }

        EpaUtils.getFeedback().info(AsmMessages.getMessage(
            AsmMessages.READING_PROPERTIES_FINISHED, filename));
        return properties;
    }
}
