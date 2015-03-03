package com.wily.introscope.epagent;

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
      channel = new SystemOutFeedbackChannel("CEM EPA");
    } else {
      channel = EPAgent.GetFeedback();
    }

    return channel;
  }


}
