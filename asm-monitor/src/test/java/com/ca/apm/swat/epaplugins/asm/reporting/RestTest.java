package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RestTest extends WriterTestBase {

  public final static String EPA_URL = "http://localhost:8888/apm/metricFeed";

  @Before
  public void setup() {
    try {
      URL url = new URL(EPA_URL);
      writer = new RestfulMetricWriter(url);
      metricPrefix = "RestTest";
    } catch (MalformedURLException e) {
      Assert.fail(e.getMessage());
    }
  }


  /**
   * test only works for {RestfulMetricWriter}
   */
  @Test
  public void invalidMetricName() {
    try {
      // invalid metric name
      writer.writeIntCounter(metricPrefix + "|Invalid|Metric|Name", 1234);
      writer.flushMetrics();
      Assert.fail("didn't get wrong metric error");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("HTTP response code: 409"));
    }
  }

  /**
   * test only works for {RestfulMetricWriter}
   */
  @Test
  public void invalidMetricType() {
    try {
      // invalid metric type
      writer.writeMetric("invalid type", metricPrefix + "|Sample Data|invalid:invalid", 1234);
      writer.flushMetrics();

      Assert.fail("didn't get wrong metric error");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("HTTP response code: 409"));
    }
  }

  /**
   * test only works for {RestfulMetricWriter}
   */
  @Test
  public void invalidMetricValue() {
    // invalid metric value
    try {
      writer.writeMetric("invalid type", metricPrefix + "|Sample Data|invalid:invalid", 1234);
      writer.flushMetrics();
      
      Assert.fail("didn't get wrong metric error");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("HTTP response code: 409"));
    }
  }


  /**
   * test requires to be run as EPA Java Plugin for other writers
   */
  @Test
  public void errorDetectorTest() {
    writer.writeErrorDetectorEntry("Error","Resource");
    try {
      writer.flushMetrics();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

}
