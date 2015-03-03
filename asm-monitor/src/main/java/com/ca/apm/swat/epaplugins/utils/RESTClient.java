package com.ca.apm.swat.epaplugins.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class RESTClient {
  public RESTClient(String proxyHost, String proxyPort, String proxyUser, String proxyPassword) {
    if (proxyHost.length() != 0) {
      System.setProperty("http.proxyHost", proxyHost);
      System.setProperty("https.proxyHost", proxyHost);
    }
    if (proxyPort.length() != 0) {
      System.setProperty("http.proxyPort", proxyPort);
      System.setProperty("https.proxyPort", proxyPort);
    }
    if ((proxyUser.length() != 0) && (proxyPassword.length() != 0))
      Authenticator.setDefault(new ProxyAuthenticator(proxyUser, proxyPassword));
  }

  public String request(boolean quiet, String method, URL url, String params) throws IOException {
    if (!quiet) {
      System.out.println("[issuing request: " + method + " " + url + "]");
    }

    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);

    byte[] bytes = params.getBytes();
    InputStream body = new ByteArrayInputStream(bytes);

    byte[] buffer = new byte[8192];
    int read = 0;
    if (body != null) {
      connection.setDoOutput(true);

      OutputStream output = connection.getOutputStream();
      while ((read = body.read(buffer)) != -1) {
        output.write(buffer, 0, read);
      }

    }

    long time = System.currentTimeMillis();
    connection.connect();

    InputStream responseBodyStream = connection.getInputStream();
    StringBuffer responseBody = new StringBuffer();



    while ((read = responseBodyStream.read(buffer)) != -1) {
      responseBody.append(new String(buffer, 0, read));
    }
    connection.disconnect();
    time = System.currentTimeMillis() - time;

    if (!quiet) {
      System.out.println("[read " + responseBody.length() + " chars in " + time + "ms]");
    }

    if (!quiet) {
      String header = null;
      String headerValue = null;
      int index = 0;
      while ((headerValue = connection.getHeaderField(index)) != null) {
        header = connection.getHeaderFieldKey(index);

        if (header == null)
          System.out.println(headerValue);
        else {
          System.out.println(header + ": " + headerValue);
        }
        index++;
      }
      System.out.println("");
    }

    System.out.flush();
    return responseBody.toString();
  }


  /**
   * Finds the first occurrence of the pattern in the text. Implements the
   * Knuth-Morris-Pratt Algorithm for Pattern Matching
   */
  public int indexOf(byte[] data, byte[] pattern, int startIndex) {
    int[] failure = computeFailure(pattern);

    int j = 0;
    if (data.length == 0)
      return -1;

    for (int i = startIndex; i < data.length; i++) {
      while (j > 0 && pattern[j] != data[i]) {
        j = failure[j - 1];
      }
      if (pattern[j] == data[i]) {
        j++;
      }
      if (j == pattern.length) {
        return i - pattern.length + 1;
      }
    }
    return -1;
  }

  /**
   * Computes the failure function using a boot-strapping process, where the
   * pattern is matched against itself.
   */
  private int[] computeFailure(byte[] pattern) {
    int[] failure = new int[pattern.length];

    int j = 0;
    for (int i = 1; i < pattern.length; i++) {
      while (j > 0 && pattern[j] != pattern[i]) {
        j = failure[j - 1];
      }
      if (pattern[j] == pattern[i]) {
        j++;
      }
      failure[i] = j;
    }

    return failure;
  }

  public org.w3c.dom.Document readXml(InputStream is) throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    dbf.setValidating(false);
    dbf.setIgnoringComments(false);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setNamespaceAware(true);
    // dbf.setCoalescing(true);
    // dbf.setExpandEntityReferences(true);

    DocumentBuilder db = null;
    db = dbf.newDocumentBuilder();
    db.setEntityResolver(new NullResolver());

    // db.setErrorHandler( new MyErrorHandler());

    return db.parse(is);
  }

  class NullResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      return new InputSource(new StringReader(""));
    }
  }
}
