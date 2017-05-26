package com.ca.apm.swat.epaplugins.asm;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.HarHandler;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.wily.introscope.epagent.EpaUtils;
import java.util.Map;

/**
 * Test class for testing the RBM - Har file handling
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Rod Olliver - CA Services
 *
 */
public class RbmDisplayTest extends FileTest {


    @Before
    public void setup() {
        super.setup();
        
        // set properties
        Properties props = EpaUtils.getProperties();
        props.setProperty(RESPONSE_CODES, "1,404,600");
        props.setProperty(RESPONSE_CODES + ".1", "200,401,6007");
        props.setProperty(RESPONSE_CODES + ".404", "6404,7001,9501");
        props.setProperty(RESPONSE_CODES + ".600", "110,1042,1043,7011,-11");
        props.setProperty(REPORT_ASSERTION_FAILURES_AS, "404");

        Formatter.setProperties(props);
    }

    /**
     * Test responseCode 200.
     */
    @Test
    public void testRbmOutput() {

        try {
            	String xmlString =  new String(
                            Files.readAllBytes(
                                    Paths.get("target/test-classes/firefox_har_full2.json")
                            )
                        );	
            		
            HarHandler handler = new HarHandler();
            String metricTree = "Monitors|CA|TestService";
            
            Map<String, String> metricMap = handler.generateMetrics(new MetricMap(), xmlString, metricTree);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            String[] expectedMetrics = {
                    metricTree + "|001 https_//test.ca.com/TestService/services/TestServicePort:Response Code",
                    metricTree + "|001 https_//test.ca.com/TestService/services/TestServicePort:Status Message",
                    metricTree + "|001 https_//test.ca.com/TestService/services/TestServicePort:Status Message Value"};

            if (EpaUtils.getBooleanProperty(REPORT_LABELS_IN_PATH, false)) {
	            expectedMetrics[0] = metricTree + "|001 |TestService:Response Code";
	            expectedMetrics[1] = metricTree + "|001 |TestService:Status Message";
	            expectedMetrics[2] = metricTree + "|001 |TestService:Status Message Value";
            }

            //checkMetrics(expectedMetrics, metricMap);
            
           // Assert.assertEquals("200", metricMap.get(expectedMetrics[0]));
           // Assert.assertEquals("200 - OK", metricMap.get(expectedMetrics[1]));
           // Assert.assertEquals("1", metricMap.get(expectedMetrics[2]));
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}