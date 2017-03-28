package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import java.util.Map;

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
     * Generate metrics from API call result
     * @param map Map to be populated by metrics
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @return map containing the metrics
     * @throws AsmException error during metrics generation
     */
    public Map<String, String> generateMetrics(Map<String,String> map,
                                               String jsonString,
                                               String metricTree)
                                                       throws AsmException;
}
