/**
 * Implement Har PageTimings Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */

package com.ca.apm.swat.epaplugins.asm.har.json;


import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.asm.har.Timings;

public class JsonTimings implements Timings {

    JSONObject obj;

    public JsonTimings(JSONObject obj) {
        this.obj = obj;
    }

    public Integer getDns() {
        return obj.getInt("dns");
    }

    public Integer getConnect() {
        return obj.getInt("connect");
    }

    public int getSend() {
        return obj.getInt("send");
    }

    public int getWait() {
        return obj.getInt("wait");
    }

    public int getReceive() {
        return obj.getInt("receive");
    }

    public int getSsl() {
        return obj.getInt("ssl");
    }

    public Integer getBlocked() {
        return obj.getInt("blocked");
    }

    public String getComment() {
        return obj.getString("comment");
    }

}