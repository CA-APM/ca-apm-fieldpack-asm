package com.ca.apm.swat.epaplugins.asm;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ca.apm.swat.epaplugins.asm.rules.Rule;

/**
 * Test class for testing the acct_credits API.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class RuleTest extends FileTest {


    /**
     * Test getFolders() without any properties.
     */
    @Test
    public void getFoldersAndRulesSimple() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            AsmReader.getProperties().setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
            AsmReader.getProperties().setProperty(SKIP_INACTIVE_MONITORS, FALSE);

            String[] folders = {"root_folder"};

            // load file
            accessor.loadFile(RULE_CMD, "target/test-classes/rule_get.json");

            // call API
            HashMap<String, List<Rule>> folderMap = requestHelper.getFoldersAndRules(folders);

            // folderMap should contain those entries
            String[] expectedRules = {
                "all_rules",
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

//            for (Iterator<String> fit = folderMap.keySet().iterator(); fit.hasNext(); ) {
//                String key = fit.next();
//                System.out.println("folder " + key);
//
//                for (Iterator<Rule> rit = folderMap.get(key).iterator(); rit.hasNext(); ) {
//                    System.out.println("  " + rit.next().getName());
//                }
//            }

            // check
            checkRules(expectedRules, folderMap.get(folders[0]));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test getFolders() without any properties.
     */
    @Test
    public void getFoldersAndRulesFolder() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            AsmReader.getProperties().setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
            AsmReader.getProperties().setProperty(SKIP_INACTIVE_MONITORS, FALSE);

            String[] folders = {"Tests"};

            // load file
            accessor.loadFile(RULE_CMD, "target/test-classes/rule_get_folder.json");

            // call API
            HashMap<String, List<Rule>> folderMap = requestHelper.getFoldersAndRules(folders);

            // folderMap should contain those entries
            String[] expectedRules = {
                "all_rules",
                "Amazon.com",
                "Bad request test",
                "Simple HTTP validation test",
                "Simple HTTP validation test - fail",
                "Simple JMeter 2",
                "Simple JMeter recording"
            };

//            for (Iterator<String> fit = folderMap.keySet().iterator(); fit.hasNext(); ) {
//                String key = fit.next();
//                System.out.println("folder " + key);
//
//                for (Iterator<Rule> rit = folderMap.get(key).iterator(); rit.hasNext(); ) {
//                    System.out.println("  " + rit.next().getName());
//                }
//            }

            // check
            checkRules(expectedRules, folderMap.get(folders[0]));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test getFolders() with asm.skipInactiveMonitors=true.
     */
    @Test
    public void getFoldersAndRulesSkipInactive() {

        try {
            // set properties
            AsmReader.getProperties().setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            AsmReader.getProperties().setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);
            AsmReader.getProperties().setProperty(SKIP_INACTIVE_MONITORS, TRUE);

            String[] folders = {"Tests"};

            // load file
            accessor.loadFile(RULE_CMD, "target/test-classes/rule_get_folder.json");

            // call API
            HashMap<String, List<Rule>> folderMap = requestHelper.getFoldersAndRules(folders);

            // folderMap should contain those entries
            String[] expectedRules = {
                "Amazon.com",
                "Bad request test",
                "Simple HTTP validation test",
                "Simple HTTP validation test - fail",
                "Simple JMeter recording"
            };

//            for (Iterator<String> fit = folderMap.keySet().iterator(); fit.hasNext(); ) {
//                String key = fit.next();
//                System.out.println("folder " + key);
//
//                for (Iterator<Rule> rit = folderMap.get(key).iterator(); rit.hasNext(); ) {
//                    System.out.println("  " + rit.next().getName());
//                }
//            }
           
            // check
            checkRules(expectedRules, folderMap.get(folders[0]));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Checks that all expected rules exist in the list.
     * @param expected array of expected strings
     * @param actual list of rules to check against
     */
    public void checkRules(String[] expected, List<Rule> actual) {

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
