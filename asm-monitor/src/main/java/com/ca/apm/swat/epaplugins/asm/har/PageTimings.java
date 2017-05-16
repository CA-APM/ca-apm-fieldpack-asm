package com.ca.apm.swat.epaplugins.asm.har;

public interface PageTimings {
    @OptionalItem
    public Integer getOnContentLoad();
    
    @OptionalItem
    public Integer getOnLoad();
    
    @OptionalItem
    public String getComment();
}
