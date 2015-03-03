package com.ca.apm.swat.epaplugins.utils;

public abstract interface StringFilter
{
  public static final StringFilter VOID = new StringFilter() {
    public String filter(String input) {
      return input;
    }
  };

  public static final StringFilter INVALID = new StringFilter() {
    public String filter(String input) {
      throw new Error("Must not be reached");
    }
  };

  public abstract String filter(String paramString);
}

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.StringFilter
 * JD-Core Version:    0.6.0
 */