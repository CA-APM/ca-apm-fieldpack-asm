package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Entry;
import com.ca.apm.swat.epaplugins.asm.har.Cache;
import com.ca.apm.swat.epaplugins.asm.har.Request;
import com.ca.apm.swat.epaplugins.asm.har.Response;
import com.ca.apm.swat.epaplugins.asm.har.Timings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;

/**
 * Implement Har Entry Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonEntry implements Entry {

    JSONObject obj;

    public JsonEntry(JSONObject obj) {
        this.obj = obj;
    }

    public String getVersion() {
        return obj.getString("version");
    }

    public String getName() {
        return obj.getString("name");
    }

    public String getPageref() {
        return obj.getString("pageref");
    }

    /** 
     * Get the start time for this Entry.
     * 
     * @return Calendar object for the time this entry started
     */
    public Calendar getStartedDateTime() {
        Calendar cal = Calendar.getInstance();

        try {
            cal.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(
                    obj.getString("startedDateTime")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cal;

    }

    public Integer getTime() {
        return obj.getInt("time");
    }

    public Request getRequest() {
        return new JsonRequest(obj.getJSONObject("request"));
    }

    public Response getResponse() {
        return new JsonResponse(obj.getJSONObject("response"));
    }

    public Cache getCache() {
        return new JsonCache(obj.getJSONObject("cache"));
    }

    public Timings getTimings() {
        return new JsonTimings(obj.getJSONObject("timings"));
    }

    public String getServerIpAddress() {
        return obj.getString("ServerIPAddress");
    }

    public String getConnection() {
        return obj.getString("connection");
    }

    public String getComment() {
        return obj.getString("comment");
    }
}