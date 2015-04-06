package com.ca.apm.swat.epaplugins.asm.reporting;

import org.junit.Before;


public class XmlTest extends WriterTestBase {

    /**
     * Set up test environment.
     */
    @Before
    public void setup() {
        writer = new XmlMetricWriter(System.out);
        metricPrefix = "XMLTest";
    }

}
