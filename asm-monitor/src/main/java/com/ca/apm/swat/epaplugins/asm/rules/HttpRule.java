package com.ca.apm.swat.epaplugins.asm.rules;


public class HttpRule extends BaseRule {

    protected HttpRule(String name, String type, String folder, String[] tags) {
        super(name, folder, tags);
        setType(type);
    }

}
