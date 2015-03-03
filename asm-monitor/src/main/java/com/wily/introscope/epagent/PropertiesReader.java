package com.wily.introscope.epagent;

import com.ca.apm.swat.epaplugins.utils.EPAConstants;
import com.wily.introscope.epagent.EPAgent;
import com.wily.util.feedback.IModuleFeedbackChannel;
import com.wily.util.feedback.SystemOutFeedbackChannel;


/**
 * Reads the application properties from the property file
 * 
 * @author Andreas Reiss - CA Wily Professional Service
 *
 */
public class PropertiesReader {


  /**
   * @return the Logging component
   */
  public static IModuleFeedbackChannel getFeedback() {
    IModuleFeedbackChannel channel;
    if (EPAgent.GetInstance() == null) {
      channel = new SystemOutFeedbackChannel(EPAConstants.apmcmProductNameShort + " EPA");
    } else {
      channel = EPAgent.GetFeedback();
    }

    return channel;
  }


}
