package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Page;
import com.ca.apm.swat.epaplugins.asm.har.PageTimings;
import com.ca.apm.swat.epaplugins.asm.har.Assertion;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Implement Har Page Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class JsonPage implements Page {

    JSONObject obj;

    public JsonPage(JSONObject obj) {
        this.obj = obj;
    }

    /** 
     * Return list of all assertions.
     * 
     * @return assertions
     */
    public Iterable<Assertion> get_assertions() {

        if (obj.has("_assertions")) {
            JSONArray allAssertions = obj.getJSONArray("_assertions");

            ArrayList<Assertion> assertions = new ArrayList<Assertion>(allAssertions.length());

            for (int i = 0; i < allAssertions.length(); i++) {
                Assertion assertion = new JsonAssertion(allAssertions.getJSONObject(i));
                assertions.add(assertion);
            }
            return assertions;
        } else {
            return new ArrayList<Assertion>(0);
        }
    }

    public String getComment() {
        return obj.getString("comment");
    }

    public String getId() {
        return obj.getString("id");
    }

    public PageTimings getPageTimings() {
        return new JsonPageTimings(obj.getJSONObject("pageTimings"));
    }

    public Calendar getStartedDateTime() {
        // not implemented
        return null;
    }

    public String getTitle() {
        return obj.getString("title");
    }

}