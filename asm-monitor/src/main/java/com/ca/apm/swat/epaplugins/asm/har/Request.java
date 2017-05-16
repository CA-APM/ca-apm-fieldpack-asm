package com.ca.apm.swat.epaplugins.asm.har;

public interface Request {
    public String getMethod();
    
    public String getUrl();
    
    public String getHttpVersion();
    
    @IterableClass(type = Cookie.class)
    public Iterable<Cookie> getCookies();
    
    @IterableClass(type = Header.class)
    public Iterable<Header> getHeaders();
    
    @IterableClass(type = QueryString.class)
    public Iterable<QueryString> getQueryString();
    
    @OptionalItem
    public PostData getPostData();
    
    public int getHeadersSize();
    
    public int getBodySize();
    
    @OptionalItem
    public String getComment();
}
