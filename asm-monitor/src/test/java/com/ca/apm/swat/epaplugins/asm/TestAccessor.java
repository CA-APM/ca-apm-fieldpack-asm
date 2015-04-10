package com.ca.apm.swat.epaplugins.asm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.ca.apm.swat.epaplugins.asm.error.LoginError;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

/**
 * TestAccessor mimics the real accessor but reads test responses from disk
 *   instead of calling the App Synthetic Monitor API.
 *
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class TestAccessor extends Accessor implements AsmProperties {

    public static final String FILE_EXTENSION = ".json";
    
    private HashMap<String, String> responses = null;
    private String filePrefix;

    /**
     * Creates a new TestAccessor.
     * @param filePrefix the path to prepend to files being loaded.
     */
    public TestAccessor(String filePrefix) {
        this.filePrefix = filePrefix;
        this.responses = new HashMap<String, String>();
    }

    /**
     * Mimics executing a call against the App Synthetic Monitor API.
     *   API responses are loaded from files. The default filename to 
     * @param callType API call
     * @param callParams parameters
     * @return unpadded API call result
     * @throws Exception if an error occurred,
     *     e.g. an error code like 1000 (authentication error) or
     *     1001 (call syntax error) was returned by the API call
     */
    public String executeApi(String callType, String callParams)
        throws Exception {
        
        if (!this.responses.containsKey(callType)) {
            loadFile(callType);
        }
        return responses.get(callType);
    }

    /**
     * Load a test response file from disk.
     * @param callType API call
     * @throws IOException if an error occurred reading the file
     */
    private void loadFile(String callType) throws IOException {
        String filename = this.filePrefix + callType + FILE_EXTENSION;
        loadFile(callType, filename);
    }

    /**
     * Load a test response file from disk.
     *   This method can be used to "preload" files for test cases.
     * @param callType API call that the file should be associated with
     * @param path full path of the file to be loaded
     * @throws IOException if an error occurred reading the file
     */
    public void loadFile(String callType, String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            String contents = readFile(path, EpaUtils.getEncoding());
            //System.out.println(contents);
            
            // remove "doCallback(" from contents
            String unpadded = Accessor.unpadJson(contents);
            //System.out.println(unpadded);
            
            if (null == unpadded) {
                throw new IOException("error reading or converting file " + path);
            }
            
            // put the response in the map
            this.responses.put(callType, unpadded);
        }
    }

    /**
     * Read a file from disk and return its contents as String.
     * @param path file path
     * @param encoding encoding of the file contents
     * @return file contents
     * @throws IOException if an error occurred reading the file
     */
    static String readFile(String path, String encoding) 
        throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public String login() throws LoginError, Exception {
        throw new UnsupportedOperationException("login()");
    }

}
