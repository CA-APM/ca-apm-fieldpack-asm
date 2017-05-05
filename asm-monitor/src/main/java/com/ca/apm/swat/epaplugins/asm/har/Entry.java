package com.ca.apm.swat.epaplugins.asm.har;

import java.util.Calendar;

public interface Entry {
    @OptionalItem
    public String getPageref();
    public Calendar getStartedDateTime();
    public Integer getTime();
    public Request getRequest();
    public Response getResponse();
    public Cache getCache();
    public Timings getTimings();
    @OptionalItem
    public String getServerIPAddress();
    @OptionalItem
    public String getConnection();
    @OptionalItem
    public String getComment();    
}
