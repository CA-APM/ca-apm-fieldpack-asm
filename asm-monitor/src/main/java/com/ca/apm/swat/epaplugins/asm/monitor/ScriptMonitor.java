package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;



/**
 * {@link Monitor} implementation for script monitors.
 * A ScriptMonitor generates additional metrics per JMeter step.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class ScriptMonitor extends BaseMonitor {
    private static final Handler DECODER;
    
    static {
        // build chain of responsibility
        Handler jmeterHandler = new JMeterScriptHandler();                
        Handler fixer = new XmlFixer(jmeterHandler);
        Handler downloader;
        if (EpaUtils.getBooleanProperty(AsmProperties.FIX_AMPERSAND, true)) {
            downloader = new AssetDownloader(fixer, "jtl", "url");
        } else {
            downloader = new AssetDownloader(jmeterHandler, "jtl", "url");
        }                
        DECODER = new InflatingBase64Decoder(downloader);
    }

    /**
     * Create a new script monitor.
     * @param name name of the monitor
     * @param folder folder of the monitor
     * @param tags tags of the monitor
     * @param url URL
     * @param active is active?
     */
    protected ScriptMonitor(String name,
                            String folder,
                            String[] tags,
                            String url,
                            boolean active) {
        super(DECODER, name, SCRIPT_MONITOR, folder, tags, url, active);
    }
}
