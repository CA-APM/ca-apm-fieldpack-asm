package com.ca.apm.swat.epaplugins.asm;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.format.Formatter;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the fldr_get API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class FormatterTest extends FileTest {


    /**
     * Test Formatter.mapResponseToStatusCode().
     */
    @Test
    public void mapResponseToStatusCode() {

        try {
            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(RESPONSE_CODES, "200,404,600");
            props.setProperty(RESPONSE_CODES + ".200", "401,6007");
            props.setProperty(RESPONSE_CODES + ".404", "6404,7001,9501");
            props.setProperty(RESPONSE_CODES + ".600", "110,1042,1043,7011,-11");

            Formatter.setProperties(props);

            Formatter format = Formatter.getInstance();

            // call API
            Assert.assertEquals("200 expected", 200, format.mapResponseToStatusCode(401));
            Assert.assertEquals("200 expected", 200, format.mapResponseToStatusCode(6007));
            Assert.assertEquals("404 expected", 404, format.mapResponseToStatusCode(6404));
            Assert.assertEquals("404 expected", 404, format.mapResponseToStatusCode(7001));
            Assert.assertEquals("404 expected", 404, format.mapResponseToStatusCode(9501));
            Assert.assertEquals("600 expected", 600, format.mapResponseToStatusCode(110));
            Assert.assertEquals("600 expected", 600, format.mapResponseToStatusCode(1042));
            Assert.assertEquals("600 expected", 600, format.mapResponseToStatusCode(1043));
            Assert.assertEquals("600 expected", 600, format.mapResponseToStatusCode(7011));
            Assert.assertEquals("600 expected", 600, format.mapResponseToStatusCode(-11));
            Assert.assertEquals("0 expected",     0, format.mapResponseToStatusCode(0));
            Assert.assertEquals("-1 expected",   -1, format.mapResponseToStatusCode(-1));
            Assert.assertEquals("815 expected", 815, format.mapResponseToStatusCode(815));
            Assert.assertEquals("42 expected",    42, format.mapResponseToStatusCode(42));
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
