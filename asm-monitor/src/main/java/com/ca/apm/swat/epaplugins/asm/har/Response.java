package com.ca.apm.swat.epaplugins.asm.har;

public interface Response {
    public int getStatus();
    
    public String getStatusText();
    
    public String getHttpVersion();
    
    @IterableClass(type = Cookie.class)
    public Iterable<Cookie> getCookies();
    
    @IterableClass(type = Header.class)
    public Iterable<Header> getHeaders();
    
    public Content getContent();
    
    public String getRedirectUrl();
    
    public int getHeadersSize();
    
    public int getBodySize();
    
    @OptionalItem
    public String getComment();
}
