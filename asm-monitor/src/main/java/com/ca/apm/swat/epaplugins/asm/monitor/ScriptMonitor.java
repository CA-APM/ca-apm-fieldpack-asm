package com.ca.apm.swat.epaplugins.asm.monitor;



/**
 * {@link Monitor} implementation for script monitors.
 * A ScriptMonitor generates additional metrics per JMeter step.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class ScriptMonitor extends BaseMonitor {

    /**
     * Create a new script monitor.
     * @param name name of the monitor
     * @param folder folder of the monitor
     * @param tags tags of the monitor
     */
    protected ScriptMonitor(String name,
                            String folder,
                            String[] tags,
                            String url,
                            boolean active) {
        super(name, SCRIPT_MONITOR, folder, tags, url, active);

        // build chain of responsibility
        Handler jmeterHandler = new JMeterScriptHandler();
        Handler decoder = new InflatingBase64Decoder();
        decoder.setSuccessor(jmeterHandler);
        setSuccessor(decoder);
    }
}
