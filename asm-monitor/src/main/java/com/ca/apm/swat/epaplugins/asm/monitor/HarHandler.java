package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.asm.har.Page;
import com.ca.apm.swat.epaplugins.asm.har.json.JsonHar;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;
import java.util.Map;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;
import com.ca.apm.swat.epaplugins.asm.har.Assertion;
import com.ca.apm.swat.epaplugins.asm.har.Entry;
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
     * 
     * @param metricMap
     *            map to insert metrics into
     * @param harString
     *            string representing har file
     * @param metricTree
     *            metric tree prefix
     * @return map containing the metrics
     */
    @Override
    public Map<String, String> generateMetrics(
            Map<String, String> metricMap, String harString, String metricTree) {

        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "HarHandler - harString = " + harString);
        }

        if (!harString.startsWith("{\"log\":")) {
            if (harString.startsWith(HAR_OR_LOG_TAG)) {
                // Do nothing - already have seen it.
                // and we don't need this log
            }

            return metricMap;
        }

        try {

            JsonHar har = new JsonHar(new JSONObject(harString));

            int step = 0;
            Formatter format = Formatter.getInstance();

            for (Page page : har.getLog().getPages()) {

                String metric;
                String label;

                label = page.getTitle();

                // report metrics
                metric = EpaUtils.fixMetricName(
                        metricTree + METRIC_PATH_SEPARATOR 
                        + format.formatStep(++step, label) + METRIC_NAME_SEPARATOR);

                if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                    EpaUtils.getFeedback().debug(module, "METRIC: " + metric);
                }

                int assertionFailuresCount = 0;

                for (Assertion assertion : page.get_assertions()) {
                    if (assertion.getError()) {
                        assertionFailuresCount++;
                    }
                }

                addMetric(metricMap, metric 
                        + ASSERTION_ERRORS, Integer.toString(assertionFailuresCount));
                addMetric(metricMap, metric 
                        + METRIC_NAME_LOAD_TIME, getPageMetric(page, METRIC_NAME_LOAD_TIME));
                addMetric(metricMap, metric + METRIC_NAME_CONTENT_LOAD_TIME,
                        getPageMetric(page, METRIC_NAME_CONTENT_LOAD_TIME));
                addMetric(metricMap, metric 
                        + ASSERTION_ERRORS, Integer.toString(assertionFailuresCount));

                for (Entry entry : har.getLog().getEntries(page.getId())) {

                    String entryMetric = EpaUtils
                            .fixMetricName(metricTree 
                                    + METRIC_PATH_SEPARATOR + format.formatStep(step, label)
                                    + METRIC_PATH_SEPARATOR + entry.getRequest().getUrl() 
                                    + METRIC_NAME_SEPARATOR);

                    if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                        EpaUtils.getFeedback().debug(module, "METRIC: " + metric);
                    }

                    addMetric(metricMap, entryMetric + METRIC_NAME_TOTAL_TIME,
                            getEntryMetric(entry, METRIC_NAME_TOTAL_TIME));
                    addMetric(metricMap, entryMetric + METRIC_NAME_RESPONSE_HEADER_SIZE,
                            getEntryMetric(entry, METRIC_NAME_RESPONSE_HEADER_SIZE));
                    addMetric(metricMap, entryMetric + METRIC_NAME_RESPONSE_BODY_SIZE,
                            getEntryMetric(entry, METRIC_NAME_RESPONSE_BODY_SIZE));
                    addMetric(metricMap, entryMetric + METRIC_NAME_RECEIVE_TIME,
                            getEntryMetric(entry, METRIC_NAME_RECEIVE_TIME));
                    addMetric(metricMap, entryMetric + METRIC_NAME_SEND_TIME,
                            getEntryMetric(entry, METRIC_NAME_SEND_TIME));
                    addMetric(metricMap, entryMetric + METRIC_NAME_CONNECT_TIME,
                            getEntryMetric(entry, METRIC_NAME_CONNECT_TIME));
                    addMetric(metricMap, entryMetric + METRIC_NAME_DNS_TIME,
                            getEntryMetric(entry, METRIC_NAME_DNS_TIME));
                    addMetric(metricMap, entryMetric + METRIC_NAME_BLOCKED_TIME,
                            getEntryMetric(entry, METRIC_NAME_BLOCKED_TIME));
                    addMetric(metricMap, entryMetric + METRIC_NAME_WAIT_TIME,
                            getEntryMetric(entry, METRIC_NAME_WAIT_TIME));
                }

            }

            return metricMap;

        } catch (JSONException ex) {
            EpaUtils.getFeedback().warn(module,
                    AsmMessages.getMessage(
                            AsmMessages.JSON_PARSING_ERROR_713, this.getClass().getSimpleName()));
        } catch (Exception e) {
            e.printStackTrace();
            EpaUtils.getFeedback().warn(module, "other exception");
        }

        return metricMap;
    }

    private String getPageMetric(Page page, String metricName) {
        try {
            if (metricName == METRIC_NAME_LOAD_TIME) {
                return Integer.toString(page.getPageTimings().getOnLoad());
            } else if (metricName == METRIC_NAME_CONTENT_LOAD_TIME) {
                return Integer.toString(page.getPageTimings().getOnContentLoad());
            }
        } catch (NoSuchElementException e) {
            EpaUtils.getFeedback().debug(module, "Har doesn't contain Entry metric " + metricName);
        } catch (JSONException ex) {
            EpaUtils.getFeedback().debug(module, "Har doesn't contain Entry metric " + metricName);
        }
        return null;
    }

    private String getEntryMetric(Entry entry, String metricName) {
        try {

            if (metricName == METRIC_NAME_TOTAL_TIME) {
                return Integer.toString(entry.getTime());
            } else if (metricName == METRIC_NAME_RESPONSE_HEADER_SIZE) {
                return Integer.toString(entry.getResponse().getHeadersSize());
            } else if (metricName == METRIC_NAME_RESPONSE_BODY_SIZE) {
                return Integer.toString(entry.getResponse().getBodySize());
            } else if (metricName == METRIC_NAME_RECEIVE_TIME) {
                return Integer.toString(entry.getTimings().getReceive());
            } else if (metricName == METRIC_NAME_SEND_TIME) {
                return Integer.toString(entry.getTimings().getSend());
            } else if (metricName == METRIC_NAME_CONNECT_TIME) {
                return Integer.toString(entry.getTimings().getConnect());
            } else if (metricName == METRIC_NAME_DNS_TIME) {
                return Integer.toString(entry.getTimings().getDns());
            } else if (metricName == METRIC_NAME_BLOCKED_TIME) {
                return Integer.toString(entry.getTimings().getBlocked());
            } else if (metricName == METRIC_NAME_WAIT_TIME) {
                return Integer.toString(entry.getTimings().getWait());
            }
        } catch (NoSuchElementException e) {
            EpaUtils.getFeedback().debug(module, "Har doesn't contain Entry metric " + metricName);
        } catch (JSONException ex) {
            EpaUtils.getFeedback().debug(module, "Har doesn't contain Entry metric " + metricName);
        }
        return null;
    }

    private void addMetric(Map<String, String> metricMap, String metricName, String value) {
        if (value != null) {
            metricMap.put(metricName, value);
        }
    }
}
