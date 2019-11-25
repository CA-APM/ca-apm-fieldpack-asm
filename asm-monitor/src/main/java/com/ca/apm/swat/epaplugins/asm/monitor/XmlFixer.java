
package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;
import java.util.Map;

public class XmlFixer extends AbstractHandler {

    public XmlFixer(Handler successor) {
        super(successor);
    }

    /**
     * Generate metrics from API call result.
     * XmlFixer replaces all '&' characters in xml string with "&amp;"
     * 
     * @param map to insert metrics into
     * @param xmlString xml string
     * @param metricTree metric tree prefix
     * @param API endpoint where the request came from
     * @return metricMap map containing the metrics
     * @throws AsmException error during metrics generation
     */
    public Map<String, String> generateMetrics(Map<String, String> map,
                                               String xmlString,
                                               String metricTree,
                                               String endpoint) throws AsmException {
        Module module = new Module(Thread.currentThread().getName());

        // doesn't make sense if nobody handles the result
        if (null != getSuccessor()) {
            if (EpaUtils.getFeedback().isVerboseEnabled(module)) {
                EpaUtils.getFeedback().verbose(module, AsmMessages.getMessage(
                    AsmMessages.METHOD_FOR_FOLDER_306,
                    this.getClass().getSimpleName(),
                    metricTree));
            }
            // replace all occu
            return getSuccessor().generateMetrics(map,
                                                  xmlString.replaceAll("&", "&amp;"),
                                                  metricTree, endpoint);

        } else {
            EpaUtils.getFeedback().error(module, AsmMessages.getMessage(
                AsmMessages.INVALID_HANDLER_CHAIN_910,
                this.getClass().getSimpleName()));
        }
        return map;
    }
}
