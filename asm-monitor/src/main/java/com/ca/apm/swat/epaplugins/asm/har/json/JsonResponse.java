package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Response;
import com.ca.apm.swat.epaplugins.asm.har.Content;
import com.ca.apm.swat.epaplugins.asm.har.Cookie;
import com.ca.apm.swat.epaplugins.asm.har.Header;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Implement Har Response Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonResponse implements Response {

    JSONObject obj;

    public JsonResponse(JSONObject obj) {
        this.obj = obj;
    }

    public int getStatus() {
        return obj.getInt("status");
    }

    public String getStatusText() {
        return obj.getString("statusText");
    }

    public String getHttpVersion() {
        return obj.getString("httpVersion");
    }

    public Iterable<Cookie> getCookies() {
        return null;
    }

    /** 
     * Get list of all headers, and an empty list if there are none.
     * 
     * @return headers
     */
    public Iterable<Header> getHeaders() {
        if (obj.has("headers")) {
            JSONArray allHeaders = obj.getJSONArray("headers");

            ArrayList<Header> headers = new ArrayList<Header>(allHeaders.length());

            for (int i = 0; i < allHeaders.length(); i++) {
                Header header = new JsonHeader(allHeaders.getJSONObject(i));
                headers.add(header);
            }
            return headers;
        } else {
            return new ArrayList<Header>(0);
        }
    }

    public Content getContent() {
        return null;
    }

    public String getRedirectUrl() {
        return obj.getString("redirectUrl");
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
