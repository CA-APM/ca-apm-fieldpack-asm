package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Assertion;

import org.json.JSONObject;

/**
 * Implement Har Assertion Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonAssertion implements Assertion {

    JSONObject obj;

    public JsonAssertion(JSONObject obj) {
        this.obj = obj;
    }

    public String getType() {
        return obj.getString("type");
    }

    public String getName() {
        return obj.getString("name");
    }

    public String getMessage() {
        return obj.getString("message");
    }

    public boolean getError() {
        return obj.getBoolean("error");
    }
}