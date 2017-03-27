/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ca.apm.swat.epaplugins.asm;

import java.util.Map;

/**
 * Holds the result of logs call
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
