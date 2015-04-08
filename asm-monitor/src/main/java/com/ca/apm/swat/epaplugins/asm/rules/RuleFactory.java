package com.ca.apm.swat.epaplugins.asm.rules;

import com.ca.apm.swat.epaplugins.utils.AsmProperties;

/**
 * Factory for creating {@link Rule}s with type dependent behaviour.
 * Every implementation of Rule may generate different metrics.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class RuleFactory implements AsmProperties {

    private static Rule ALL_RULES_RULE = null;

    /**
     * Factory method returning a {@link Rule} based on its type.
     * @param name rule name
     * @param type rule type, e.g. http, script, ...
     * @param folder rule folder
     * @param tags list of tags
     * @return a new <code>Rule</code> object
     */
    public static Rule getRule(String name, String type, String folder, String[] tags) {
        if (type == null) {
            return null;
        }     
        if (type.equalsIgnoreCase(SCRIPT_RULE)
                /*|| type.equalsIgnoreCase(RBM_RULE)
                || type.equalsIgnoreCase(BROWSER_RULE)*/) {
            return new ScriptRule(name, folder, tags);
        }

        return new BaseRule(name, type, folder, tags);
    }

    /**
     * Factory method returning the ALL_RULES {@link Rule}.
     * @return the ALL_RULES object
     */
    public static Rule getAllRulesRule() {
        if (null == ALL_RULES_RULE) {
            ALL_RULES_RULE =
                    new BaseRule(ALL_RULES, ALL_RULES, EMPTY_STRING, EMPTY_STRING_ARRAY);
        }
        return ALL_RULES_RULE;
    }
}