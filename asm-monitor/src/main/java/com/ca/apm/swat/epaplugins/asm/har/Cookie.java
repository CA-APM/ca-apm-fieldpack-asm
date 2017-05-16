package com.ca.apm.swat.epaplugins.asm.har;

import java.util.Calendar;

public interface Cookie {
    public String getName();
    
    public String getValue();
    
    @OptionalItem
    public String getPath();
    
    @OptionalItem
    public Calendar getExpires();
    
    @OptionalItem
    public Boolean getHttpOnly();
    
    @OptionalItem
    public Boolean getSecure();
    
    @OptionalItem
    public String getComment();
}
