package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.PageTimings;

import org.json.JSONObject;

/**
 * Implement Har PageTimings Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonPageTimings implements PageTimings {

    JSONObject obj;

    public JsonPageTimings(JSONObject obj) {
        this.obj = obj;
    }

    public Integer getOnContentLoad() {
        return obj.getInt("onContentLoad");
    }

    public Integer getOnLoad() {
        return obj.getInt("onLoad");
    }

    public String getComment() {
        return obj.getString("commnent");
    }

}