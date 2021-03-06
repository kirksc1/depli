package com.depli.store.cache.descriptor;

import static java.lang.Float.NaN;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * Memory Usage Data
 *
 * Representation of {@link ManagementFactory#getMemoryMXBean#MemoryUsage} class data in mega bytes.
 *
 * @author Lahiru Pathirage
 * @since 3/28/17
 */

public class MemoryUsageData {

  private float init;
  private float used;
  private float committed;
  private float max;

  public MemoryUsageData() {
    this.init = NaN;
    this.used = NaN;
    this.committed = NaN;
    this.max = -NaN;
  }

  private static float toFloat(long value) {
    if (value == -1) {
      return NaN;
    }

    return Math.round((value / (1024f * 1024f)) * 10f) / 10f;
  }

  public float getCommitted() {
    return committed;
  }

  public void setCommitted(float committed) {
    this.committed = committed;
  }

  public float getInit() {
    return init;
  }

  public void setInit(float init) {
    this.init = init;
  }

  public float getMax() {
    return max;
  }

  public void setMax(float max) {
    this.max = max;
  }

  public float getUsed() {
    return used;
  }

  public void setUsed(float used) {
    this.used = used;
  }

  public void setDynamicData(MemoryUsage memoryUsage) {
    this.init = toFloat(memoryUsage.getInit());
    this.used = toFloat(memoryUsage.getUsed());
    this.committed = toFloat(memoryUsage.getCommitted());
    this.max = toFloat(memoryUsage.getMax());
  }
}
