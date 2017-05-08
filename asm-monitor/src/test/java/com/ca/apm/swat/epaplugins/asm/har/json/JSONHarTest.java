/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.FileTest;
import com.ca.apm.swat.epaplugins.asm.har.Entry;
import com.ca.apm.swat.epaplugins.asm.har.Header;
import com.ca.apm.swat.epaplugins.asm.har.Page;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Martin Ma?ura <macma13@ca.com>
 */
public class JSONHarTest extends FileTest {

    public JSONHarTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testGetLog() throws IOException {        
        JSONHar har = new JSONHar(
                new JSONObject(
                        new String(
                                Files.readAllBytes(
                                        Paths.get("target/test-classes/firefox_har_full.json")
                                )
                            )
                    )
            );

        assertThat(har.getLog().getVersion(), is("1.2"));

        for(Page page : har.getLog().getPages()) {
            assertThat(page.getPageTimings().getOnLoad(), is(6294));
            assertNull(page.get_assertions());
        }

        int i = 0;
        for(Entry entry : har.getLog().getEntries()) {
            i++;
            assertThat(entry.getPageref(), is("Page 1"));
            if(i == 8) {
                for(Header h : entry.getResponse().getHeaders()) {
                    assertThat(h.getName(), is("Content-Type"));
                    assertThat(h.getValue(), is("text/html; charset=UTF-8"));
                    break;
                }
                break;
            }
            if(i > 8) {
                fail();
            }
        }
    }
}
