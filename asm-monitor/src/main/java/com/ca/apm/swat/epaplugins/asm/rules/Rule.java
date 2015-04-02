package com.ca.apm.swat.epaplugins.asm.rules;

/**
 * Interface for all rules.
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
     * An empty String is returned if the folder is the root folder.
     */
    public String getFolder();
    
    /**
     * Returns the tags of the rule.
     * @return tags of the rule
     */
    public String[] getTags();
}
