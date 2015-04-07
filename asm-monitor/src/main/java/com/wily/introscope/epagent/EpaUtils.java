package com.wily.introscope.epagent;

import java.util.regex.Pattern;

import com.ca.apm.swat.epaplugins.asm.error.InitializationError;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.ca.apm.swat.epaplugins.utils.StringFilter;
import com.ca.apm.swat.epaplugins.utils.TextNormalizer;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.feedback.SeverityLevel;
import com.wily.util.feedback.SystemOutFeedbackChannel;


/**
 * EPAgent utilities.
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
public class EpaUtils {

    private EpaUtils() {
    }

    /**
     * Get the logging component.
     * @return the logging component
     */
    public static IModuleFeedbackChannel getFeedback() {
        IModuleFeedbackChannel channel;
        if (EPAgent.GetInstance() == null) {
            channel = new SystemOutFeedbackChannel(AsmProperties.ASM_PRODUCT_NAME_SHORT + " EPA",
                SeverityLevel.INFO);
        } else {
            channel = EPAgent.GetFeedback();
        }

        return channel;
    }

    /**
     * Replace unsupported characters in metric name.
     * @param rawMetric raw metric name
     * @return cleansed metric name
     */
    public static String fixMetric(String rawMetric) {
        StringFilter normalizer = null;
        try {
            normalizer = TextNormalizer.getNormalizationStringFilter();
        } catch (ClassNotFoundException e) {
            EpaUtils.getFeedback().error(e.getMessage());
            throw new InitializationError(
                AsmMessages.getMessage(AsmMessages.NORMALIZER_INFO, e.getMessage()));
        }

        String metricKeyNormalized = normalizer.filter(rawMetric);
        Pattern pattern = Pattern.compile(AsmProperties.JSON_PATTERN);
        String fixedMetric =
                pattern.matcher(metricKeyNormalized).replaceAll(AsmProperties.EMPTY_STRING);

        return fixedMetric.replace("\\", "-")
                //.replace("/", "-")
                .replace(",", "_")
                .replace(";", "-")
                .replace("&", "and");
    }
}
