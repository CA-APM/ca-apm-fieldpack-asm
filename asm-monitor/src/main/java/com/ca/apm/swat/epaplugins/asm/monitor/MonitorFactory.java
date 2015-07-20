package com.ca.apm.swat.epaplugins.asm.monitor;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;

/**
 * Factory for creating {@link Monitor}s with type dependent behaviour.
 * Every implementation of Monitor may generate different metrics.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class MonitorFactory implements AsmProperties {

    private static Monitor ALL_MONITORS_MONITOR = null;

    private static Map<String, Monitor> monitorMap = null;

    /**
     * Factory method returning a {@link Monitor} based on its type.
     * @param name monitor name
     * @param type monitor type, e.g. http, script, ...
     * @param folder monitor folder
     * @param tags list of tags
     * @return a new <code>Monitor</code> object
     */
    public static Monitor createMonitor(String name,
                                        String type,
                                        String folder,
                                        String[] tags,
                                        boolean active) {
        if (type == null) {
            return null;
        }

        Monitor monitor = null;
        // TODO make configurable
        if (type.equalsIgnoreCase(SCRIPT_MONITOR)
                /* don't handle here until we figured out what to do with har data
                || type.equalsIgnoreCase(REAL_BROWSER_MONITOR)
                || type.equalsIgnoreCase(FULL_PAGE_MONITOR)*/) {
            monitor = new ScriptMonitor(name, folder, tags, active);
        } else {
            monitor = new BaseMonitor(name, type, folder, tags, active);
        }
     
        if (null == monitorMap) {
            monitorMap = new HashMap<String, Monitor>();
        }
        monitorMap.put(name, monitor);
        
        return monitor;
    }

    /**
     * Factory method returning the ALL_MONITORS {@link Monitor}.
     * @return the ALL_MONITORS object
     */
    public static Monitor getAllMonitorsMonitor() {
        if (null == ALL_MONITORS_MONITOR) {
            ALL_MONITORS_MONITOR =
                    new BaseMonitor(ALL_MONITORS,
                        ALL_MONITORS,
                        EMPTY_STRING,
                        EMPTY_STRING_ARRAY,
                        false);
        }
        return ALL_MONITORS_MONITOR;
    }
    
    /**
     * Find a monitor by its name.
     * @param name name of the monitor
     * @return the monitor or null if no monitor with that name was found.
     */
    public static Monitor findMonitor(String name) {
        if (null == monitorMap) {
            return null;
        }
        return monitorMap.get(name); 
    }
  
}