/**
 * Implement Har Request Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */

package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Request;
import com.ca.apm.swat.epaplugins.asm.har.Cookie;
import com.ca.apm.swat.epaplugins.asm.har.Header;
import com.ca.apm.swat.epaplugins.asm.har.OptionalItem;
import com.ca.apm.swat.epaplugins.asm.har.PostData;
import com.ca.apm.swat.epaplugins.asm.har.QueryString;


import org.json.JSONObject;


public class JsonRequest implements Request {

    JSONObject obj;

    public JsonRequest(JSONObject obj) {
        this.obj = obj;
    }

    public String getMethod() {
        return obj.getString("method");
    }

    public String getUrl() {
        return obj.getString("url");
    }

    public String getHttpVersion() {
        return obj.getString("httpVersion");
    }

    public Iterable<Cookie> getCookies() {
        return null;
    }

    public Iterable<Header> getHeaders() {
        return null;
    }

    public Iterable<QueryString> getQueryString() {
        return null;
    }

    @OptionalItem
    public PostData getPostData() {
        return null;
    }

    public int getHeadersSize() {
        return obj.getInt("headersSize");
    }

    public int getBodySize() {
        return obj.getInt("bodySize");
    }

    public String getComment() {
        return obj.getString("comment");
    }
}
