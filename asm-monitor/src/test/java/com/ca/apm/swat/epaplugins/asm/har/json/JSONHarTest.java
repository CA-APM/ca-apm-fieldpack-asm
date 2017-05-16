/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ca.apm.swat.epaplugins.asm.har.json;

import com.ca.apm.swat.epaplugins.asm.FileTest;
import com.ca.apm.swat.epaplugins.asm.har.Entry;
import com.ca.apm.swat.epaplugins.asm.har.Page;
import com.ca.apm.swat.epaplugins.asm.har.PageTimings;
import com.ca.apm.swat.epaplugins.asm.har.Assertion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
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
    	
    	String s =  new String(
                                Files.readAllBytes(
                                        Paths.get("target/test-classes/firefox_har_full2.json")
                                )
                            );
 
        JsonHar har = new JsonHar( new JSONObject(s));
        
        assertThat(har.getLog().getVersion(), is("1.2"));
        assertThat(har.getLog().getBrowser().getVersion(), is("23.0"));
        assertThat(har.getLog().getCreator().getName(), is("BrowserMob Proxy"));
        
        
        for(Page page : har.getLog().getPages()) {
			PageTimings p = page.getPageTimings();
			assertNotNull(p);
			
			//System.out.println(page.getTitle() + ":onload = " + p.getOnLoad());
			
			if(p != null)
	            assertThat(page.getPageTimings().getOnLoad(), isOneOf(6294,218,36,1685,1781,668));
			
			for(Assertion assertion: page.get_assertions() )
			{
				assertThat(assertion.getName(), is("Assertion"));
				assertTrue(assertion.getError() == false);
				assertThat(assertion.getMessage(), is("Content check for pattern How did the idea for doodles"));
				System.out.println("Assertion name=" + assertion.getName());
				System.out.println("Assertion error=" + assertion.getError());
				System.out.println("Assertion message=" + assertion.getMessage());
			}
			
	        
	        for(Entry entry : har.getLog().getEntries(page.getId())) {
	            
//	        	System.out.println(entry.getPageref());
	        	
	//       	System.out.println("Entry " + entry.getPageref() + " request-headersize=>  " + entry.getRequest().getHeadersSize() );
	//        	System.out.println("Entry " + entry.getPageref() + " started=" + entry.getStartedDateTime());
	//       	System.out.println("Entry " + entry.getPageref() + " comment=" + entry.getComment());
	//        	System.out.println("Entry " + entry.getPageref() + " request-url=>  " + entry.getRequest().getUrl() );
	//       	System.out.println("Entry " + entry.getPageref() + " request-headersize=>  " + entry.getRequest().getHeadersSize() );
	//      	System.out.println("Entry " + entry.getPageref() + " timings=recv=>> " + entry.getTimings().getReceive() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=send=>> " + entry.getTimings().getSend() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=ssl =>> " + entry.getTimings().getSsl() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=conn=>> " + entry.getTimings().getConnect() );
	//       	System.out.println("Entry " + entry.getPageref() + " timings=dns =>> " + entry.getTimings().getDns() );
	//       	System.out.println("Entry " + entry.getPageref() + " timings=bloc=>>> " + entry.getTimings().getBlocked() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=wait=>> " + entry.getTimings().getWait() );
	//        	System.out.println("Entry " + entry.getPageref() + " response=bodysize=>> " + entry.getResponse().getBodySize());
	        	
	            assertThat(entry.getPageref(),  containsString("Page"));
	            assertThat("url=" + entry.getRequest().getUrl(), entry.getRequest().getUrl(), containsString("http"));
	            assertTrue("Request size = " + entry.getRequest().getHeadersSize() + " is <= 0 or > 882",  entry.getRequest().getHeadersSize() > 0 && entry.getRequest().getHeadersSize() <= 882);
	            assertTrue("Wait Time = " + entry.getTimings().getWait() + " is <= 0 or > 436",  entry.getTimings().getWait() > 0 && entry.getTimings().getWait() <= 436);
	        }
        }
    }
    
    /*  Similar test, but with different JSON that is missing some nodes */
    @Test
    public void testGetLogWithMissingNodes() throws IOException {        
    	
    	String s =  new String(
                                Files.readAllBytes(
                                        Paths.get("target/test-classes/firefox_har_full_with_missing.json")
                                )
                            );
 
        JsonHar har = new JsonHar( new JSONObject(s));
        

        assertThat(har.getLog().getVersion(), is("1.2"));
        assertThat(har.getLog().getBrowser().getVersion(), is("23.0"));
        assertThat(har.getLog().getCreator().getName(), is("BrowserMob Proxy"));
        
        
        for(Page page : har.getLog().getPages()) {
			PageTimings p = page.getPageTimings();
			assertNotNull(p);
			
	//		System.out.println(page.getTitle() + ":onload = " + p.getOnLoad());
			
			if(p != null)
	            assertThat(page.getPageTimings().getOnLoad(), isOneOf(6294,218,36,1685,1781,668));
			
			for(Assertion assertion: page.get_assertions() )
			{
				System.out.println("Assertion name=" + assertion.getName());
				System.out.println("Assertion error=" + assertion.getError());
				System.out.println("Assertion type=" + assertion.getType());
				System.out.println("Assertion message=" + assertion.getMessage());
			}
			
	        
	        for(Entry entry : har.getLog().getEntries(page.getId())) {
	            assertThat(entry.getPageref(),  containsString("Page"));
	            
	            try{
	            	assertThat(entry.getRequest().getUrl(), containsString("http"));
	            }
	            catch(JSONException e){
	            	// do nothing / expected
	            }
	            
	            try{
		            assertTrue("Request size = " + entry.getRequest().getHeadersSize() + " is <= 0 or > 882",  entry.getRequest().getHeadersSize() > 0 && entry.getRequest().getHeadersSize() <= 882);
		            assertTrue("Wait Time = " + entry.getTimings().getWait() + " is <= 0 or > 436",  entry.getTimings().getWait() > 0 && entry.getTimings().getWait() <= 436);
	            }catch(JSONException e){
	            	// expected
	            }
	            
	//       	System.out.println("Entry " + entry.getPageref() + " request-headersize=>  " + entry.getRequest().getHeadersSize() );
	//        	System.out.println("Entry " + entry.getPageref() + " started=" + entry.getStartedDateTime());
	//       	System.out.println("Entry " + entry.getPageref() + " comment=" + entry.getComment());
	//        	System.out.println("Entry " + entry.getPageref() + " request-url=>  " + entry.getRequest().getUrl() );
	//       	System.out.println("Entry " + entry.getPageref() + " request-headersize=>  " + entry.getRequest().getHeadersSize() );
	//      	System.out.println("Entry " + entry.getPageref() + " timings=recv=>> " + entry.getTimings().getReceive() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=send=>> " + entry.getTimings().getSend() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=ssl =>> " + entry.getTimings().getSsl() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=conn=>> " + entry.getTimings().getConnect() );
	//       	System.out.println("Entry " + entry.getPageref() + " timings=dns =>> " + entry.getTimings().getDns() );
	//       	System.out.println("Entry " + entry.getPageref() + " timings=bloc=>>> " + entry.getTimings().getBlocked() );
	//        	System.out.println("Entry " + entry.getPageref() + " timings=wait=>> " + entry.getTimings().getWait() );
	//        	System.out.println("Entry " + entry.getPageref() + " response=bodysize=>> " + entry.getResponse().getBodySize());
	        	
	        }
        }
    }
}
