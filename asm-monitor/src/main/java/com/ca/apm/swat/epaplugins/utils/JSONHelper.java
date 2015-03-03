package com.ca.apm.swat.epaplugins.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONHelper {
  
  public static String unpadJSON(String jsonWithPadding) throws Exception {
    String patternToMatch = "doCallback\\((.*)\\)([\n]*)";

    Pattern unpad = Pattern.compile(patternToMatch);
    Matcher matched = unpad.matcher(jsonWithPadding);

    if (matched.find()) {
      return matched.group(1);
    }
    return null;
  }

}
