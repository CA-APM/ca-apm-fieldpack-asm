package com.ca.apm.swat.epaplugins.asm.rules;


public class ScriptRule extends BaseRule {

    
    protected ScriptRule(String name, String folder, String[] tags) {
        super(name, folder, tags);
        setType(SCRIPT_RULE);
    }
}
