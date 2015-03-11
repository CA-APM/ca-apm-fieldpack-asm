package com.ca.apm.swat.epaplugins.asm.reporting;

import org.junit.Before;


public class TextTest extends WriterTestBase {


  @Before
  public void setup() {
    writer = new TextMetricWriter();
    metricPrefix = "TextTest";
  }

}
