package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.wily.introscope.epagent.EpaUtils;

/**
 * Overrides the HashMap implementation to make sure no values are overwritten.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class MetricMap extends HashMap<String, String> {

    private static final long serialVersionUID = 2388439240326112312L;

    /**
     * Associates the specified value with the specified key in this map.
     *   If the map previously contained a mapping for the key, the old value is NOT replaced.
     */
    @Override
    public String put(String key, String value) {
        if (this.containsKey(key)) {
            if (EpaUtils.getFeedback().isDebugEnabled()) {
                EpaUtils.getFeedback().debug("not putting " + key + "=" + value
                    + " in map, original value =" + this.get(key));
            }
            // don't insert
            return null;
        }
        return super.put(key, value);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *   If the map previously contained a mapping for the key, the old value is NOT replaced.
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        for (Iterator<? extends String> it = map.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            if (!this.containsKey(key)) {
                super.put(key, map.get(key));
            } else {
                if (EpaUtils.getFeedback().isDebugEnabled()) {
                    EpaUtils.getFeedback().debug("not putting " + key + "=" + map.get(key)
                        + " in map, original value =" + this.get(key));
                }
            }
        }
    }
}
