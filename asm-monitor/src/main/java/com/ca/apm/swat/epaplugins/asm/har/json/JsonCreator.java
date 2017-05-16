package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Creator;

import org.json.JSONObject;

/**
 * Implement Har Creator Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonCreator implements Creator {

    JSONObject obj;

    public JsonCreator(JSONObject obj) {
        this.obj = obj;
    }

    public String getVersion() {
        return obj.getString("version");

    }

    public String getComment() {
        return obj.getString("comment");
    }

    public String getName() {
        return obj.getString("name");
    }
}