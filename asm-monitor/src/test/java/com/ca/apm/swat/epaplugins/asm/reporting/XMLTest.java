package com.ca.apm.swat.epaplugins.asm.reporting;

import org.junit.Before;


public class XMLTest extends WriterTestBase {

  @Before
  public void setup() {
      writer = new XMLMetricWriter(System.out);
      metricPrefix = "XMLTest";
  }

}
