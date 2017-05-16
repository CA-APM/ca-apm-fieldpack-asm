package com.ca.apm.swat.epaplugins.asm.har;

import java.util.Calendar;

public interface CacheState {
    @OptionalItem
    public Calendar getExpires();
    
    public Calendar getLastAccess();
    
    public String getETag();
    
    public int getHitCount();
    
    @OptionalItem
    public String comment();
}
