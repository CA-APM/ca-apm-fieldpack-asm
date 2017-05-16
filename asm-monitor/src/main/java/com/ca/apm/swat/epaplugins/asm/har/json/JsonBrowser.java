package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Browser;

import org.json.JSONObject;

/**
 * Implement Har Browser Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonBrowser implements Browser {

    JSONObject obj;

    public JsonBrowser(JSONObject obj) {
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