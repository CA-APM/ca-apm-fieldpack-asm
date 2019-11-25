
package com.ca.apm.swat.epaplugins.asm.monitor;

import com.ca.apm.swat.epaplugins.asm.error.AsmException;
import com.ca.apm.swat.epaplugins.utils.AsmMessages;
import com.wily.introscope.epagent.EpaUtils;
import com.wily.util.feedback.Module;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

public class AssetDownloader extends AbstractHandler {
    private final String path;
    private final String key;

    /**
     * AssetDownloader constructor.
     * @param successor successor
     * @param path download path
     * @param key key top extract
     */
    public AssetDownloader(Handler successor, String path, String key) {
        super(successor);
        this.path = path;
        this.key = key;        
    }

    /**
     * Generate metrics from API call result.
     * AssetDownloader replaces a reference to an asset with it's actual content
     * 
     * @param map map to insert metrics into
     * @param string a string
     * @param metricTree metric tree prefix
     * @param API endpoint where the request came from
     * @return metricMap map containing the metrics
     * @throws AsmException error during metrics generation
     */
    public Map<String, String> generateMetrics(Map<String, String> map,
                                               String string,
                                               String metricTree,
                                               String endpoint) throws AsmException {
        Module module = new Module(Thread.currentThread().getName());

        // doesn't make sense if nobody handles the result
        if (null != getSuccessor()) {
            
            BufferedReader in = null;
            
            try {
                String url = new JSONObject(string).getJSONObject(path).getString(key); 
                in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                StringBuilder sb = new StringBuilder();
                String str;
                while ((str = in.readLine()) != null) {
                    sb.append(str);
                }
                string = sb.toString();

            } catch (JSONException ex) {
                // assume the string represents an asset, and pass it on unchanged
            } catch (Exception ex) {
                EpaUtils.getFeedback().warn(module, AsmMessages.getMessage(
                        AsmMessages.ASSET_DOWNLOAD_ERROR_714, 
                        this.getClass().getSimpleName(), 
                        ex.getMessage()
                    ));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }

            return getSuccessor().generateMetrics(map, string, metricTree, endpoint);

        } else {
            EpaUtils.getFeedback().error(module, AsmMessages.getMessage(
                    AsmMessages.INVALID_HANDLER_CHAIN_910, 
                    this.getClass().getSimpleName()
                ));
        }
        return map;
    }
}
