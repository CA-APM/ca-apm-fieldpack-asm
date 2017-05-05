package com.ca.apm.swat.epaplugins.asm.har;

import org.eclipse.swt.browser.Browser;

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
    public String getComment();
}
