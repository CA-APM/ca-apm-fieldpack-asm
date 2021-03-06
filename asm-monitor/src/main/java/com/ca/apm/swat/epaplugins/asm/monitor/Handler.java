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
     * @return next Handler in chain.
     */
    public Handler getSuccessor();
    
    /**
     * Generate metrics from API call result
     * @param map Map to be populated by metrics
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @param API endpoint where the request came from
     * @return map containing the metrics
     * @throws AsmException error during metrics generation
     */
    public Map<String, String> generateMetrics(Map<String,String> map,
                                               String jsonString,
                                               String metricTree,
                                               String endpoint)
                                                       throws AsmException;
}
