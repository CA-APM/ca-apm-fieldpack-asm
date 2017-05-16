
package com.ca.apm.swat.epaplugins.asm.har;

public interface Param {
    public String getName();
    
    @OptionalItem
    public String getValue();
    
    @OptionalItem
    public String getFileName();
    
    @OptionalItem
    public String getContentType();
    
    @OptionalItem
    public String getComment();
}
