package com.ca.apm.swat.epaplugins.asm.monitor;


public class AdvancedMonitor extends BaseMonitor {

    /**
     * Create a new advanced monitor.
     * @param type type of monitor
     * @param name name of the monitor
     * @param folder folder of the monitor
     * @param tags tags of the monitor
     * @param url
     * @param active
     */
    protected AdvancedMonitor(String type, String name,
                            String folder,
                            String[] tags,
                            String url,
                            boolean active) {
        super(name, type, folder, tags, url, active);

        // build chain of responsibility
        Handler harHandler = new HarHandler();
        Handler decoder = new InflatingBase64Decoder();
        Handler downloader = new AssetDownloader("har", "uri");
        decoder.setSuccessor(downloader);
        downloader.setSuccessor(harHandler);
        setSuccessor(decoder);
    }
}
