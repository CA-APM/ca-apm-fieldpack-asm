package com.ca.apm.swat.epaplugins.asm;

import org.junit.Assert;

/**
 * Test class for testing the rule_psp API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class PublicTest extends FileTest {

    @Override
    public void setup() {
        super.setup();
        
        // we need to load the the monitoring station map
        try {
            requestHelper.getMonitoringStations();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("error getting monitoring stations: " + e.getMessage());
        }
    }

    //TODO: implement test cases

}
