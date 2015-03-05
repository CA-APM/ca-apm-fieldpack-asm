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
