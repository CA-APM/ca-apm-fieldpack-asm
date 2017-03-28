package com.ca.apm.swat.epaplugins.asm.reporting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;

/**
 * Overrides the HashMap implementation to add logging.
 * 
 *
 */
public class MetricMapReplacing extends HashMap<String, String> {

    private static final long serialVersionUID = 2388439240326112312L;
    private static Module module = new Module("Asm.reporting.MetricMapReplacing");
    
    /**
     * Associates the specified value with the specified key in this map.
     *   If the map previously contained a mapping for the key, the old value is replaced.
     */
    @Override
    public String put(String metricPath, String value) {        
        if (this.containsKey(metricPath)) {
            if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                EpaUtils.getFeedback().debug(module, AsmMessages.getMessage(
                    AsmMessages.DUPLICATE_METRIC_REPLACED_201,
                    metricPath,
                    value,
                    this.get(metricPath)));
            }
        }                    
        return super.put(metricPath, value);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *   If the map previously contained a mapping for the key, the old value is replaced.
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        for (Iterator<? extends String> it = map.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            if (this.containsKey(key)) {
                if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                    EpaUtils.getFeedback().debug(module, AsmMessages.getMessage(
                        AsmMessages.DUPLICATE_METRIC_REPLACED_201,
                        key,
                        map.get(key),
                        this.get(key)));
                }
            }
        }
        super.putAll(map);
    }
}
