package com.ca.apm.swat.epaplugins.asm.har;

public interface Cache {
    @OptionalItem
    public CacheState getBeforeRequest();
    
    @OptionalItem
    public CacheState getAfterRequest();
    
    @OptionalItem
    public String getComment();
}
