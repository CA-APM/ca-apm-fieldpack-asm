package com.ca.apm.swat.epaplugins.asm.monitor;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;
import java.util.Map;

public class SimpleBase64Decoder implements Handler {

    protected Handler successor = null;  

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    /**
     * Generate metrics from API call result.
     * SimpleBase64Decoder decodes the Base64 encoded string and forwards to the next handler.
     * 
     * @param map map to insert metrics into
     * @param encodedString Base64 encoded string
     * @param metricTree metric tree prefix
     * @return metricMap map containing the metrics
     * @throws AsmException error during metrics generation
     */
    public Map<String, String> generateMetrics(Map<String, String> map,
                                               String encodedString,
                                               String metricTree)
            throws AsmException {

        // doesn't make sense if nobody handles the result
        if (null != successor) {
            if (EpaUtils.getFeedback().isDebugEnabled()) {
                EpaUtils.getFeedback().debug(AsmMessages.getMessage(
                    AsmMessages.METHOD_FOR_FOLDER_306,
                    this.getClass().getSimpleName()));
            }
            // decode base64
            byte[] decoded = Base64.decodeBase64(encodedString);
            if (decoded != null) {
                // convert to UTF8
                String decodedString;
                try {
                    decodedString = new String(decoded, 0,
                        decoded.length, EpaUtils.getEncoding());
                } catch (UnsupportedEncodingException e) {
                    // should not happen: UTF8 should be supported
                    String errorMessage = AsmMessages.getMessage(AsmMessages.RUN_ERROR_904,
                        AsmProperties.ASM_PRODUCT_NAME,
                        this.getClass(),
                        e.getMessage());
                    EpaUtils.getFeedback().error(errorMessage);
                    throw new Error(errorMessage, e);
                }

                // call next handler in chain
                return successor.generateMetrics(map, decodedString, metricTree);
            } else {
                EpaUtils.getFeedback().warn(AsmMessages.getMessage(
                    AsmMessages.BYTES_DECODED_NULL_909,
                    this.getClass().getSimpleName(),
                    metricTree));
            }
        } else {
            EpaUtils.getFeedback().warn(AsmMessages.getMessage(
                AsmMessages.INVALID_HANDLER_CHAIN_910,
                this.getClass().getSimpleName()));
        }
        return map;
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
