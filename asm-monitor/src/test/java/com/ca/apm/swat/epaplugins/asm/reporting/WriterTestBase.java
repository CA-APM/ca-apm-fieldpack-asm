package com.ca.apm.swat.epaplugins.asm.reporting;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class WriterTestBase {

  /**
   * must be set by implementors
   */
  protected MetricWriter writer = null;

  /**
   * must be set by implementors
   */
  protected String metricPrefix;

  @Before
  public abstract void setup();

  @Test
  public void restTest1() {
    writer.writeIntCounter(metricPrefix + "|Sample Data1|Ints:IntCounter", 1234);

    writer.writeIntAverage(metricPrefix + "|Sample Data1|Ints:IntAverage", 3);
    writer.writeIntAverage(metricPrefix + "|Sample Data1|Ints:IntAverage", 4);
    writer.writeIntAverage(metricPrefix + "|Sample Data1|Ints:IntAverage", 8);

    writer.writeLongCounter(metricPrefix + "|Sample Data1|Longs:LongCounter", 12345678L);

    writer.writeLongAverage(metricPrefix + "|Sample Data1|Longs:LongAverage", 30000L);
    writer.writeLongAverage(metricPrefix + "|Sample Data1|Longs:LongAverage", 40000L);
    writer.writeLongAverage(metricPrefix + "|Sample Data1|Longs:LongAverage", 80000L);

    writer.writeStringMetric(metricPrefix + "|Sample Data1|Strings:StringEvent", "The quick brown fox jumps over the lazy dog");

    try {
      writer.flushMetrics();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void restTest2() {
    writer.writeMetric(MetricWriter.kIntCounter, "Sample Data2|Ints:IntCounter", 1234);
    writer.writeMetric(MetricWriter.kLongCounter, "Sample Data2|Longs:LongCounter", 12345678L);
    writer.writeErrorMessage("Something went terribly wrong!!!");

    writer.writeMetric(MetricWriter.kIntCounter, "Sample Data2|Ints:Float", 12.34f);
    writer.writeStringMetric(metricPrefix + "|Sample Data2|Strings:StringEvent", "The quick brown fox jumps over the lazy dog");
    writer.writeIntRate(metricPrefix + "|Sample Data2|Ints:IntRate", 30);

    writer.writePerIntervalCounter(metricPrefix + "|Sample Data2|Ints:PerIntervalCounter", 1234);

    writer.writeTimestamp(metricPrefix + "|Sample Data2|Timestamp:Timestamp", new Date());

    writer.writeIntCounterForceExist(metricPrefix + "|Sample Data2|Ints:IntCounterForceExists", 1234);

    try {
      writer.flushMetrics();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}