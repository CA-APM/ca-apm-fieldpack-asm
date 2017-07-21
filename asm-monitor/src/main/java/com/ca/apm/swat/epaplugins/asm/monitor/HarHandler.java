package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.asm.har.Page;
import com.ca.apm.swat.epaplugins.asm.har.json.JsonHar;
import com.ca.apm.swat.epaplugins.asm.reporting.MetricMap;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.ca.apm.swat.epaplugins.utils.AsmProperties;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.StringUtils;
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
        
        try {

            JSONObject h1 = new JSONObject(harString);
            JSONObject h2 = h1.optJSONObject("har");

            JsonHar har = new JsonHar(h2 != null ? h2 : h1);

            int step = 1;

            for (Page page : har.getLog().getPages()) {

                String label = null;
                
//                if (EpaUtils.getBooleanProperty(REPORT_LABELS_IN_PATH, false)) {
//                    label = METRIC_PATH_SEPARATOR + page.getTitle();
//                }
//                else {
                label = page.getTitle();
//                }

                metricMap.putAll(reportPageMetrics(metricTree,page, step, label));
                /*
                for (Entry entry : har.getLog().getEntries(page.getId())) {
                    metricMap.putAll(reportEntryMetrics(metricTree,entry, step, label));
                }
                */
                ++step;
            }

            return metricMap;

        } catch (JSONException ex) {
            EpaUtils.getFeedback().warn(module,
                    AsmMessages.getMessage(
                            AsmMessages.JSON_PARSING_ERROR_713, this.getClass().getSimpleName()));
        } catch (Exception e) {
            e.printStackTrace();
            EpaUtils.getFeedback().warn(module, "Caught exception:" + e.getMessage());
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
    
        value = StringUtils.escapeHTMLSpecialCharacters(value);
    
        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "METRIC: " + metricName + ":" + value);
        }
        
        if (value != null) {
            metricMap.put(metricName, value);
        }
        
    }
    
    private Map<String, String> reportPageMetrics(
            String metricTree, Page page, int step, String label) {

        MetricMap metricMap = new MetricMap();
        Formatter format = Formatter.getInstance();
        // report metrics
        String metric = EpaUtils.fixMetricName(
                metricTree + METRIC_PATH_SEPARATOR 
                + format.formatStep(step, label) + METRIC_NAME_SEPARATOR);

        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "METRIC: " + metric);
        }
                
        int assertionFailuresCount = 0;
        int assertionNum = 1;
        
        for (Assertion assertion : page.get_assertions()) {
                                
            if (assertion.getError()) {
                assertionFailuresCount++;
            }
                                
            metricMap.putAll(
                   reportAssertionMetrics(metricTree,assertion, step, label, assertionNum));
            
            ++assertionNum;
        }
        
        addMetric(metricMap, metric 
                + ASSERTION_ERRORS, Integer.toString(assertionFailuresCount));
        addMetric(metricMap, metric 
                + METRIC_NAME_LOAD_TIME, getPageMetric(page, METRIC_NAME_LOAD_TIME));
        addMetric(metricMap, metric + METRIC_NAME_CONTENT_LOAD_TIME,
                getPageMetric(page, METRIC_NAME_CONTENT_LOAD_TIME));
        
        return metricMap;
    }
    
    private Map<String, String> reportAssertionMetrics(
            String metricTree, Assertion assertion, int step, String label, int insertionNum) {
        
        MetricMap metricMap = new MetricMap();
        Formatter format = Formatter.getInstance();
        
        String assertionName = assertion.getName();
        String assertionError = (assertion.getError() ? "1" : "0" );
        String assertionMessage = assertion.getMessage();
        
        if ( assertionName != null && ( assertionName.equals("Assertion") 
                || assertionName.equals("Assertion Failure") ) ) {
            assertionName = "Assertion " + insertionNum;
        }
    
        String metric = EpaUtils.fixMetricName(metricTree 
                   + METRIC_PATH_SEPARATOR + format.formatStep(step, label)
                   + METRIC_PATH_SEPARATOR + assertionName
                   + METRIC_NAME_SEPARATOR);
       
        addMetric(metricMap, metric + ASSERTION_ERRORS ,assertionError); 
        addMetric(metricMap, metric + ASSERTION_MESSAGE ,assertionMessage);
       
        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "ASSERTION METRIC: " 
                + metric + assertion.getMessage());
        }
        
        return metricMap;
    }
 
    
    private Map<String, String> reportEntryMetrics(
            String metricTree, Entry entry, int step, String label) {

        MetricMap metricMap = new MetricMap();
        Formatter format = Formatter.getInstance();

        String url = entry.getRequest().getUrl();
        String text = url;

        if ((null != text) && (0 < text.length())) {
            // lopal05: now normalize URL, we dont't want anything behind '?' as that may result
            // in metric tree explosion. Each new request creating new path / element.
            int indexOfChar = text.indexOf(";");
            if (indexOfChar > 0) {
                text = text.substring(0, indexOfChar);
            }
            indexOfChar = text.indexOf("?");
            if (indexOfChar > 0) {
                text = text.substring(0, indexOfChar);
            }

            url = text;
            if (EpaUtils.getFeedback().isDebugEnabled(module)) {
                EpaUtils.getFeedback().debug(module, "replaced URL '" + url
                        + "' with text '" + text + "'");
            }
        }
        
        String metric = EpaUtils
                .fixMetricName(metricTree 
                        + METRIC_PATH_SEPARATOR + format.formatStep(step, label)
                        + METRIC_PATH_SEPARATOR + text
                        + METRIC_NAME_SEPARATOR);

        if (EpaUtils.getFeedback().isDebugEnabled(module)) {
            EpaUtils.getFeedback().debug(module, "METRIC: " + metric);
        }

        addMetric(metricMap, metric + METRIC_NAME_TOTAL_TIME,
                getEntryMetric(entry, METRIC_NAME_TOTAL_TIME));
        addMetric(metricMap, metric + METRIC_NAME_RESPONSE_HEADER_SIZE,
                getEntryMetric(entry, METRIC_NAME_RESPONSE_HEADER_SIZE));
        addMetric(metricMap, metric + METRIC_NAME_RESPONSE_BODY_SIZE,
                getEntryMetric(entry, METRIC_NAME_RESPONSE_BODY_SIZE));
        addMetric(metricMap, metric + METRIC_NAME_RECEIVE_TIME,
                getEntryMetric(entry, METRIC_NAME_RECEIVE_TIME));
        addMetric(metricMap, metric + METRIC_NAME_SEND_TIME,
                getEntryMetric(entry, METRIC_NAME_SEND_TIME));
        addMetric(metricMap, metric + METRIC_NAME_CONNECT_TIME,
                getEntryMetric(entry, METRIC_NAME_CONNECT_TIME));
        addMetric(metricMap, metric + METRIC_NAME_DNS_TIME,
                getEntryMetric(entry, METRIC_NAME_DNS_TIME));
        addMetric(metricMap, metric + METRIC_NAME_BLOCKED_TIME,
                getEntryMetric(entry, METRIC_NAME_BLOCKED_TIME));
        addMetric(metricMap, metric + METRIC_NAME_WAIT_TIME,
                getEntryMetric(entry, METRIC_NAME_WAIT_TIME));
        
        return metricMap;
    }

    @Override
    public Handler getSuccessor() {
        return null;
    }
}