package com.ca.apm.swat.epaplugins.asm.monitor;


/**
 * Interface for all monitors.
 * Monitor behaviour (generating metrics) is type dependent.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface Monitor extends Handler {

    /**
     * Returns the name of the monitor.
     * @return name of the monitor
     */
    public String getName();
    
    /**
     * Returns the type of the monitor.
     * @return type of the monitor
     */
    public String getType();
    
    /**
     * Returns the folder of the monitor.
     * @return folder of the monitor.
     *     An empty String is returned if the folder is the root folder.
     */
    public String getFolder();
    
    /**
     * Returns the tags of the monitor.
     * @return tags of the monitor
     */
    public String[] getTags();
}
