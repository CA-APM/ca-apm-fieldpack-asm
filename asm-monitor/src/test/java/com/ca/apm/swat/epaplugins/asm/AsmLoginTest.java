package com.ca.apm.swat.epaplugins.asm;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.error.LoginError;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;


public class AsmLoginTest implements AsmProperties {

    private Properties properties = null;

    /**
     * Set up the test environment.
     */
    @Before
    public void setup() {
        this.properties = new Properties();
        this.properties.setProperty(USER, "bryan.jasper@ca.com");
        this.properties.setProperty(URL, "https://api.cloudmonitor.ca.com/1.6");
        this.properties.setProperty(NUM_LOGS, "5");
        this.properties.setProperty(WAIT_TIME, "5000");
        this.properties.setProperty(LOCAL_TEST, FALSE);
    }

    /**
     * Try to login with plain password.
     */
    @Test
    public void plainLogin() {

        try {
            this.properties.setProperty(PASSWORD, "CAdemo123!");
            this.properties.setProperty(PASSWORD_ENCRYPTED, FALSE);

            System.out.println("\nplainLogin: pw=" + this.properties.getProperty(PASSWORD)
                + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));

            CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(this.properties);
            CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(
                cloudMonitorAccessor, this.properties);

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

            System.out.println("\nencryptedLogin: pw=" + this.properties.getProperty(PASSWORD)
                + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));

            CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(this.properties);
            CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(
                cloudMonitorAccessor, this.properties);

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

            System.out.println("\nencryptedLogin2: pw=" + this.properties.getProperty(PASSWORD)
                + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));

            CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(this.properties);
            CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(
                cloudMonitorAccessor, this.properties);

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

            System.out.println("\nwrongLogin: pw=" + this.properties.getProperty(PASSWORD)
                + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));

            CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(this.properties);
            CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(
                cloudMonitorAccessor, this.properties);

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

            System.out.println("\nwrongEncryptedLogin: pw=" + this.properties.getProperty(PASSWORD)
                + ", enc=" + this.properties.getProperty(PASSWORD_ENCRYPTED));

            CloudMonitorAccessor cloudMonitorAccessor = new CloudMonitorAccessor(this.properties);
            CloudMonitorRequestHelper requestHelper = new CloudMonitorRequestHelper(
                cloudMonitorAccessor, this.properties);

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
