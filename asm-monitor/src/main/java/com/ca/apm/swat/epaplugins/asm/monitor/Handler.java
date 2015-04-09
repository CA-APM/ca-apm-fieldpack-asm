package com.ca.apm.swat.epaplugins.asm.monitor;

import java.util.HashMap;

/**
 * Handler interface for generating metrics from a JSON string.
 * Implements the Chain of Responsibility design pattern.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface Handler {
    
    /**
     * Add a Handler to the chain of responsibility.
     * @param successor next Handler in chain
     */
    public void setSuccessor(Handler successor);
    
    /**
     * Generate metrics from API call result. 
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @return map containing the metrics
     */
    public HashMap<String, String> generateMetrics(String jsonString, String metricTree);
}
