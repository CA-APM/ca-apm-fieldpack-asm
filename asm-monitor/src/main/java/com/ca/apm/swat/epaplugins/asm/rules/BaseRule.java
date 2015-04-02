package com.ca.apm.swat.epaplugins.asm.rules;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

public abstract class BaseRule implements Rule, AsmProperties {

    private String name = null;
    private String folder = null;
    private String[] tags = null;
    private String type = null;

    /**
     * Rule base class.
     * @param name name of the rule
     * @param folder folder of the rule
     * @param tags tags of the rule
     */
    protected BaseRule(String name, String folder, String[] tags) {
        this.name = name;
        this.folder = folder;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public String getFolder() {
        return folder;
    }

    public String[] getTags() {
        return tags;
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    /**
     * Compare the name of the rule with a string.
     * Needed to include/exclude rules by name.
     * @param anotherName string to compare rule name with
     * @return true if the rule name equals anotherName
     */
    public boolean equals(String anotherName) {
        EpaUtils.getFeedback().debug("equals(String s) called for Rule " + name + " with s = anotherName"); 
        return this.name.equals(anotherName);
    }

    /**
     * Compare the names of the rules.
     * @param anotherRule rule to compare with
     * @return true if the rule names are equal
     */
    public boolean equals(Rule anotherRule) {
        return this.name.equals(anotherRule.getName());
    }
}
