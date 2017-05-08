package com.ca.apm.swat.epaplugins.asm.har;

public interface Browser {
    public String getName();
    public String getVersion();
    @OptionalItem
    public String getComment();
}
