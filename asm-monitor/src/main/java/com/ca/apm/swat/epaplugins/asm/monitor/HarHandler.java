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
import com.ca.apm.swat.epaplugins.asm.format.Formatter;

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


        EpaUtils.getFeedback().info("_RO_ HarHandler generateMetrics (" + metricTree + ", "
                + harString + ")");


        if (!harString.startsWith("{\"log\":")) {
            if (harString.startsWith(HAR_OR_LOG_TAG)) {
                // Do nothing - already have seen it.
                // and we don't need this log
            }
         EpaUtils.getFeedback().info("_RO_ HarHandler generateMetrics - breaking out because it starts with something other than log");

            return metricMap;
        }

        try {

                 EpaUtils.getFeedback().info("_RO_ HarHandler generateMetrics - getting log");
             JSONHar har = new JSONHar(new JSONObject(harString));
             EpaUtils.getFeedback().info("_RO_ HarHandler generateMetrics - got har");

             Log log = har.getLog();

             EpaUtils.getFeedback().info("_RO_ HarHandler generateMetrics - got log");


            int step = 1;
            Formatter format = Formatter.getInstance();


            for(Page page : log.getPages()) {
                 EpaUtils.getFeedback().info("_RO_ HarHandler generateMetrics - log page");
                String label, url;
                int onLoad;
                url = null;


                label = page.getTitle();
                onLoad = page.getPageTimings().getOnLoad();


                EpaUtils.getFeedback().info("HarHandler - label = " + label);

                 // report metrics
                String metric = EpaUtils.fixMetricName(metricTree + METRIC_PATH_SEPARATOR
                        + format.formatStep(step++, (label == null ? url : label)) + METRIC_NAME_SEPARATOR);
                if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                    EpaUtils.getFeedback().debug(module, "METRIC: " + metric);
                }
                EpaUtils.getFeedback().info("_RO_ HarHandler metric add  metric=" + metric + "onLoad=" + onLoad );

                metricMap.put(metric + "Load Time",    Integer.toString(onLoad));

                return metricMap;


            }
        } catch (JSONException ex) {
            EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                    AsmMessages.JSON_PARSING_ERROR_713,
                    this.getClass().getSimpleName()
                ));
        } catch (Exception e) {
                e.printStackTrace();
                EpaUtils.getFeedback().warn(module, "other exception");
        }

        return metricMap;
    }
}
