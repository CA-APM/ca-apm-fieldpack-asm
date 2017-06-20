package com.ca.apm.swat.epaplugins.asm.monitor;


public class AdvancedMonitor extends BaseMonitor {
    private static final Handler DECODER;

    static {
        // build chain of responsibility
        Handler harHandler = new HarHandler();
        Handler downloader = new AssetDownloader(harHandler, "har", "uri");
        DECODER = new InflatingBase64Decoder(downloader);
    }

    /**
     * Create a new advanced monitor.
     * @param type type of monitor
     * @param name name of the monitor
     * @param folder folder of the monitor
     * @param tags tags of the monitor
     * @param url URL (unused) url this monitor is following
     * @param active activation state of this monitor
     */
    protected AdvancedMonitor(String type, String name,
                            String folder,
                            String[] tags,
                            String url,
                            boolean active) {
        super(DECODER, name, type, folder, tags, url, active);
    }
}
