package com.ca.apm.swat.epaplugins.asm.monitor;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
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
     * @throws AsmException error during metrics generation
     */
    public MetricMap generateMetrics(String compressedString, String metricTree)
            throws AsmException {

        // doesn't make sense if nobody handles the result
        if (null != successor) {
            byte[] bytesDecompressed = null;
            try {
                bytesDecompressed = decompress(compressedString.getBytes(EpaUtils.getEncoding()));
                if (bytesDecompressed != null) {
                    String decompressedString;
                    decompressedString = new String(bytesDecompressed, 0,
                        bytesDecompressed.length, EpaUtils.getEncoding());
                    return successor.generateMetrics(decompressedString, metricTree);
                } else {
                    EpaUtils.getFeedback().error(AsmMessages.getMessage(
                        AsmMessages.BYTES_DECODED_NULL_909,
                        this.getClass().getSimpleName(),
                        metricTree));
                }
            } catch (UnsupportedEncodingException e) {
                // should not happen: UTF8 should be supported
                String errorMessage = AsmMessages.getMessage(AsmMessages.RUN_ERROR_904,
                    AsmProperties.ASM_PRODUCT_NAME,
                    this.getClass(),
                    e.getMessage());
                EpaUtils.getFeedback().error(errorMessage);
                throw new Error(errorMessage, e);
            }
        } else {
            EpaUtils.getFeedback().error(AsmMessages.getMessage(
                AsmMessages.INVALID_HANDLER_CHAIN_910,
                this.getClass().getSimpleName()));
        }
        return new MetricMap();
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
