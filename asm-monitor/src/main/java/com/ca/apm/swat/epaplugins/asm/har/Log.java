package com.ca.apm.swat.epaplugins.asm.har;

public interface Log {
    public String getVersion();
    
    public Creator getCreator();
    
    @OptionalItem
    public Browser getBrowser();
    
    @IterableClass(type = Page.class)
    @OptionalItem
    public Iterable<Page> getPages();
    
    @OptionalItem
    @IterableClass(type = Entry.class)
    public Iterable<Entry> getEntries();
    
    @OptionalItem
    @IterableClass(type = Entry.class)
    
    public Iterable<Entry> getEntries(String pageref);
    
    public String getComment();
}