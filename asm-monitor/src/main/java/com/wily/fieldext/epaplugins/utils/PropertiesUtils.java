/*    */ package com.wily.fieldext.epaplugins.utils;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.InputStreamReader;
/*    */ import java.util.ArrayList;
/*    */ import java.util.HashMap;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import java.util.StringTokenizer;
/*    */ 
/*    */ 
/*    */ public class PropertiesUtils
/*    */ {
/* 16 */   private static Map<String, String> agentProperties = new HashMap<String, String>();
/*    */ 
/*    */   public PropertiesUtils(String filename) throws Exception {
/*    */     try {
/* 20 */       getPropertiesFromFile(filename);
/*    */     } catch (Exception e) {
/* 22 */       System.err.println("Error: Could not initialize properties file: " + filename);
/* 23 */       throw new Exception(e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void getPropertiesFromFile(String inFile) throws Exception {
/* 28 */     FileInputStream inStream = new FileInputStream(new File(inFile));
/* 29 */     BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
/*    */ 
/* 31 */     String line = in.readLine();
/* 32 */     while ((line = in.readLine()) != null) {
/* 33 */       if ((line.length() == 0) || 
/* 34 */         (line.charAt(0) == '#') || (!line.contains("="))) continue;
/* 35 */       agentProperties.put(line.substring(0, line.indexOf('=')), 
/* 36 */         line.substring(line.indexOf('=') + 1, line.length()));
/*    */     }
/*    */ 
/* 40 */     inStream.close();
/*    */   }
/*    */ 
/*    */   public void setProperty(String propertyKey, String propertyValue) {
/* 44 */     agentProperties.put(propertyKey, propertyValue);
/*    */   }
/*    */ 
/*    */   public String getProperty(String propertyKey) {
/* 48 */     String value = (String)agentProperties.get(propertyKey);
/*    */     try {
/* 50 */       if (value.length() == 0)
/* 51 */         value = "";
/*    */     }
/*    */     catch (NullPointerException e) {
/* 54 */       value = "";
/*    */     }
/*    */ 
/* 57 */     return value;
/*    */   }
/*    */ 
/*    */   public String getProperty(String propertyKey, String defaultValue) {
/* 61 */     String value = getProperty(propertyKey);
/*    */ 
/* 63 */     if (value.length() == 0) {
/* 64 */       value = defaultValue;
/*    */     }
/*    */ 
/* 67 */     return value;
/*    */   }
/*    */ 
/*    */   public String[] getPropetiesArray(String propertyKey) {
/* 71 */     String propertyValue = getProperty(propertyKey);
/* 72 */     StringTokenizer tokenizer = new StringTokenizer(propertyValue, ",");
/* 73 */     List<String> elements = new ArrayList<String>();
/*    */ 
/* 75 */     while (tokenizer.hasMoreElements()) {
/* 76 */       String element = (String)tokenizer.nextElement();
/* 77 */       elements.add(element.trim());
/*    */     }
/*    */ 
/* 80 */     return (String[])elements.toArray(EPAConstants.kNoStringArrayProperties);
/*    */   }
/*    */ }

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.PropertiesUtils
 * JD-Core Version:    0.6.0
 */