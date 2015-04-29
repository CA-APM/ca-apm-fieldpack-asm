package com.ca.apm.swat.epaplugins.asm.format;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Formatter formats the output.
 *   Call setProperties() before getInstance()!
 *   Implemented using the singleton pattern.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class Formatter implements AsmProperties {

    private static Formatter instance = null;
    
    private HashMap<String, String> responseCodeMap = null;
    private HashSet<String> ignoreTags = null;
    private HashSet<String> ignoreTagsMonitor = null;
    private HashSet<Integer> suppressStepResponseCodes = null;
    
    private NumberFormat stepNumberFormat = null;
    private String stepPrefix = EMPTY_STRING;
    private boolean printStepUrl = true;
    
    /**
     * Create a new Formatter.
     */
    private Formatter() {
        stepNumberFormat = NumberFormat.getIntegerInstance();
        stepNumberFormat.setMinimumIntegerDigits(3);
    }

    /**
     * Set the properties used for formatting and initialize internal data structures.
     * @param properties the properties
     */
    public static void setProperties(Properties properties) {
        getInstance().createResponseCodeMappings(properties);
        getInstance().createIgnoreTags(properties);
        getInstance().createSuppressStepsWithCodes(properties);
        getInstance().setStepConfiguration(properties);
    }

    private void setStepConfiguration(Properties properties) {
        // set number of digits in step number
        String digits = properties.getProperty(STEP_FORMAT_DIGITS);
        if ((null != digits) && (!EMPTY_STRING.equals(digits))) {
            try {
                int digit = Integer.parseInt(digits);
                this.stepNumberFormat.setMinimumIntegerDigits(digit);
            } catch (NumberFormatException e) {
                EpaUtils.getFeedback().warn("non-integer value found in "
                        + STEP_FORMAT_DIGITS + ": " + digits);
            }  
        }

        // set step prefix
        String prefix = properties.getProperty(STEP_FORMAT_PREFIX, EMPTY_STRING);
        if (null != digits) {
            setStepPrefix(prefix);
        }

        // print URL in step?
        String printUrl = properties.getProperty(STEP_FORMAT_URL, TRUE);
        if (null != printUrl) {
            if (TRUE.equalsIgnoreCase(printUrl)) {
                setPrintStepUrl(true);
            } else {
                setPrintStepUrl(false);
            }
        }
    }

    /**
     * Creates a lookup map for JMeter response codes to ignore from the properties.
     * @param properties settings
     */
    private void createSuppressStepsWithCodes(Properties properties) {
        this.suppressStepResponseCodes = new HashSet<Integer>();

        String tag = properties.getProperty(SUPPRESS_STEP_WITH_CODES, EMPTY_STRING);

        if (EMPTY_STRING.equals(tag)) {
            return;
        }
        
        String[] tags = tag.split(",");
        
        // add to set
        for (int i = 0; i < tags.length;  ++i) {
            try {
                int responseCode = Integer.parseInt(tags[i]);
                this.suppressStepResponseCodes.add(new Integer(responseCode));
            } catch (NumberFormatException e) {
                EpaUtils.getFeedback().warn("non-integer value found in "
                        + SUPPRESS_STEP_WITH_CODES + ": " + tags[i]);
            }
        }
    }

    /**
     * Creates a lookup map for tags to ignore from the properties.
     * 
     * @param properties settings
     */
    private void createIgnoreTags(Properties properties) {
        this.ignoreTags = new HashSet<String>();
        this.ignoreTagsMonitor = new HashSet<String>();

        String tag = properties.getProperty(IGNORE_TAGS, EMPTY_STRING);

        if (EMPTY_STRING.equals(tag)) {
            return;
        }
        
        String[] tags = tag.split(",");
        
        // add to set
        for (int i = 0; i < tags.length;  ++i) {
            this.ignoreTags.add(tags[i]);
            // also ignore for monitors
            this.ignoreTagsMonitor.add(tags[i]);
        }

        // do again for monitors
        tag = properties.getProperty(IGNORE_TAGS_MONITOR, EMPTY_STRING);

        if (EMPTY_STRING.equals(tag)) {
            return;
        }
        
        tags = tag.split(",");
        
        for (int i = 0; i < tags.length;  ++i) {
            this.ignoreTagsMonitor.add(tags[i]);
        }
    }

    /**
     * Create response code mappings.
     *   Mapping is from right to left so asm.responseCodes.404=6404,7001
     *   means both 6404 and 7001 will be mapped to 404!
     * @param properties the properties
     */
    private void createResponseCodeMappings(Properties properties) {
        this.responseCodeMap = new HashMap<String, String>();

        String responseCode = properties.getProperty(RESPONSE_CODES, EMPTY_STRING);
        
        if (EMPTY_STRING.equals(responseCode)) {
            return;
        }
        
        String[] codes = responseCode.split(",");
        
        for (int i = 0; i < codes.length;  ++i) {

            // make sure it's an integer
            try {
                Integer.parseInt(codes[i]);
            } catch (NumberFormatException e) {
                EpaUtils.getFeedback().warn("error in " + RESPONSE_CODES + ": "
                        + codes[i] + " is not an integer! mapping will be ignored");
                continue;
            }
            
            String map = properties.getProperty(RESPONSE_CODES + "." + codes[i], EMPTY_STRING);

            if (!EMPTY_STRING.equals(map)) {
                String[] mappings = map.split(",");

                for (int j = 0; j < mappings.length;  ++j) {
                                        
                    // make sure it's an integer
                    try {
                        Integer.parseInt(mappings[j]);
                    } catch (NumberFormatException e) {
                        EpaUtils.getFeedback().warn("error in " + RESPONSE_CODES + ": "
                                + mappings[j] + " is not an integer! mapping will be ignored");
                        continue;
                    }

                    // put entry into map
                    this.responseCodeMap.put(mappings[j], codes[i]);
                    
                    if (EpaUtils.getFeedback().isDebugEnabled()) {
                        EpaUtils.getFeedback().debug("response code mapping: "
                                + mappings[j] + " -> " + codes[i]);
                    }
                }
            }
        }
    }

    /**
     * Get the single instance. Call setProperties() first!
     * @return the one and only Formatter instance
     */
    public static Formatter getInstance() {
        if (null == instance) {
            instance = new Formatter();
        }
        return instance;
    }

    /**
     * Set a {@link NumberFormat} to format the step number.
     * @param stepNumberFormat numbe format
     */
    public void setStepNumberFormat(NumberFormat stepNumberFormat) {
        this.stepNumberFormat = stepNumberFormat;
    }

    /**
     * Set a prefix for the step, e.g. "Step " or empty string.
     * @param stepPrefix the prefix
     */
    public void setStepPrefix(String stepPrefix)  {
        this.stepPrefix = stepPrefix;
    }
    
    /**
     * Decide whether to include the URL in the step metric name or not.
     * @param printStepUrl true - include URL, false - not
     */
    public void setPrintStepUrl(boolean printStepUrl) {
        this.printStepUrl = printStepUrl;
    }

    /**
     * Generates a step name from the input.
     *    Depending on the settings of Formatter the output for (1, "index.html") could be
     *    "Step 1 /index.html" or "001 /index.html" or "Step 001".
     * @param step number of the step
     * @param url URL of the step
     * @return step name
     */
    public String formatStep(int step, String url) {
        StringBuffer buf = new StringBuffer(stepPrefix);
        
        if ((null != stepPrefix) && (stepPrefix.length() > 0)) {
            buf.append(BLANK);
        }
        
        buf.append(stepNumberFormat.format(step));
        
        if (printStepUrl && (null != url) && (url.length() > 0)) {
            buf.append(BLANK).append(url);
        }
        
        return buf.toString();
    }
    
    /**
     * Map status values.
     * @param responseCode input value
     * @return mapped output status code
     */
    public int mapResponseToStatusCode(int responseCode) {
        return Integer.parseInt(mapResponseToStatusCode(Integer.toString(responseCode)));
    }

    /**
     * Map status values.
     * @param responseCode input value
     * @return mapped output status code
     */
    public String mapResponseToStatusCode(String responseCode) {
        // try to find in map
        if ((null != responseCodeMap) && (responseCodeMap.containsKey(responseCode))) {
            return responseCodeMap.get(responseCode);
        }
        
        return responseCode;
    }

    /**
     * Ignore a tag? I.e. don't generate a metric for it.
     * @param tag tag to test
     * @return true if it should be ignored
     */
    public boolean ignoreTag(String tag) {
        return ignoreTags.contains(tag);
    }

    /**
     * Ignore a tag for a monitor metric? I.e. don't generate a metric for it.
     * @param tag tag to test
     * @return true if it should be ignored
     */
    public boolean ignoreTagForMonitor(String tag) {
        return ignoreTagsMonitor.contains(tag);
    }
    
    /**
     * Ignore a response code for a JMeter step? I.e. don't generate a metric for it.
     * @param responseCode response code to test
     * @return true if it should be suppressed
     */
    public boolean suppressResponseCode(int responseCode) {
        return suppressStepResponseCodes.contains(new Integer(responseCode));
    }
}
