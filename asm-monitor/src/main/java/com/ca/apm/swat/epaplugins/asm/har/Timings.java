package com.ca.apm.swat.epaplugins.asm.har;

public interface Timings {
    @OptionalItem
    public Integer getBlocked();
    
    @OptionalItem
    public Integer getDns();
    
    @OptionalItem
    public Integer getConnect();
    
    public int getSend();
    
    public int getWait();
    
    public int getReceive();
    
    @OptionalItem
    public int getSsl();
    
    @OptionalItem
    public String getComment();
}
