package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.asm.har.Page;
import com.ca.apm.swat.epaplugins.asm.har.json.JSONHar;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.ca.apm.swat.epaplugins.asm.har.Log;

public class HarHandler implements Handler, AsmProperties {

    private static final Module module = new Module("Asm.monitor.HarHandler");
    protected Handler successor = null;  

    @Override
    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

    /**
     * Generate metrics from API call result. 
     * @param metricMap map to insert metrics into
     * @param harString
     * @param metricTree metric tree prefix
     * @return map containing the metrics
     */
    @Override
    public Map<String, String> generateMetrics(Map<String, String> metricMap,
                                               String harString,
                                               String metricTree) {

        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module,
                "HarHandler - harString = " + harString);
        }

        if (!harString.startsWith("{log\":\"")) {
            if (harString.startsWith(HAR_OR_LOG_TAG)) {
                // Do nothing - already have seen it.
                // and we don't need this log
            }
            return metricMap;
        }

        try {
            Log log = new JSONHar(new JSONObject(harString)).getLog();
            
            for(Page page : log.getPages()) {
                // TODO output metrics
            }
        } catch (JSONException ex) {
            EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                    AsmMessages.JSON_PARSING_ERROR_713,
                    this.getClass().getSimpleName()
                ));
        }

        return metricMap;
    }
}
