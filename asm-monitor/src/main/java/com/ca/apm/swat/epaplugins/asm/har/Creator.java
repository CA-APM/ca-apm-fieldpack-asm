package com.ca.apm.swat.epaplugins.asm.har;

public interface Creator {
    public String getName();
    
    public String getVersion();
    
    @OptionalItem
    public String getComment();
}
