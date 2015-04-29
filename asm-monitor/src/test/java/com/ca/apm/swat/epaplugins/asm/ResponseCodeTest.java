package com.ca.apm.swat.epaplugins.asm;

import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.ca.apm.swat.epaplugins.asm.monitor.JMeterScriptHandler;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the fldr_get API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class ResponseCodeTest extends FileTest {


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
     * Test responseCode 401.
     */
    @Test
    public void test401() {

        try {
            String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<testResults version=\"1.2\">"
                    + "<httpSample t=\"322\" lt=\"322\" rt=\"1\" pt=\"321\" dl=\"321\" "
                    + "ts=\"1430332869872\" s=\"false\" lb=\"TestService\" "
                    + "rc=\"401\" rm=\"Unauthorized\" dt=\"text\" de=\"utf-8\" by=\"1492\" "
                    + "sc=\"1\" ec=\"1\" hn=\"test.ca.com\" "
                    + "wm.user=\"\" wm.memory=\"\">"
                    + "<assertionResult>"
                    + "<name>Response Assertion - Contains</name>"
                    + "<failure>true</failure>"
                    + "<error>false</error>"
                    + "<failureMessage>Test failed: text expected to contain /Success/"
                    + "</failureMessage>"
                    + "</assertionResult>"
                    + "<responseHeader class=\"java.lang.String\">HTTP/1.1 401 Unauthorized"
                    + "Date: Wed, 29 Apr 2015 18:40:14 GMT"
                    + "Server: Apache"
                    + "WWW-Authenticate: Basic realm=&quot;testLDAP&quot;"
                    + "Keep-Alive: timeout=5, max=24"
                    + "Connection: Keep-Alive"
                    + "Content-Type: text/html;charset=utf-8"
                    + "Set-Cookie: NSC_qsebb63o-wjq-ttm-443="
                    + "ffffffff095c651b45525d5f4f58455e445a4a42378b;path=/;secure;httponly"
                    + "Set-Cookie: citrix_ns_id=1059HF8lcHXoSsKQpg/Kgz2uyBEA010;"
                    + " Domain=.ca.com; Path=/; HttpOnly"
                    + "X-Expires-Orig: None"
                    + "Cache-Control: max-age=0, must-revalidate, private"
                    + "Transfer-Encoding: chunked"
                    + "</responseHeader>"
                    + "  <requestHeader class=\"java.lang.String\">Content-Length: 550"
                    + "Connection: keep-alive"
                    + "Content-Type: text/xml; charset=utf-8"
                    + "</requestHeader>"
                    + "  <java.net.URL>https://test.ca.com/TestService/"
                    + "services/TestServicePort</java.net.URL>"
                    + "</httpSample>"
                    + "</testResults>";

            JMeterScriptHandler handler = new JMeterScriptHandler();
            String metricTree = "Monitors|Test|TestService";
            
            MetricMap metricMap = handler.generateMetrics(xmlString, metricTree);
            
            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            String[] expectedMetrics = {
                    metricTree + "|001 TestService:Response Code",
                    metricTree + "|001 TestService:Status Message",
                    metricTree + "|001 TestService:Status Message Value"};

            checkMetrics(expectedMetrics, metricMap);

            Assert.assertEquals("401", metricMap.get(expectedMetrics[0]));
            Assert.assertEquals("401 - Unauthorized", metricMap.get(expectedMetrics[1]));
            Assert.assertEquals("1", metricMap.get(expectedMetrics[2]));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test responseCode 200.
     */
    @Test
    public void test200Ok() {

        try {
            String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<testResults version=\"1.2\">"
                    + "<httpSample t=\"322\" lt=\"322\" rt=\"1\" pt=\"321\" dl=\"321\" "
                    + "ts=\"1430332869872\" s=\"false\" lb=\"TestService\" "
                    + "rc=\"200\" rm=\"OK\" dt=\"text\" de=\"utf-8\" by=\"1492\" "
                    + "sc=\"1\" ec=\"0\" hn=\"test.ca.com\" "
                    + "wm.user=\"\" wm.memory=\"\">"
                    + "<assertionResult>"
                    + "<name>Response Assertion - Contains</name>"
                    + "<failure>false</failure>"
                    + "<error>false</error>"
                    + "</assertionResult>"
                    + "<responseHeader class=\"java.lang.String\">HTTP/1.1 200 OK"
                    + "Date: Wed, 29 Apr 2015 18:40:14 GMT"
                    + "Server: Apache"
                    + "WWW-Authenticate: Basic realm=&quot;testLDAP&quot;"
                    + "Keep-Alive: timeout=5, max=24"
                    + "Connection: Keep-Alive"
                    + "Content-Type: text/html;charset=utf-8"
                    + "Set-Cookie: NSC_qsebb63o-wjq-ttm-443="
                    + "ffffffff095c651b45525d5f4f58455e445a4a42378b;path=/;secure;httponly"
                    + "Set-Cookie: citrix_ns_id=1059HF8lcHXoSsKQpg/Kgz2uyBEA010;"
                    + " Domain=.test.com; Path=/; HttpOnly"
                    + "X-Expires-Orig: None"
                    + "Cache-Control: max-age=0, must-revalidate, private"
                    + "Transfer-Encoding: chunked"
                    + "</responseHeader>"
                    + "  <requestHeader class=\"java.lang.String\">Content-Length: 550"
                    + "Connection: keep-alive"
                    + "Content-Type: text/xml; charset=utf-8"
                    + "</requestHeader>"
                    + "  <java.net.URL>https://testservices.ca.com/TestService/"
                    + "services/TestServicePort</java.net.URL>"
                    + "</httpSample>"
                    + "</testResults>";

            JMeterScriptHandler handler = new JMeterScriptHandler();
            String metricTree = "Monitors|Test|TestService";
            
            MetricMap metricMap = handler.generateMetrics(xmlString, metricTree);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            String[] expectedMetrics = {
                    metricTree + "|001 TestService:Response Code",
                    metricTree + "|001 TestService:Status Message",
                    metricTree + "|001 TestService:Status Message Value"};

            checkMetrics(expectedMetrics, metricMap);
            
            Assert.assertEquals("200", metricMap.get(expectedMetrics[0]));
            Assert.assertEquals("200 - OK", metricMap.get(expectedMetrics[1]));
            Assert.assertEquals("1", metricMap.get(expectedMetrics[2]));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test responseCode 200 with assertion.
     */
    @Test
    public void test200Assertion() {

        try {
            String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<testResults version=\"1.2\">"
                    + "<httpSample t=\"322\" lt=\"322\" rt=\"1\" pt=\"321\" dl=\"321\" "
                    + "ts=\"1430332869872\" s=\"false\" lb=\"TestService\" "
                    + "rc=\"200\" rm=\"OK\" dt=\"text\" de=\"utf-8\" by=\"1492\" "
                    + "sc=\"1\" ec=\"1\" hn=\"test.ca.com\" "
                    + "wm.user=\"\" wm.memory=\"\">"
                    + "<assertionResult>"
                    + "<name>Response Assertion - Contains</name>"
                    + "<failure>true</failure>"
                    + "<error>false</error>"
                    + "<failureMessage>Test failed: text expected to contain /Success/"
                    + "</failureMessage>"
                    + "</assertionResult>"
                    + "<responseHeader class=\"java.lang.String\">HTTP/1.1 200 OK"
                    + "Date: Wed, 29 Apr 2015 18:40:14 GMT"
                    + "Server: Apache"
                    + "WWW-Authenticate: Basic realm=&quot;testLDAP&quot;"
                    + "Keep-Alive: timeout=5, max=24"
                    + "Connection: Keep-Alive"
                    + "Content-Type: text/html;charset=utf-8"
                    + "Set-Cookie: NSC_qsebb63o-wjq-ttm-443="
                    + "ffffffff095c651b45525d5f4f58455e445a4a42378b;path=/;secure;httponly"
                    + "Set-Cookie: citrix_ns_id=1059HF8lcHXoSsKQpg/Kgz2uyBEA010;"
                    + " Domain=.test.com; Path=/; HttpOnly"
                    + "X-Expires-Orig: None"
                    + "Cache-Control: max-age=0, must-revalidate, private"
                    + "Transfer-Encoding: chunked"
                    + "</responseHeader>"
                    + "  <requestHeader class=\"java.lang.String\">Content-Length: 550"
                    + "Connection: keep-alive"
                    + "Content-Type: text/xml; charset=utf-8"
                    + "</requestHeader>"
                    + "  <java.net.URL>https://testservices.ca.com/TestService/"
                    + "services/TestServicePort</java.net.URL>"
                    + "</httpSample>"
                    + "</testResults>";

            JMeterScriptHandler handler = new JMeterScriptHandler();
            String metricTree = "Monitors|Test|TestService";
            
            MetricMap metricMap = handler.generateMetrics(xmlString, metricTree);

            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            String[] expectedMetrics = {
                    metricTree + "|001 TestService:Response Code",
                    metricTree + "|001 TestService:Status Message",
                    metricTree + "|001 TestService:Status Message Value"};

            checkMetrics(expectedMetrics, metricMap);
            
            Assert.assertEquals("200", metricMap.get(expectedMetrics[0]));
            Assert.assertEquals("Test failed: text expected to contain /Success/", metricMap.get(expectedMetrics[1]));
            Assert.assertEquals("404", metricMap.get(expectedMetrics[2]));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test responseCode 404.
     */
    @Test
    public void test404() {

        try {
            String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<testResults version=\"1.2\">"
                    + "<httpSample t=\"322\" lt=\"322\" rt=\"1\" pt=\"321\" dl=\"321\" "
                    + "ts=\"1430332869872\" s=\"false\" lb=\"TestService\" "
                    + "rc=\"404\" rm=\"Not found\" dt=\"text\" de=\"utf-8\" by=\"1492\" "
                    + "sc=\"1\" ec=\"1\" hn=\"test.ca.com\" "
                    + "wm.user=\"\" wm.memory=\"\">"
                    + "<assertionResult>"
                    + "<name>Response Assertion - Contains</name>"
                    + "<failure>true</failure>"
                    + "<error>false</error>"
                    + "<failureMessage>Test failed: text expected to contain /Success/"
                    + "</failureMessage>"
                    + "</assertionResult>"
                    + "<responseHeader class=\"java.lang.String\">HTTP/1.1 404 Not found"
                    + "Date: Wed, 29 Apr 2015 18:40:14 GMT"
                    + "Server: Apache"
                    + "WWW-Authenticate: Basic realm=&quot;testLDAP&quot;"
                    + "Keep-Alive: timeout=5, max=24"
                    + "Connection: Keep-Alive"
                    + "Content-Type: text/html;charset=utf-8"
                    + "Set-Cookie: NSC_qsebb63o-wjq-ttm-443="
                    + "ffffffff095c651b45525d5f4f58455e445a4a42378b;path=/;secure;httponly"
                    + "Set-Cookie: citrix_ns_id=1059HF8lcHXoSsKQpg/Kgz2uyBEA010;"
                    + " Domain=.ca.com; Path=/; HttpOnly"
                    + "X-Expires-Orig: None"
                    + "Cache-Control: max-age=0, must-revalidate, private"
                    + "Transfer-Encoding: chunked"
                    + "</responseHeader>"
                    + "  <requestHeader class=\"java.lang.String\">Content-Length: 550"
                    + "Connection: keep-alive"
                    + "Content-Type: text/xml; charset=utf-8"
                    + "</requestHeader>"
                    + "  <java.net.URL>https://test.ca.com/TestService/"
                    + "services/TestServicePort</java.net.URL>"
                    + "</httpSample>"
                    + "</testResults>";

            JMeterScriptHandler handler = new JMeterScriptHandler();
            String metricTree = "Monitors|Test|TestService";
            
            MetricMap metricMap = handler.generateMetrics(xmlString, metricTree);
            
            if (DEBUG) {
                TreeSet<String> sortedSet = new TreeSet<String>(metricMap.keySet());
                for (Iterator<String> it = sortedSet.iterator(); it.hasNext(); ) {
                    String key = it.next();
                    System.out.println(key + " = " + metricMap.get(key));
                }
            }

            String[] expectedMetrics = {
                    metricTree + "|001 TestService:Response Code",
                    metricTree + "|001 TestService:Status Message",
                    metricTree + "|001 TestService:Status Message Value"};

            checkMetrics(expectedMetrics, metricMap);

            Assert.assertEquals("404", metricMap.get(expectedMetrics[0]));
            Assert.assertEquals("404 - Not found", metricMap.get(expectedMetrics[1]));
            Assert.assertEquals("404", metricMap.get(expectedMetrics[2]));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
