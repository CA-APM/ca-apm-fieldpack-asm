package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for testing the acct_credits API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class CreditTest extends FileTest {

    /**
     * Test getCredits().
     */
    @Test
    public void getCredits() {

        try {
            // call API
            HashMap<String, String> metricMap = requestHelper.getCredits();

            // metricMap should contain those entries
            String expectedMetrics[] = {
                                        "Credits:SMS Credits Available",
                                        "Credits:API Credits Available",
                                        "Credits:Check Credits Available"
            };

            // check
            checkMetrics(expectedMetrics, metricMap);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
