package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.monitor.Monitor;
import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the rule_get API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class MonitorTest extends FileTest {


    /**
     * Test getFolders() without any properties.
     */
    @Test
    public void getMonitorsSimple() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            EpaUtils.getProperties().setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
            EpaUtils.getProperties().setProperty(SKIP_INACTIVE_MONITORS, FALSE);

            String[] folders = {"root_folder"};

            // load file
            accessor.loadFile(MONITOR_GET_CMD, "target/test-classes/rule_get.json");

            // call API
            HashMap<String, List<Monitor>> folderMap = requestHelper.getMonitors(folders);

            // folderMap should contain those entries
            String[] expectedMonitors = {
                "all_monitors",
                "AT&T Home",
                "CA Home page",
                "Cat.com click-through RBM",
                "foo",
                "Google.com",
                "Test parameter passing",
                "This is a long monitor name to see if there is a limitation on the length",
                "Wikipedia API test",
                "Yammer - Web landing page"
            };

            if (DEBUG) {
                for (Iterator<String> fit = folderMap.keySet().iterator(); fit.hasNext(); ) {
                    String key = fit.next();
                    System.out.println("folder " + key);

                    for (Iterator<Monitor> rit = folderMap.get(key).iterator(); rit.hasNext(); ) {
                        System.out.println("  " + rit.next().getName());
                    }
                }
            }
            
            // check
            checkMonitors(expectedMonitors, folderMap.get(folders[0]));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test getFolders() without any properties.
     */
    @Test
    public void getMonitorsFolder() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            EpaUtils.getProperties().setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
            EpaUtils.getProperties().setProperty(SKIP_INACTIVE_MONITORS, FALSE);

            String[] folders = {"Tests"};

            // load file
            accessor.loadFile(MONITOR_GET_CMD, "target/test-classes/rule_get_folder.json");

            // call API
            HashMap<String, List<Monitor>> folderMap = requestHelper.getMonitors(folders);

            // folderMap should contain those entries
            String[] expectedMonitors = {
                "all_monitors",
                "Amazon.com",
                "Bad request test",
                "Simple HTTP validation test",
                "Simple HTTP validation test - fail",
                "Simple JMeter 2",
                "Simple JMeter recording"
            };

            if (DEBUG) {
                for (Iterator<String> fit = folderMap.keySet().iterator(); fit.hasNext(); ) {
                    String key = fit.next();
                    System.out.println("folder " + key);

                    for (Iterator<Monitor> rit = folderMap.get(key).iterator(); rit.hasNext(); ) {
                        System.out.println("  " + rit.next().getName());
                    }
                }
            }
            
            // check
            checkMonitors(expectedMonitors, folderMap.get(folders[0]));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test getFolders() with asm.skipInactiveMonitors=true.
     */
    @Test
    public void getMonitorsSkipInactive() {

        try {
            // set properties
            EpaUtils.getProperties().setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            EpaUtils.getProperties().setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
            EpaUtils.getProperties().setProperty(SKIP_INACTIVE_MONITORS, TRUE);

            String[] folders = {"Tests"};

            // load file
            accessor.loadFile(MONITOR_GET_CMD, "target/test-classes/rule_get_folder.json");

            // call API
            HashMap<String, List<Monitor>> folderMap = requestHelper.getMonitors(folders);

            // folderMap should contain those entries
            String[] expectedMonitors = {
                "Amazon.com",
                "Bad request test",
                "Simple HTTP validation test",
                "Simple HTTP validation test - fail",
                "Simple JMeter recording",
                "Simple JMeter 2"
            };

            if (DEBUG) {
                for (Iterator<String> fit = folderMap.keySet().iterator(); fit.hasNext(); ) {
                    String key = fit.next();
                    System.out.println("folder " + key);

                    for (Iterator<Monitor> rit = folderMap.get(key).iterator(); rit.hasNext(); ) {
                        System.out.println("  " + rit.next().getName());
                    }
                }
            }
            
            // check
            checkMonitors(expectedMonitors, folderMap.get(folders[0]));
            
            // assert that monitor "Simple JMeter 2" is not active
            for (Iterator<Monitor> it = folderMap.get(folders[0]).iterator(); it.hasNext(); ) {
                Monitor mon = it.next();
                if (mon.getName().equals("Simple JMeter 2")) {
                    Assert.assertFalse(mon.isActive());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Checks that all expected monitors exist in the list.
     * @param expected array of expected strings
     * @param actual list of monitors to check against
     */
    public void checkMonitors(String[] expected, List<Monitor> actual) {

        Assert.assertEquals("expected " + expected.length + " entries",
            expected.length, actual.size());

        for (int i = 0; i < expected.length; ++i) {
            boolean match = false;
            
            for (int j = 0; j < actual.size(); ++j) {
                if (expected[i].equals(actual.get(j).getName())) {
                    match = true; // we have a match
                    break; // jump out of actual loop
                }
            }
            
            // we have no match
            if (!match) {
                Assert.fail(expected[i] + " not found");
            }
        }
    }
}
