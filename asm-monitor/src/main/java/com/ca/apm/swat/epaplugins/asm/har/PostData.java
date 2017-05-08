package com.ca.apm.swat.epaplugins.asm.har;

public interface PostData {
    public String getMimeType();
    @IterableClass(type = Param.class)
    public Iterable<Param> getParams();
    public String getText();
    @OptionalItem
    public String getComment();
}
