package com.ca.apm.swat.epaplugins.asm.har;

/**
 * An extension for Real Browser Monitor.
 */
public interface Assertion {
    public String getType();
    
    public String getName();
    
    public String getMessage();
    
    public boolean getError();
}
