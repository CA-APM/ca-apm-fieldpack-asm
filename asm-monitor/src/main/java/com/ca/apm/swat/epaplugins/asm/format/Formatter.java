package com.ca.apm.swat.epaplugins.asm.format;

import java.text.NumberFormat;
import java.util.Properties;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;

/**
 * Formatter formats the output.
 *   Call setProperties() before getInstance()!
 *   Implemented using the singleton pattern.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class Formatter implements AsmProperties {

    private static Properties properties = null;
    private static Formatter  instance = null;
    
    private NumberFormat stepNumberFormat = null;
    private String stepPrefix = EMPTY_STRING;
    private boolean printStepUrl = true;
    
    /**
     * Create a new Formatter.
     */
    private Formatter() {
        // TODO use properties
        stepNumberFormat = NumberFormat.getIntegerInstance();
        stepNumberFormat.setMinimumIntegerDigits(3);
    }

    /**
     * Set the properties used for formatting.
     * @param properties the properties
     */
    public static void setProperties(Properties properties) {
        Formatter.properties = properties;
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
        buf.append(stepNumberFormat.format(step));
        if (printStepUrl) {
            buf.append(BLANK).append(url);
        }
        
        return buf.toString();
    }
    
    /**
     * Map status values.
     * @param responsecode input value
     * @return mapped output status code
     */
    public int mapResponseToStatusCode(int responsecode) {
        // TODO: map status values according to configuration
        int statusCode = 0;
        switch (responsecode) {
          case 403 :
              statusCode = 401;
              break;

          case 404 :
              statusCode = 404;
              break;

          case 500 :
              statusCode = 500;
              break;

          case 503 :
              statusCode = 500;
              break;

          default:
              break;
        }
        return statusCode;
    }


}
