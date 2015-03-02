/*    */ package com.wily.fieldext.epaplugins.utils;
/*    */ 
/*    */ import java.io.ByteArrayInputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.OutputStream;
/*    */ import java.net.Authenticator;
/*    */ import java.net.HttpURLConnection;
/*    */ import java.net.URL;
/*    */ 
/*    */ 
/*    */ public class RESTClient
/*    */ {
/*    */   public RESTClient(String proxyHost, String proxyPort, String proxyUser, String proxyPassword)
/*    */   {
/* 16 */     if (proxyHost.length() != 0) {
/* 17 */       System.setProperty("http.proxyHost", proxyHost);
/* 18 */       System.setProperty("https.proxyHost", proxyHost);
/*    */     }
/* 20 */     if (proxyPort.length() != 0) {
/* 21 */       System.setProperty("http.proxyPort", proxyPort);
/* 22 */       System.setProperty("https.proxyPort", proxyPort);
/*    */     }
/* 24 */     if ((proxyUser.length() != 0) && (proxyPassword.length() != 0))
/* 25 */       Authenticator.setDefault(new ProxyAuthenticator(proxyUser, proxyPassword));
/*    */   }
/*    */ 
/*    */   public String request(boolean quiet, String method, URL url, String params)
/*    */     throws IOException
/*    */   {
/* 32 */     if (!quiet)
/*    */     {
/* 34 */       System.out.println("[issuing request: " + method + " " + url + "]");
/*    */     }
/*    */ 
/* 37 */     HttpURLConnection connection = (HttpURLConnection)url.openConnection();
/* 38 */     connection.setRequestMethod(method);
/*    */ 
/* 40 */     byte[] bytes = params.getBytes();
/* 41 */     InputStream body = new ByteArrayInputStream(bytes);
/*    */ 
/* 44 */     byte[] buffer = new byte[8192];
/* 45 */     int read = 0;
/* 46 */     if (body != null)
/*    */     {
/* 48 */       connection.setDoOutput(true);
/*    */ 
/* 50 */       OutputStream output = connection.getOutputStream();
/* 51 */       while ((read = body.read(buffer)) != -1)
/*    */       {
/* 53 */         output.write(buffer, 0, read);
/*    */       }
/*    */ 
/*    */     }
/*    */ 
/* 58 */     long time = System.currentTimeMillis();
/* 59 */     connection.connect();
/*    */ 
/* 61 */     InputStream responseBodyStream = connection.getInputStream();
/* 62 */     StringBuffer responseBody = new StringBuffer();
/* 63 */     while ((read = responseBodyStream.read(buffer)) != -1)
/*    */     {
/* 65 */       responseBody.append(new String(buffer, 0, read));
/*    */     }
/* 67 */     connection.disconnect();
/* 68 */     time = System.currentTimeMillis() - time;
/*    */ 
/* 71 */     if (!quiet) {
/* 72 */       System.out.println("[read " + responseBody.length() + " chars in " + time + "ms]");
/*    */     }
/*    */ 
/* 76 */     if (!quiet)
/*    */     {
/* 78 */       String header = null;
/* 79 */       String headerValue = null;
/* 80 */       int index = 0;
/* 81 */       while ((headerValue = connection.getHeaderField(index)) != null)
/*    */       {
/* 83 */         header = connection.getHeaderFieldKey(index);
/*    */ 
/* 85 */         if (header == null)
/* 86 */           System.out.println(headerValue);
/*    */         else {
/* 88 */           System.out.println(header + ": " + headerValue);
/*    */         }
/* 90 */         index++;
/*    */       }
/* 92 */       System.out.println("");
/*    */     }
/*    */ 
/* 95 */     System.out.flush();
/* 96 */     return responseBody.toString();
/*    */   }
/*    */ }

/* Location:           \\vmware-host\Shared Folders\Documents\1APM-Wily\EPA\CloudMonitor\APMCMAgent\lib\APMCMPlugin.jar
 * Qualified Name:     com.wily.fieldext.epaplugins.utils.RESTClient
 * JD-Core Version:    0.6.0
 */