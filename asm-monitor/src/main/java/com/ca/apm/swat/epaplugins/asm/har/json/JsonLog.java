/**
 * Implement Har Log Class.
 * 
 * @author Rod Olliver - CA Services
 *
 */

package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.har.Creator;
import com.ca.apm.swat.epaplugins.asm.har.Browser;
import com.ca.apm.swat.epaplugins.asm.har.Entry;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ca.apm.swat.epaplugins.asm.har.Log;
import com.ca.apm.swat.epaplugins.asm.har.Page;

public class JsonLog implements Log {

    JSONObject obj;

    public JsonLog(JSONObject obj) {
        this.obj = obj;
    }

    /**
     * Get the comment field.
     * 
     * @return comment
     */
    public String getComment() {
        return obj.getString("comment");
    }

    /**
     * Get the version field.
     * 
     * @return version
     */
    public String getVersion() {
        return obj.getString("version");
    }

    /**
     * Get the creator field.
     * 
     * @return creator
     */
    public Creator getCreator() {
        return new JsonCreator(obj.getJSONObject("creator"));
    }

    public Browser getBrowser() {
        return new JsonBrowser(obj.getJSONObject("browser"));
    }

    /**
    * Get a list of all pages, if there are no pages, this will return an empty list.
    * 
    * @return entries
    */
    public Iterable<Page> getPages() {

        JSONArray allPages;
        ArrayList<Page> pages;

        allPages = obj.getJSONArray("pages");

        if (allPages == null) {
            allPages = new JSONArray();
        }

        pages = new ArrayList<Page>(allPages.length());

        for (int i = 0; i < allPages.length(); i++) {
            pages.add(new JsonPage(allPages.getJSONObject(i)));
        }

        return pages;
    }

   /**
     * Get a list of all entries, if there are no entries, this will return an empty list.
     * 
     * @return entries
     */
    public Iterable<Entry> getEntries() {
        JSONArray allEntries;
        ArrayList<Entry> entries;

        allEntries = obj.getJSONArray("entries");

        if (allEntries == null) {
            allEntries = new JSONArray();
        }

        entries = new ArrayList<Entry>(allEntries.length());

        for (int i = 0; i < allEntries.length(); i++) {
            entries.add(new JsonEntry(allEntries.getJSONObject(i)));
        }

        return entries;
    }

    /**
     * Get a list of all entries that match a given pageref, * If there are no entries, this 
     * will return an empty list.  The pageref should match the ID of the Page node
     * 
     * @param pageref The pageref to match
     * @return entries
     */
    public Iterable<Entry> getEntries(String pageref) {
        JSONArray allEntries;
        ArrayList<Entry> entries;

        allEntries = obj.getJSONArray("entries");

        if (allEntries == null) {
            allEntries = new JSONArray();
        }

        entries = new ArrayList<Entry>(allEntries.length());

        for (int i = 0; i < allEntries.length(); i++) {
            Entry entry = new JsonEntry(allEntries.getJSONObject(i));

            if (entry.getPageref() != null && entry.getPageref().equals(pageref)) {
                entries.add(entry);
            }
        }

        return entries;
    }

}