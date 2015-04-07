package com.ca.apm.swat.epaplugins.asm.rules;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;

public class InflatingBase64Decoder implements Handler {

    protected Handler successor = null;  

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    /**
     * Generate metrics from API call result.
     * InflatingBase64Decoder decodes the Base64 encoded string and unzips it
     *   before forwarding the string to the next handler.
     * 
     * @param encodedString Base64 encoded string
     * @param metricTree metric tree prefix
     * @param properties plugin properties that control output format and filtering
     * @param checkpointMap map containing all checkpoints of App Synthetic Monitor
     * @return metricMap map containing the metrics
     */
    public HashMap<String, String> generateMetrics(String encodedString,
        String metricTree,
        Properties properties,
        HashMap<String, String> checkpointMap) {

        // doesn't make sense if nobody handles the result
        if (null != successor) {
            if (EpaUtils.getFeedback().isVerboseEnabled()) {
                EpaUtils.getFeedback().verbose("InflatingBase64Decoder start");
            }
            // decode base64
            byte[] decoded = Base64.decodeBase64(encodedString);
            if (decoded != null) {
                // decompress
                byte[] bytesDecompressed = decompress(decoded);
                if (bytesDecompressed != null) {
                    // convert to UTF8
                    String decodedString;
                    try {
                        decodedString = new String(bytesDecompressed, 0,
                            bytesDecompressed.length, AsmProperties.UTF8);
                    } catch (UnsupportedEncodingException e) {
                        // should not happen: UTF8 should be supported
                        String errorMessage = AsmMessages.getMessage(AsmMessages.RUN_ERROR,
                            AsmProperties.ASM_PRODUCT_NAME,
                            this.getClass(),
                            e.getMessage());
                        EpaUtils.getFeedback().error(errorMessage);
                        throw new Error(errorMessage, e);
                    }

                    // call next handler in chain
                    return successor.generateMetrics(decodedString,
                        metricTree, properties, checkpointMap);
                } else {
                    EpaUtils.getFeedback().warn("InflatingBase64Decoder decompress == null!");
                }
            } else {
                EpaUtils.getFeedback().warn("InflatingBase64Decoder decoded == null!");
            }
        } else {
            EpaUtils.getFeedback().warn("InflatingBase64Decoder has no sucessor!");
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
            Inflater inflater = new Inflater();
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
