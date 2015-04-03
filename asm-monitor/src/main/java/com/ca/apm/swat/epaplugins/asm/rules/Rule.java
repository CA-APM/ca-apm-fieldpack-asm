package com.ca.apm.swat.epaplugins.asm.rules;

import java.util.HashMap;
import java.util.Properties;

/**
 * Interface for all rules.
 * Rule behaviour (generating metrics) is type dependent.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public interface Rule {

    /**
     * Returns the name of the rule.
     * @return name of the rule
     */
    public String getName();
    
    /**
     * Returns the type of the rule.
     * @return type of the rule
     */
    public String getType();
    
    /**
     * Returns the folder of the rule.
     * @return folder of the rule.
     *     An empty String is returned if the folder is the root folder.
     */
    public String getFolder();
    
    /**
     * Returns the tags of the rule.
     * @return tags of the rule
     */
    public String[] getTags();

    /**
     * Generate metrics from API call result. 
     * @param jsonString API call result.
     * @param metricTree metric tree prefix
     * @param properties plugin properties that control output format and filtering
     * @param checkpointMap map containing all checkpoints of App Synthetic Monitor
     * @return metricMap map containing the metrics
     */
    public HashMap<String, String> generateMetrics(String jsonString,
        String metricTree,
        Properties properties,
        HashMap<String, String> checkpointMap);
}
