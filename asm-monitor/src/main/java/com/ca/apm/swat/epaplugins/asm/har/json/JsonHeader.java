/**
 * Implement Har Header Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */

package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Header;

import org.json.JSONObject;

public class JsonHeader implements Header {

    JSONObject obj;

    public JsonHeader(JSONObject obj) {
        this.obj = obj;
    }
    
    /**
     * Get the name field.
     * 
     * @returns name field
     */
    public String getName() {
        return obj.getString("name");
    }
    
    /**
     * Get the value field.
     * 
     * @returns value field
     */
    public String getValue() {
        return obj.getString("value");
    }
    

    /** 
     * Get the comment field.
     * 
     * @returns Comment field
     */
    public String getComment() {
        return obj.getString("comment");
    }
}