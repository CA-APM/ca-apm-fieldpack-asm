package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Cache;
import com.ca.apm.swat.epaplugins.asm.har.CacheState;

import org.json.JSONObject;

/**
 * Implement Har Cache Class .
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonCache implements Cache {

    JSONObject obj;

    public JsonCache(JSONObject obj) {
        this.obj = obj;
    }

    public CacheState getBeforeRequest() {
        /* Not implemented */
        return null;
    }
    
    public CacheState getAfterRequest() {
        /* Not implemented */
        return null;
    }

    public String getComment() {
        return obj.getString("comment");
    }
}