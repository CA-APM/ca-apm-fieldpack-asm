package com.ca.apm.swat.epaplugins.asm.har;

public interface Content {
    public int getSize();
    @OptionalItem
    public Integer getCompression();
    public String getMimeType();
    @OptionalItem
    public String getText();
    @OptionalItem
    public String getEncoding();
    @OptionalItem
    public String getComment();
}
