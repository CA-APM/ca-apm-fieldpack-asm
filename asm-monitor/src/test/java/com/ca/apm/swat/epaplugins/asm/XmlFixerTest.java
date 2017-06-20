package com.ca.apm.swat.epaplugins.asm;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.monitor.Handler;
import com.ca.apm.swat.epaplugins.asm.monitor.XmlFixer;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;
import java.util.Map;

/**
 * Test class for testing the XmlFixer class.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class XmlFixerTest implements AsmProperties {

    public final static boolean DEBUG = TRUE.equals(System.getProperty("DEBUG", FALSE));

    /**
     * Test xmlFixer() .
     */
    @Test
    public void xmlFixerTest() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(FIX_AMPERSAND, TRUE);

            String[] test = {"Me & you",
                             "Tom & Jerry",
                             "Huey, Dewey & Louie",
                             "http://localhost:8082/ApmServer/#/map?f=%7B%22apf%22:null%7D&g=3&m=L&cht=0&chs=1&cha=0",
                             "no ampersand whatsoever"};

            String[] expected = {"Me &amp; you",
                             "Tom &amp; Jerry",
                             "Huey, Dewey &amp; Louie",
                             "http://localhost:8082/ApmServer/#/map?f=%7B%22apf%22:null%7D&amp;g=3&amp;m=L&amp;cht=0&amp;chs=1&amp;cha=0",
                             "no ampersand whatsoever"};

            Checker checker = this.new Checker();
            Handler fixer = new XmlFixer(checker);
            
            for (int i = 0; i < test.length; ++i) {
                checker.setExpectedResult(expected[i]);

                fixer.generateMetrics(new MetricMap(), test[i], null);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    private class Checker implements Handler {
        private String expected = null;
    
        public void setExpectedResult(String expectedResult) {
            expected = expectedResult;
        }
    
        public Map<String, String> generateMetrics(Map<String, String> map, String xmlString, String metricTree) {
            if (DEBUG) {
                System.out.println(xmlString);
            }
            Assert.assertEquals(xmlString, expected);
            return map;
        }

        @Override
        public Handler getSuccessor() {
            return null;
        }
    }

}
