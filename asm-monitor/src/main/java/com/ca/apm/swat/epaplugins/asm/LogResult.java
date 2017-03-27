package com.ca.apm.swat.epaplugins.asm;

import java.util.Map;

/**
 * Holds the result of logs call.
 * 
 */
public class LogResult {
    private final Map<String, String> map;
    private final String lastId;

    public LogResult(Map<String, String> map, String lastId) {
        this.map = map;
        this.lastId = lastId;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String getLastId() {
        return lastId;
    }
}
