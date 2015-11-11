package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricNameFilter;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.TextMetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.XmlMetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.FileWatcher;
import com.wily.introscope.epagent.EpaUtils;


/**
 * Main thread for App Synthetic Monitor EPA plugin.
 */
public class AsmReader implements AsmProperties {

    private static final long DEFAULT_UPDATE_INTERVAL = 60L; // 60 minutes

    private HashMap<String, String> creditsMap = new HashMap<String, String>();
    private boolean keepRunning;
    private int numRetriesLeft;
    private long configUpdateInterval = DEFAULT_UPDATE_INTERVAL * 60000;
    private long lastConfigUpdateTimestamp = 0;
    private AsmRequestHelper requestHelper = null;
    private AsmMetricReporter metricReporter = null;
    private HashMap<String, List<Monitor>> folderMap = null;
    private HashMap<String, AsmReaderThread> threadMap = null;

    private static String propertyFileName = PROPERTY_FILE_NAME;
    private static AsmReader instance;

    /**
     * Called by EPAgent.
     * @param args arguments
     * @param psEpa interface to EPAgent, write metrics here
     * @throws Exception thrown if unrecoverable errors occur
     */
    public static void main(String[] args, PrintStream psEpa) throws Exception {
        try {
            propertyFileName = (args.length != 0) ? args[0] :
                PROPERTY_FILE_DIR + '/' + PROPERTY_FILE_NAME;
            
            if (null == propertyFileName) {
                System.out.println("propertyFileName = null"); 
            } else {
                System.out.println("propertyFileName = " + propertyFileName
                    /* + ", args[0] = " + args[0]*/); 
            }

            
            // read properties
            Properties properties = null;
            int retries = 2;
            do {
                try {
                    properties = readPropertiesFromFile(propertyFileName);
                } catch (FileNotFoundException e) {
                    String oldPropertyFileName = propertyFileName;
                    // retry with default names
                    if (2 == retries) {
                        propertyFileName = PROPERTY_FILE_DIR + '/' + PROPERTY_FILE_NAME;
                    } else {
                        propertyFileName = PROPERTY_FILE_NAME;
                    }
                    --retries;
                    //EpaUtils.getFeedback().verbose(
                    System.out.println("property file '" + oldPropertyFileName
                        + "' not found, retrying with '" + propertyFileName + "'");
                }
            } while ((null == properties) && (0 < retries)); 

            String locale = properties.getProperty(LOCALE, DEFAULT_LOCALE);
            AsmMessages.setLocale(new Locale(locale.substring(0, 2),
                locale.substring(3,5)));

            AsmReader thisReader = AsmReader.getInstance();
            int epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = getMetricWriter(psEpa);

            // start main loop
            thisReader.work(epaWaitTime, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR_900,
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
            propertyFileName = (args.length != 0) ? args[0] : PROPERTY_FILE_NAME;
            Properties properties = readPropertiesFromFile(propertyFileName);

            AsmReader reader = AsmReader.getInstance();
            int epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new TextMetricWriter();
            reader.work(epaWaitTime, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR_900,
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

        // set AsmReader instance configUpdateInterval 
        String interval = EpaUtils.getProperty(CONFIG_UPDATE_INTERVAL,
            Long.toString(DEFAULT_UPDATE_INTERVAL));
        if (interval != null) {
            try {
                // convert from minutes to ms
                getInstance().configUpdateInterval = Long.parseLong(interval) * 60000;
            } catch (NumberFormatException e) {
                EpaUtils.getFeedback().warn(
                    AsmMessages.getMessage(AsmMessages.NON_INT_PROPERTY_WARN_700,
                        CONFIG_UPDATE_INTERVAL,
                        interval,
                        getInstance().configUpdateInterval));

            }
        }


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
        requestHelper = new AsmRequestHelper(accessor);
        metricReporter = new AsmMetricReporter(metricWriter);

        this.keepRunning = true;
        this.numRetriesLeft = 10;

        Date now = new Date();
        lastConfigUpdateTimestamp = now.getTime();

        // connect and read folders, monitors and monitoring stations.
        folderMap = initialize(requestHelper);
        threadMap = startThreads(folderMap);

        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            if (null == threadMap) {
                EpaUtils.getFeedback().verbose("work(): threadMap is null");
            } else {
                StringBuffer buf = new StringBuffer("work(): threadMap has ");
                buf.append(threadMap.size());
                buf.append(" folders: ");
                boolean first = true;
                for (Iterator<String> it = threadMap.keySet().iterator(); it.hasNext(); ) {
                    if (!first) {
                        buf.append(", ");
                    } else {
                        first = false;
                    }
                    buf.append(it.next());
                }

                EpaUtils.getFeedback().verbose(buf.toString());
            }
        }  

        while (keepRunning) {
            try {

                // get credits
                if (EpaUtils.getBooleanProperty(METRICS_CREDITS, false)) {
                    creditsMap = requestHelper.getCredits();
                    metricReporter.printMetrics(creditsMap);
                    //creditsMap.putAll(metricReporter.resetMetrics(creditsMap));
                }
                
                // print API stats
                requestHelper.printApiCallStatistics();

                // check if we need to reread the configuration
                now = new Date();
                long timeElapsed = now.getTime() - lastConfigUpdateTimestamp;
                if ((configUpdateInterval > 0) && (configUpdateInterval < timeElapsed)) {
                    lastConfigUpdateTimestamp = now.getTime();

                    stopThreads(threadMap);
                    folderMap = readConfiguration();
                    threadMap = startThreads(folderMap);
                }
                Thread.sleep(epaWaitTime);
            } catch (Exception e) {
                if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                        && (numRetriesLeft > 0)) {
                    numRetriesLeft = retryConnection(numRetriesLeft,
                        AsmMessages.getMessage(AsmMessages.PARENT_THREAD));
                } else if (e instanceof InterruptedException) {
                    // ignore, the config file has changed
                    EpaUtils.getFeedback().verbose(
                        e.getMessage() == null ? e.toString() : e.getMessage());
                } else {
                    EpaUtils.getFeedback().error(
                        AsmMessages.getMessage(AsmMessages.RUN_ERROR_904,
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
     * Start reader threads for folders.
     * @param folderMap map of the folders
     * @return map of threads
     */
    private HashMap<String, AsmReaderThread> startThreads(HashMap<String,
        List<Monitor>> folderMap) {

        HashMap<String, AsmReaderThread> threadMap = new HashMap<String, AsmReaderThread>();        

        // TODO: have a thread pool with a fixed number of threads that pick folders from a queue
        // start a thread per folder
        for (Iterator<String> it = folderMap.keySet().iterator(); it.hasNext(); ) {
            String folder = it.next();
            AsmReaderThread rt = new AsmReaderThread(
                folder,
                requestHelper,
                folderMap,
                metricReporter);
            threadMap.put(folder, rt);
            rt.start();
        }

        // watch configuration file
        startFileWatcher();

        return threadMap;
    }

    /**
     * Start file watch task for configuration file
     */
    private void startFileWatcher() {
        // create task to watch for property file changes
        TimerTask fileWatchTask = new FileWatcher(new File(AsmReader.propertyFileName)) {
            protected void onChange(File file) {
                // here we code the action on a change
                EpaUtils.getFeedback().info(AsmMessages.getMessage(
                    AsmMessages.PROPERTY_FILE_CHANGED_506, file.getPath()));
                
                try {
                    stopThreads(AsmReader.getInstance().threadMap);
                    AsmReader.setProperties(readPropertiesFromFile(file.getPath()));
                    AsmReader.getInstance().folderMap = readConfiguration();
                    AsmReader.getInstance().threadMap =
                            startThreads(AsmReader.getInstance().folderMap);
                } catch (Exception e) {
                    if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                            && (numRetriesLeft > 0)) {
                        numRetriesLeft = retryConnection(numRetriesLeft,
                            AsmMessages.getMessage(AsmMessages.PARENT_THREAD));
                    } else {
                        EpaUtils.getFeedback().error(
                            AsmMessages.getMessage(AsmMessages.RUN_ERROR_904,
                                ASM_PRODUCT_NAME,
                                AsmMessages.PARENT_THREAD,
                                e.getMessage() == null ? e.toString() : e.getMessage() ));
                        if (EpaUtils.getFeedback().isVerboseEnabled()) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            PrintStream stream = new PrintStream(out);
                            e.printStackTrace(stream);
                            EpaUtils.getFeedback().verbose(out.toString());
                        }
                        keepRunning = Boolean.valueOf(false);
                        System.exit(2);
                    }
                }
            }
        };

        Timer timer = new Timer();
        // repeat the check every minute
        timer.schedule(fileWatchTask, new Date(), 60000);
    }

    /**
     * Stop reader threads for folders.
     * @param threadMap map of threads
     */
    private void stopThreads(HashMap<String, AsmReaderThread> threadMap) {

        if (null == threadMap) {
            EpaUtils.getFeedback().warn("threadmap is null");
            throw new IllegalStateException("threadmap is null");
        } else if (EpaUtils.getFeedback().isVerboseEnabled()) {
            StringBuffer buf = new StringBuffer("stopThreads(): threadMap has ");
            buf.append(threadMap.size());
            buf.append(" folders: ");
            boolean first = true;
            for (Iterator<String> it = threadMap.keySet().iterator(); it.hasNext(); ) {
                if (!first) {
                    buf.append(", ");
                } else {
                    first = false;
                }
                buf.append(it.next());
            }

            EpaUtils.getFeedback().verbose(buf.toString());
        }

        // tell threads to stop
        for (Iterator<AsmReaderThread> it = threadMap.values().iterator(); it.hasNext(); ) {
            AsmReaderThread thread = it.next();
            thread.stopThread();
            thread.interrupt();
            EpaUtils.getFeedback().verbose("interrupted thread " + thread.getName());
        }

        // wait for threads to stop
        for (Iterator<AsmReaderThread> it = threadMap.values().iterator(); it.hasNext(); ) {
            do {
                try {
                    AsmReaderThread thread = it.next();
                    EpaUtils.getFeedback().verbose("waiting for thread " + thread.getName()
                        + " to finish");
                    thread.join();
                    EpaUtils.getFeedback().verbose("thread " + thread.getName() + " finished");
                } catch (InterruptedException e) {
                    // ignore
                }
            } while (Thread.interrupted());
        }
        EpaUtils.getFeedback().verbose("exiting stopThread()");
    }
    
    /**
     * Read the configuration: update folders. monitors and stations
     * @return map of folders and monitors
     * @throws Exception an error occurred
     */
    private HashMap<String, List<Monitor>> readConfiguration() throws Exception {
        String[] folders = null;
        HashMap<String, List<Monitor>> folderMap = null;

        // read folders
        folders = requestHelper.getFolders();

        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            StringBuffer buf = new StringBuffer("read folders: ");
            for (int i = 0; i < folders.length; ++i) {
                buf.append(folders[i] + ", ");
            }
            EpaUtils.getFeedback().verbose(buf.toString());
        }

        // read monitors
        folderMap = requestHelper.getMonitors(folders);

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
        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            EpaUtils.getFeedback().verbose("read monitoring stations");
        }

        EpaUtils.getFeedback().info(AsmMessages.getMessage(
            AsmMessages.READ_CONFIGURATION_505, EpaUtils.getProperty(URL)));

        return folderMap;
    }

    /**
     * Retry to connect.
     * @param numRetriesLeft retries left
     * @param apmcmInfo message to log
     * @return number of retries left
     */
    public int retryConnection(int numRetriesLeft, String apmcmInfo) {
        EpaUtils.getFeedback().error(AsmMessages.getMessage(AsmMessages.CONNECTION_ERROR_902,
            ASM_PRODUCT_NAME, apmcmInfo));
        if (numRetriesLeft > 0) {
            EpaUtils.getFeedback().info(AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_501,
                numRetriesLeft));
            numRetriesLeft--;
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EpaUtils.getFeedback().error(
                AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_ERROR_903));
        }
        return numRetriesLeft;
    }

    /**
     * Initialize AsmReader: connect and read folders, monitors and monitoring stations.
     * @param requestHelper the request helper
     * @return map of folders and monitors
     */
    public HashMap<String, List<Monitor>> initialize(AsmRequestHelper requestHelper) {
        HashMap<String, List<Monitor>> folderMap = null;
        boolean keepTrying = true;
        int initNumRetriesLeft = 10;

        while (keepTrying) {
            try {
                // connect
                requestHelper.connect();
                EpaUtils.getFeedback().info(AsmMessages.getMessage(
                    AsmMessages.CONNECTED_503, EpaUtils.getProperty(URL)));

                folderMap = readConfiguration();
                
                keepTrying = false;

            } catch (Exception e) {
                if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                        && (initNumRetriesLeft > 0)) {
                    initNumRetriesLeft = retryConnection(initNumRetriesLeft,
                        AsmMessages.getMessage(AsmMessages.AGENT_INITIALIZATION));
                } else {
                    EpaUtils.getFeedback().error(
                        AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR_900,
                            ASM_PRODUCT_NAME, e.getMessage()));
                    // e.printStackTrace();
                    keepTrying = false;
                    System.exit(1);
                }
            }
        }

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
     * @param filename property file name
     * @return the properties read
     * @throws IOException error reading the file
     */
    public static Properties readPropertiesFromFile(String filename) throws IOException {
        FileInputStream inStream = new FileInputStream(new File(filename));
        Properties properties = new Properties();

        try {
            properties.load(inStream);
        } catch (IOException e) {
            if (null != EpaUtils.getFeedback()) {
                EpaUtils.getFeedback().error(AsmMessages.getMessage(
                    AsmMessages.READING_PROPERTIES_ERROR_901, filename, e.getMessage()));
            }
            throw e;
        }
        inStream.close();
        AsmReader.setProperties(properties);

        if (EpaUtils.getFeedback().isVerboseEnabled()) {
            EpaUtils.getFeedback().verbose(AsmMessages.getMessage(
                AsmMessages.READING_PROPERTIES_300, filename));
            // use TreeSet so we get output alphabetically sorted
            Set<Object> copy = new TreeSet<Object>(properties.keySet());
            for (Iterator<Object> it = copy.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                EpaUtils.getFeedback().verbose(key + "=" + properties.getProperty(key));
            }
        }

        EpaUtils.getFeedback().info(AsmMessages.getMessage(
            AsmMessages.READING_PROPERTIES_FINISHED_500, filename));
        return properties;
    }
}
