package com.ca.apm.swat.epaplugins.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricNameFilter;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.TextMetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.XmlMetricWriter;
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

    private static AsmReader instance;

    /**
     * Called by EPAgent.
     * @param args arguments
     * @param psEpa interface to EPAgent, write metrics here
     * @throws Exception thrown if unrecoverable errors occur
     */
    public static void main(String[] args, PrintStream psEpa) throws Exception {
        try {
            Properties properties = readPropertiesFromFile((args.length != 0) ? args[0] :
                PROPERTY_FILE_NAME);

            String locale = properties.getProperty(LOCALE, DEFAULT_LOCALE);
            AsmMessages.setLocale(new Locale(locale.substring(0, 2),
                locale.substring(3,5)));

            AsmReader thisReader = new AsmReader();
            int epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = getMetricWriter(psEpa);

            // start main loop
            thisReader.work(epaWaitTime, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR,
                    ASM_PRODUCT_NAME, e.getMessage()));
            System.exit(1);
        } catch (Error e) {
            EpaUtils.getFeedback().error(e.getMessage());
            System.exit(1);
        }

    }

    /**
     * Called when testing.
     * @param args arguments
     */
    public static void main(String[] args) {

        try {
            Properties properties = readPropertiesFromFile(args.length != 0 ? args[0] :
                PROPERTY_FILE_NAME);

            AsmReader reader = AsmReader.getInstance();
            int epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new TextMetricWriter();
            reader.work(epaWaitTime, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR,
                    ASM_PRODUCT_NAME, e.getMessage()));
            // e.printStackTrace();
            System.exit(1);
        }


    }

    /**
     * Get the single instance. Call setProperties() first!
     * @return the one and only Formatter instance
     */
    public static AsmReader getInstance() {
        if (null == instance) {
            instance = new AsmReader();
        }
        return instance;
    }

    /**
     * Set the global properties.
     * @param properties the properties
     */
    protected static void setProperties(Properties properties) {
        EpaUtils.setProperties(properties);
        Formatter.setProperties(properties);
    }

    /**
     * Main method of AsmReader.
     * Connects to ASM API and gets all folder, monitor and monitoring station information.
     * Then starts a thread per folder to collect the monitor metrics.
     * @param epaWaitTime sleep time in main loop
     * @param properties properties read from config file
     * @param metricWriter interface to EPAgent, write metrics here
     */
    private void work(int epaWaitTime, MetricWriter metricWriter) {

        AsmAccessor accessor = new AsmAccessor();
        AsmRequestHelper requestHelper = new AsmRequestHelper(accessor);

        this.keepRunning = true;
        this.numRetriesLeft = 10;

        // connect and read folders, monitors and monitoring stations.
        HashMap<String, List<Monitor>> folderMap = initialize(requestHelper);

        AsmMetricReporter metricReporter = new AsmMetricReporter(metricWriter);

        // TODO: have a thread pool with a fixed number of threads that pick folders from a queue
        // start a thread per folder
        for (Iterator<String> it = folderMap.keySet().iterator(); it.hasNext(); ) {
            AsmReaderThread rt = new AsmReaderThread(
                it.next(),
                requestHelper,
                folderMap,
                metricReporter);
            rt.start();
        }

        while (keepRunning) {
            try {

                // get credits
                if (EpaUtils.getProperty(METRICS_CREDITS, FALSE).equals(TRUE)) {
                    creditsMap = requestHelper.getCredits();
                    metricReporter.printMetrics(creditsMap);
                    //creditsMap.putAll(metricReporter.resetMetrics(creditsMap));
                }

                // TODO: read config and folders again

                Thread.sleep(epaWaitTime);
            } catch (Exception e) {
                if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                        && (numRetriesLeft > 0)) {
                    numRetriesLeft = retryConnection(numRetriesLeft,
                        AsmMessages.getMessage(AsmMessages.PARENT_THREAD));
                } else {
                    EpaUtils.getFeedback().error(
                        AsmMessages.getMessage(AsmMessages.RUN_ERROR,
                            ASM_PRODUCT_NAME,
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
            ASM_PRODUCT_NAME, apmcmInfo));
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
     * Initialize AsmReader: connect and read folders, monitors and monitoring stations.
     * @param requestHelper the request helper
     * @return map of folders and monitors
     */
    public HashMap<String, List<Monitor>> initialize(AsmRequestHelper requestHelper) {
        String[] folders = null;
        HashMap<String, List<Monitor>> folderMap = null;
        boolean keepTrying = true;
        int initNumRetriesLeft = 10;

        while (keepTrying) {
            try {
                // connect
                requestHelper.connect();

                // read folders
                folders = requestHelper.getFolders();

                // TODO: remove or convert to message
                if (EpaUtils.getFeedback().isVerboseEnabled()) {
                    StringBuffer buf = new StringBuffer("read folders: ");
                    for (int i = 0; i < folders.length; ++i) {
                        buf.append(folders[i] + ", ");
                    }
                    EpaUtils.getFeedback().verbose(buf.toString());
                }

                // read monitors
                folderMap = requestHelper.getMonitors(folders);

                // TODO: remove or convert to message
                if (EpaUtils.getFeedback().isVerboseEnabled()) {
                    EpaUtils.getFeedback().verbose("read monitors: ");
                    Set<Object> copy = new TreeSet<Object>(folderMap.keySet());
                    for (Iterator<Object> fit = copy.iterator(); fit.hasNext(); ) {
                        String folder = (String) fit.next();
                        StringBuffer buf = new StringBuffer("  " + folder + " = ");
                        List<Monitor> monitors = folderMap.get(folder);
                        for (Iterator<Monitor> mit = monitors.iterator(); mit.hasNext(); ) {
                            buf.append(mit.next().getName() + ", ");
                        }
                        EpaUtils.getFeedback().verbose(buf.toString());
                    }
                }

                // read monitoring stations
                requestHelper.getMonitoringStations();

                keepTrying = false;

            } catch (Exception e) {
                if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                        && (initNumRetriesLeft > 0)) {
                    initNumRetriesLeft = retryConnection(initNumRetriesLeft,
                        AsmMessages.getMessage(AsmMessages.AGENT_INITIALIZATION));
                } else {
                    EpaUtils.getFeedback().error(
                        AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR,
                            ASM_PRODUCT_NAME, e.getMessage()));
                    // e.printStackTrace();
                    keepTrying = false;
                    System.exit(1);
                }
            }
        }

        EpaUtils.getFeedback().info(AsmMessages.getMessage(
            AsmMessages.CONNECTED, EpaUtils.getProperty(URL)));

        return folderMap;
    }

    /**
     * Returns a {@link MetricWriter} object according to the configuration.
     * @param ps stream to write metrics to
     * @return the new metric writer
     */
    public static MetricWriter getMetricWriter(PrintStream ps) {
        // configure MetricWriter
        MetricWriter metricWriter = new XmlMetricWriter(ps);

        // check for metrics to ignore
        String ignoreMetric = EpaUtils.getProperty(IGNORE_METRICS, EMPTY_STRING);
        if (!EMPTY_STRING.equals(ignoreMetric)) {
            metricWriter = new MetricNameFilter(metricWriter, ignoreMetric.split(","));
        }
        
        return metricWriter;
    }

    /**
     * Read properties from file.
     * @param filename file name
     * @return the properties read
     * @throws IOException error reading the file
     */
    public static Properties readPropertiesFromFile(String filename) throws IOException {
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
        AsmReader.setProperties(properties);

        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            EpaUtils.getFeedback().verbose(AsmMessages.getMessage(
                AsmMessages.READING_PROPERTIES, filename));
            // use TreeSet so we get output alphabetically sorted
            Set<Object> copy = new TreeSet<Object>(properties.keySet());
            for (Iterator<Object> it = copy.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                EpaUtils.getFeedback().verbose(key + "=" + properties.getProperty(key));
            }
        }

        EpaUtils.getFeedback().info(AsmMessages.getMessage(
            AsmMessages.READING_PROPERTIES_FINISHED, filename));
        return properties;
    }
}
