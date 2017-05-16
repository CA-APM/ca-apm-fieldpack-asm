package com.ca.apm.swat.epaplugins.asm.har;

import java.util.Calendar;

public interface Page {
    public Calendar getStartedDateTime();
    
    public String getId();
    
    public String getTitle();
    
    public PageTimings getPageTimings();
    
    @OptionalItem
    public String getComment();
    
    @OptionalItem
    @IterableClass(type = Assertion.class)
    public Iterable<Assertion> get_assertions();
}
