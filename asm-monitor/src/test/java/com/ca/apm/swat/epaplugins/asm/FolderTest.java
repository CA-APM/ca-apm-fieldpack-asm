package com.ca.apm.swat.epaplugins.asm;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.wily.introscope.epagent.EpaUtils;

/**
 * Test class for testing the fldr_get API.
 *   Run with -DDEBUG=true for debug output.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class FolderTest extends FileTest {


    /**
     * Test getFolders() without any properties.
     */
    @Test
    public void getFoldersSimple() {

        try {
            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(INCLUDE_FOLDERS, ALL_FOLDERS);
            props.setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);

            // call API
            String[] folderList = requestHelper.getFolders();

            // folderList should contain those entries
            String[] expectedFolders = {
                "root_folder",
                "APM Vendor Sites",
                "Caterpillar",
                "Customer Sites",
                "NML",
                "OPMS Testing",
                "Test folder name length",
                "Tests",
                "Web Service tests"
            };

            // check
            checkMetrics(expectedFolders, folderList);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test getFolders() with include property.
     */
    @Test
    public void getFoldersInclude() {

        try {
            // folderList should contain those entries
            String[] expectedFolders = {
                "APM Vendor Sites",
                "Caterpillar",
                "Customer Sites",
                "NML",
                "OPMS Testing",
                "Tests"
            };

            // create include property
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < expectedFolders.length; ++i) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(expectedFolders[i]);
            }

            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(INCLUDE_FOLDERS, buf.toString());
            props.setProperty(EXCLUDE_FOLDERS, EMPTY_STRING);

            // call API
            String[] folderList = requestHelper.getFolders();

            // check
            checkMetrics(expectedFolders, folderList);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test getFolders() with exclude property.
     */
    @Test
    public void getFoldersExclude() {

        try {
            // folderList should contain those entries
            String[] expectedFolders = {
                "root_folder",
                "APM Vendor Sites",
                "Caterpillar",
                "Customer Sites",
                "NML",
                "Tests"
            };

            String[] excludedFolders = {
                "OPMS Testing",
                "Test folder name length",
                "Web Service tests"
            };

            // create exclude property
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < excludedFolders.length; ++i) {
                if (i > 0) {
                    buf.append(',');
                    for (int j = 0; j < i; ++j) {
                        buf.append(' '); // append varying number of blanks
                    }
                }
                buf.append(excludedFolders[i]);
            }

            // set properties
            Properties props = EpaUtils.getProperties();
            props.setProperty(INCLUDE_FOLDERS, EMPTY_STRING);
            props.setProperty(EXCLUDE_FOLDERS, buf.toString());

            // call API
            String[] folderList = requestHelper.getFolders();

            // check
            checkMetrics(expectedFolders, folderList);
            checkNotExistMetrics(excludedFolders, folderList);

            if (DEBUG) {
                buf = new StringBuffer();
                for (int i = 0; i < folderList.length; ++i) {
                    if (i > 0) {
                        buf.append(',');
                    }
                    buf.append(folderList[i]);
                }
                System.out.println("folderList = " + buf.toString());             
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
