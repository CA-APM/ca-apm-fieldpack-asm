package com.ca.apm.swat.epaplugins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.wily.introscope.epagent.PropertiesReader;


public class PropertiesUtils {
  private static Map<String, String> agentProperties = new HashMap<String, String>();

  public PropertiesUtils(String filename) throws Exception {
    try {
      getPropertiesFromFile(filename);
    } catch (Exception e) {
      System.err.println("Error: Could not initialize properties file: " + filename);
      throw new Exception(e);
    }
  }

  public void getPropertiesFromFile(String inFile) throws Exception {
    FileInputStream inStream = new FileInputStream(new File(inFile));
    BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

    String line = in.readLine();
    while ((line = in.readLine()) != null) {
      if ((line.length() == 0) || (line.charAt(0) == '#') || (!line.contains("=")))
        continue;
      String property = line.substring(0, line.indexOf('='));
      String propertyValue = line.substring(line.indexOf('=') + 1, line.length());
      agentProperties.put(property, propertyValue);
      PropertiesReader.getFeedback().debug("Property: " + property + "-" + propertyValue);
    }

    inStream.close();
    PropertiesReader.getFeedback().info("Finished reading properties");
  }

  public void setProperty(String propertyKey, String propertyValue) {
    agentProperties.put(propertyKey, propertyValue);
  }

  public String getProperty(String propertyKey) {
    String value = (String) agentProperties.get(propertyKey);
    try {
      if (value.length() == 0)
        value = "";
    } catch (NullPointerException e) {
      value = "";
    }

    return value;
  }

  public String getProperty(String propertyKey, String defaultValue) {
    String value = getProperty(propertyKey);

    if (value.length() == 0) {
      value = defaultValue;
    }

    return value;
  }

  public String[] getPropetiesArray(String propertyKey) {
    String propertyValue = getProperty(propertyKey);
    StringTokenizer tokenizer = new StringTokenizer(propertyValue, ",");
    List<String> elements = new ArrayList<String>();

    while (tokenizer.hasMoreElements()) {
      String element = (String) tokenizer.nextElement();
      elements.add(element.trim());
    }

    return (String[]) elements.toArray(EPAConstants.kNoStringArrayProperties);
  }
}
