package com.ca.apm.swat.epaplugins.asm;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.error.LoginError;


/**
 * Test login. Needs network access to ASM API.
 *   Run with -DDEBUG=true for debug output.
 *  
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmLoginTest extends FileTest {

    private Properties properties = null;

    /**
     * Print debug output to System.out if system propert DEBUG is true.
     *   Run with -DDEBUG=true for debug output.
     */
    public final static boolean DEBUG = TRUE.equals(System.getProperty("DEBUG", FALSE));

    /**
     * Set up the test environment.
     */
    @Before
    public void setup() {
        String propertyFileName = "target/test-classes/AppSyntheticMonitor.properties";

        try {
            this.properties = AsmReader.readPropertiesFromFile(propertyFileName);
            this.properties.setProperty(USER, "bryan.jasper@ca.com");
            this.properties.setProperty(URL, "https://api.asm.ca.com/1.6/");
            this.properties.setProperty(WAIT_TIME, "5000");
            this.properties.setProperty(LOCAL_TEST, FALSE);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Try to login with plain password.
     */
    // skip test because we don't want a plain password here
    //@Test
    public void plainLogin() {

        try {
            this.properties.setProperty(PASSWORD, "insert password here");
            this.properties.setProperty(PASSWORD_ENCRYPTED, FALSE);

            if (DEBUG) {
                System.out.println("\nplainLogin: pw=" + this.properties.getProperty(PASSWORD)
                    + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));
            }

            AsmAccessor accessor = new AsmAccessor();
            AsmRequestHelper requestHelper = new AsmRequestHelper(accessor);

            requestHelper.connect();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Try to login with encrypted password.
     */
    @Test
    public void encryptedLogin() {

        try {
            this.properties.setProperty(PASSWORD, "sFc9hz82waUtIqjHvinc6Q==");
            this.properties.setProperty(PASSWORD_ENCRYPTED, TRUE);

            if (DEBUG) {
                System.out.println("\nencryptedLogin: pw=" + this.properties.getProperty(PASSWORD)
                    + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));
            }
            
            AsmAccessor accessor = new AsmAccessor();
            AsmRequestHelper requestHelper = new AsmRequestHelper(accessor);

            requestHelper.connect();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Try to login with encrypted password again.
     * This is to make sure no state is left after previous error with decryption.
     */
    @Test
    public void encryptedLogin2() {

        try {
            this.properties.setProperty(PASSWORD, "sFc9hz82waUtIqjHvinc6Q==");
            this.properties.setProperty(PASSWORD_ENCRYPTED, TRUE);

            if (DEBUG) {
                System.out.println("\nencryptedLogin2: pw=" + this.properties.getProperty(PASSWORD)
                    + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));
            }
            
            AsmAccessor accessor = new AsmAccessor();
            AsmRequestHelper requestHelper = new AsmRequestHelper(accessor);

            requestHelper.connect();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Try to login with wrong password.
     */
    @Test
    public void wrongLogin() {

        try {
            this.properties.setProperty(PASSWORD, "wrong password");
            this.properties.setProperty(PASSWORD_ENCRYPTED, FALSE);

            if (DEBUG) {
                System.out.println("\nwrongLogin: pw=" + this.properties.getProperty(PASSWORD)
                    + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));
            }

            AsmAccessor accessor = new AsmAccessor();
            AsmRequestHelper requestHelper = new AsmRequestHelper(accessor);

            requestHelper.connect();

            Assert.fail("login with wrong plain password succeeded");

        } catch (LoginError e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Try to login with wrong encrypted password.
     */
    @Test
    public void wrongEncryptedLogin() {

        try {
            this.properties.setProperty(PASSWORD, "sFc1234ggaUtIqjHvinc6Q==");
            this.properties.setProperty(PASSWORD_ENCRYPTED, TRUE);

            if (DEBUG) {
                System.out.println("\nwrongEncryptedLogin: pw=" + this.properties.getProperty(PASSWORD)
                    + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));
            }
            AsmAccessor accessor = new AsmAccessor();
            AsmRequestHelper requestHelper = new AsmRequestHelper(accessor);

            requestHelper.connect();

            Assert.fail("login with wrong encrypted password succeeded");

        } catch (LoginError e) {
            Assert.assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
