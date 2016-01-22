package com.ca.apm.swat.epaplugins.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.asm.error.InitializationError;
import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricNameFilter;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.TextMetricWriter;
import com.ca.apm.swat.epaplugins.asm.reporting.XmlMetricWriter;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.ErrorUtils;
import com.ca.apm.swat.epaplugins.utils.FileWatcher;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;


/**
 * Main thread for App Synthetic Monitor EPA plugin.
 */
public class AsmReader implements AsmProperties {

    private static final long DEFAULT_UPDATE_INTERVAL = 60L; // 60 minutes

    private boolean keepRunning;
    private int numRetriesLeft;
    private long configUpdateInterval = DEFAULT_UPDATE_INTERVAL * 60000;
    private long lastConfigUpdateTimestamp = 0;
    private AsmRequestHelper requestHelper = null;
    private MetricWriter metricWriter = null;
    private ExecutorService reporterService = null;
    private ScheduledExecutorService folderService = null;
    private HashMap<String, List<Monitor>> folderMap = null;
    private static Module module = new Module("Asm.MainThread");

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
            Thread.currentThread().setName(module.getName());

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
            int retries = 3;
            do {
                try {
                    if (null == propertyFileName) {
                        throw new FileNotFoundException();
                    }

                    properties = readPropertiesFromFile(propertyFileName);

                } catch (FileNotFoundException e) {
                    String oldPropertyFileName = propertyFileName;
                    // retry with default names
                    if (3 == retries) {
                        propertyFileName = System.getProperty(EpaUtils.PREFERENCES_KEY);
                    } else if (2 == retries) {
                        propertyFileName = PROPERTY_FILE_DIR + '/' + PROPERTY_FILE_NAME;
                    } else {
                        propertyFileName = PROPERTY_FILE_NAME;
                    }
                    --retries;
                    String msg = "property file '" + oldPropertyFileName
                            + "' not found, retrying with '" + propertyFileName + "'";
                    if (null != EpaUtils.getFeedback()) {
                        EpaUtils.getFeedback().verbose(module, msg);
                    } else {
                        System.err.println(msg);
                    }
                }
            } while ((null == properties) && (0 < retries));

            if (null == properties) {
                throw new InitializationError(AsmMessages.PROPERTY_FILE_NOT_FOUND_921);
            }

            String locale = properties.getProperty(LOCALE, DEFAULT_LOCALE);
            AsmMessages.setLocale(new Locale(locale.substring(0, 2),
                locale.substring(3,5)));

            AsmReader thisReader = AsmReader.getInstance();
            int epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = getMetricWriter(psEpa);

            // start main loop
            thisReader.work(epaWaitTime, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(module,
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR_900,
                    ASM_PRODUCT_NAME, e.getMessage()));
            EpaUtils.getFeedback().error(module, ErrorUtils.getStackTrace(e));
            System.exit(1);
        } catch (Error e) {
            EpaUtils.getFeedback().error(module,
                "Fatal error " + e.getMessage() + " - exiting!!!");
            System.exit(1);
        }

    }

    /**
     * Called when testing.
     * @param args arguments
     */
    public static void main(String[] args) {

        try {
            Thread.currentThread().setName(module.getName());

            propertyFileName = (args.length != 0) ? args[0] : PROPERTY_FILE_NAME;
            Properties properties = readPropertiesFromFile(propertyFileName);

            AsmReader reader = AsmReader.getInstance();
            int epaWaitTime = Integer.parseInt(properties.getProperty(WAIT_TIME));

            MetricWriter metricWriter = new TextMetricWriter();
            reader.work(epaWaitTime, metricWriter);

        } catch (Exception e) {
            EpaUtils.getFeedback().error(module,
                AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR_900,
                    ASM_PRODUCT_NAME, e.getMessage()));
            EpaUtils.getFeedback().error(module, ErrorUtils.getStackTrace(e));
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
                EpaUtils.getFeedback().warn(module,
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
        this.metricWriter = metricWriter;

        reporterService = Executors.newSingleThreadExecutor();

        this.keepRunning = true;
        this.numRetriesLeft = 10;

        Date now = new Date();
        lastConfigUpdateTimestamp = now.getTime();

        // connect and read folders, monitors and monitoring stations.
        folderMap = initialize(requestHelper);
        startThreads(folderMap);

        // watch configuration file
        startFileWatcher();

        while (keepRunning) {
            try {

                // get credits
                if (EpaUtils.getBooleanProperty(METRICS_CREDITS, false)) {
                    HashMap<String, String> creditsMap = requestHelper.getCredits();
                    reporterService.execute(new AsmMetricReporter(metricWriter, creditsMap));
                    creditsMap = null;
                }

                // print API stats
                requestHelper.printApiCallStatistics();

                // check if we need to reread the configuration
                now = new Date();
                long timeElapsed = now.getTime() - lastConfigUpdateTimestamp;
                if ((configUpdateInterval > 0) && (configUpdateInterval < timeElapsed)) {
                    lastConfigUpdateTimestamp = now.getTime();

                    stopThreads();
                    folderMap = readConfiguration();
                    startThreads(folderMap);
                }

                // print our threads
                printThreads();
                Thread.sleep(epaWaitTime);
            } catch (Exception e) {
                if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                        && (numRetriesLeft > 0)) {
                    numRetriesLeft = retryConnection(numRetriesLeft,
                        AsmMessages.getMessage(AsmMessages.PARENT_THREAD));
                } else if (e instanceof InterruptedException) {
                    // ignore, the config file has changed
                    EpaUtils.getFeedback().verbose(module,
                        e.getMessage() == null ? e.toString() : e.getMessage());
                    StackTraceElement[] ste = Thread.currentThread().getStackTrace();
                    if (null != ste) {
                        for (int i = 0; i < ste.length; ++i) {
                            EpaUtils.getFeedback().verbose(module, "  " + ste[i]);
                        }
                    }
                } else if (e instanceof AsmException) {
                    EpaUtils.getFeedback().warn(module, e.getMessage());
                } else {
                    EpaUtils.getFeedback().error(module,
                        AsmMessages.getMessage(AsmMessages.RUN_ERROR_904,
                            ASM_PRODUCT_NAME,
                            AsmMessages.PARENT_THREAD,
                            e.getMessage()));
                    EpaUtils.getFeedback().error(module, "FATAL ERROR IN work(): ", e);
                    e.printStackTrace();
                    keepRunning = Boolean.valueOf(false);
                    System.exit(2);
                }
            }
        }
    }

    public static Comparator<Thread> ThreadIdComparator = new Comparator<Thread>() {
        public int compare(Thread thread1, Thread thread2) {
            //ascending order
            return ((int) thread1.getId()) - ((int) thread2.getId());
        }
    };

    private void printThreads() {
        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            Map<Thread,StackTraceElement[]> map = Thread.getAllStackTraces();
            EpaUtils.getFeedback().verbose(module, "There are " + map.size() + " threads");

            Thread[] threads = map.keySet().toArray(new Thread[map.size()]);
            Arrays.sort(threads, ThreadIdComparator);

            for (int j = 0; j < threads.length; ++j) {
                Thread th = threads[j];
                EpaUtils.getFeedback().verbose(module, "  thread " + th.getId() + " = "
                        + th.getName());

                if (th.getName().contains("Asm")) {
                    StackTraceElement[] ste = map.get(th);
                    if (null != ste) {
                        for (int i = 0; i < ste.length; ++i) {
                            EpaUtils.getFeedback().verbose(module, "    " + ste[i]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Start reader threads for folders.
     * @param folderMap map of the folders
     */
    private void startThreads(HashMap<String,
        List<Monitor>> folderMap) {

        folderService = Executors.newScheduledThreadPool(Integer.parseInt(
            EpaUtils.getProperty(FOLDER_THREADS, "10")));

        int epaWaitTime = Integer.parseInt(EpaUtils.getProperty(WAIT_TIME));

        for (Iterator<String> it = folderMap.keySet().iterator(); it.hasNext(); ) {
            String folder = it.next();
            AsmReaderThread rt = new AsmReaderThread(
                folder,
                requestHelper,
                folderMap,
                metricWriter,
                reporterService);
            folderService.scheduleAtFixedRate(rt, 0, epaWaitTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Start file watch task for configuration file.
     */
    private void startFileWatcher() {
        // create task to watch for property file changes
        TimerTask fileWatchTask = new FileWatcher(new File(AsmReader.propertyFileName)) {
            protected void onChange(File file) {
                // here we code the action on a change
                EpaUtils.getFeedback().info(module, AsmMessages.getMessage(
                    AsmMessages.PROPERTY_FILE_CHANGED_506, file.getPath()));

                try {
                    // print our threads
                    printThreads();

                    stopThreads();
                    AsmReader.setProperties(readPropertiesFromFile(file.getPath()));
                    AsmReader.getInstance().folderMap = readConfiguration();
                    startThreads(AsmReader.getInstance().folderMap);

                    // print our threads
                    printThreads();
                } catch (Exception e) {
                    if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                            && (numRetriesLeft > 0)) {
                        numRetriesLeft = retryConnection(numRetriesLeft,
                            AsmMessages.getMessage(AsmMessages.PARENT_THREAD));
                    } else {
                        EpaUtils.getFeedback().error(module,
                            AsmMessages.getMessage(AsmMessages.RUN_ERROR_904,
                                ASM_PRODUCT_NAME,
                                AsmMessages.PARENT_THREAD,
                                e.getMessage() == null ? e.toString() : e.getMessage() ));
                        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            PrintStream stream = new PrintStream(out);
                            e.printStackTrace(stream);
                            EpaUtils.getFeedback().verbose(module, out.toString());
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

        EpaUtils.getFeedback().info(module, AsmMessages.getMessage(
            AsmMessages.CONFIG_POLLING_STARTED_507, AsmReader.propertyFileName, 60));
    }

    /**
     * Stop reader threads for folders.
     */
    private void stopThreads() {

        // tell threads to stop
        EpaUtils.getFeedback().verbose(module, "stopping folder threads");
        folderService.shutdown();

        // wait for threads to stop
        try {
            if (!folderService.awaitTermination(5, TimeUnit.SECONDS)) {
                EpaUtils.getFeedback().warn(module, "not all folder threads have stopped");
            }
        } catch (InterruptedException e) {
            EpaUtils.getFeedback().warn(module, "interrupted while stopping folder threads");
        }
        EpaUtils.getFeedback().verbose(module, "exiting stopThread()");
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

        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            StringBuffer buf = new StringBuffer("read folders: ");
            for (int i = 0; i < folders.length; ++i) {
                buf.append(folders[i] + ", ");
            }
            EpaUtils.getFeedback().verbose(module, buf.toString());
        }

        // read monitors
        folderMap = requestHelper.getMonitors(folders);

        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module, "read monitors: ");
            Set<Object> copy = new TreeSet<Object>(folderMap.keySet());
            for (Iterator<Object> fit = copy.iterator(); fit.hasNext(); ) {
                String folder = (String) fit.next();
                StringBuffer buf = new StringBuffer("  " + folder + " = ");
                List<Monitor> monitors = folderMap.get(folder);
                for (Iterator<Monitor> mit = monitors.iterator(); mit.hasNext(); ) {
                    buf.append(mit.next().getName() + ", ");
                }
                EpaUtils.getFeedback().verbose(module, buf.toString());
            }
        }

        // read monitoring stations
        requestHelper.getMonitoringStations();
        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module, "read monitoring stations");
        }

        EpaUtils.getFeedback().info(module, AsmMessages.getMessage(
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
        EpaUtils.getFeedback().error(module,
            AsmMessages.getMessage(AsmMessages.CONNECTION_ERROR_902,
                ASM_PRODUCT_NAME, apmcmInfo));
        if (numRetriesLeft > 0) {
            EpaUtils.getFeedback().info(module,
                AsmMessages.getMessage(AsmMessages.CONNECTION_RETRY_501,
                    numRetriesLeft));
            numRetriesLeft--;
            try {
                Thread.sleep(60000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EpaUtils.getFeedback().error(module,
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
                EpaUtils.getFeedback().info(module, AsmMessages.getMessage(
                    AsmMessages.CONNECTED_503, EpaUtils.getProperty(URL)));

                folderMap = readConfiguration();

                keepTrying = false;

            } catch (Exception e) {
                if ((e.toString().matches(JAVA_NET_EXCEPTION_REGEX))
                        && (initNumRetriesLeft > 0)) {
                    initNumRetriesLeft = retryConnection(initNumRetriesLeft,
                        AsmMessages.getMessage(AsmMessages.AGENT_INITIALIZATION));
                } else {
                    EpaUtils.getFeedback().error(module,
                        AsmMessages.getMessage(AsmMessages.INITIALIZATION_ERROR_900,
                            ASM_PRODUCT_NAME, e.getMessage()));
                    EpaUtils.getFeedback().error(module, ErrorUtils.getStackTrace(e));
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
                EpaUtils.getFeedback().error(module, AsmMessages.getMessage(
                    AsmMessages.READING_PROPERTIES_ERROR_901, filename, e.getMessage()));
            }
            throw e;
        }
        inStream.close();
        AsmReader.setProperties(properties);

        Module module = new Module(Thread.currentThread().getName());
        if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
            EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
                AsmMessages.READING_PROPERTIES_300, filename));
            // use TreeSet so we get output alphabetically sorted
            Set<Object> copy = new TreeSet<Object>(properties.keySet());
            for (Iterator<Object> it = copy.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                EpaUtils.getFeedback().verbose(module, key + "=" + properties.getProperty(key));
            }
        }

        EpaUtils.getFeedback().info(module, AsmMessages.getMessage(
            AsmMessages.READING_PROPERTIES_FINISHED_500, filename));
        return properties;
    }
}
