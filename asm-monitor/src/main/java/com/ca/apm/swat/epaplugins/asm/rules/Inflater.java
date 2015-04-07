package com.ca.apm.swat.epaplugins.asm.rules;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;
//import java.util.zip.Inflater;

import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

public class Inflater implements Handler {

    protected Handler successor = null;  

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    /**
     * Generate metrics from API call result.
     * Inflater unzips the compressed string and forwards to the next handler.
     * 
     * @param compressedString compressedString
     * @param metricTree metric tree prefix
     * @return metricMap map containing the metrics
     */
    public HashMap<String, String> generateMetrics(String compressedString, String metricTree) {

        // doesn't make sense if nobody handles the result
        if (null != successor) {
            byte[] bytesDecompressed = null;
            try {
                bytesDecompressed = decompress(compressedString.getBytes(AsmProperties.UTF8));
                if (bytesDecompressed != null) {
                    String decompressedString;
                    decompressedString = new String(bytesDecompressed, 0,
                        bytesDecompressed.length, AsmProperties.UTF8);
                    return successor.generateMetrics(decompressedString, metricTree);
                } else {
                    EpaUtils.getFeedback().error("bytesDecompressed is null!");
                }
            } catch (UnsupportedEncodingException e) {
                // should not happen: UTF8 should be supported
                String errorMessage = AsmMessages.getMessage(AsmMessages.RUN_ERROR,
                    AsmProperties.ASM_PRODUCT_NAME,
                    this.getClass(),
                    e.getMessage());
                EpaUtils.getFeedback().error(errorMessage);
                throw new Error(errorMessage, e);
            }
        } else {
            EpaUtils.getFeedback().error("Inflater has no sucessor!");
        }
        return new HashMap<String, String>();
    }

    /**
     * Inflate compressed data.
     * @param data compressed data
     * @return uncompressed data
     */
    public byte[] decompress(byte[] data) {
        try {
            java.util.zip.Inflater inflater = new java.util.zip.Inflater();
            inflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            byte[] output = outputStream.toByteArray();

            inflater.end();
            return output;
        } catch (Exception ex) {
            return null;
        }

    }

}
