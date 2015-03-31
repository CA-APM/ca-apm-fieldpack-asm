package com.wily.introscope.epagent;

import com.ca.apm.swat.epaplugins.utils.ASMProperties;
import com.wily.util.feedback.IModuleFeedbackChannel;
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
            channel = new SystemOutFeedbackChannel(ASMProperties.APMCM_PRODUCT_NAME_SHORT + " EPA");
        } else {
            channel = EPAgent.GetFeedback();
        }

        return channel;
    }


}
