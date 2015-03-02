package com.wily.fieldext.epaplugins.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TextNormalizer {

  public static StringFilter getNormalizationStringFilter() throws ClassNotFoundException {
    try {
      return new Java6Normalizer();
    } catch (Exception localException2) {
      try {
        return new Java5Normalizer();
      } catch (Exception ex) {
        throw new ClassNotFoundException("Cannot instantiate a Text Normalizer. Check your Java Runtime version.");
      }
    }
  }
  public static class Java6Normalizer implements StringFilter {
    private final Method normalizer;
    private final Object nfd;
    public Java6Normalizer() throws IllegalAccessException, ClassNotFoundException {
      this.normalizer = java6GetMethodNormalizer();
      this.nfd = java6GetNFD();
    }

    public String filter(String text) {
      try {
        return TextNormalizer.java6Invoke(text, this.normalizer, this.nfd);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }
  private static String java6Invoke(String text, Method normalizer, Object nfd)
      throws IllegalAccessException, InvocationTargetException {
    return ((String) normalizer.invoke(null, new Object[]{text, nfd}));
  }

  private static Method java6GetMethodNormalizer() throws ClassNotFoundException {
    Class c = Class.forName("java.text.Normalizer");
    Method[] methods = c.getMethods();
    for (int i = 0; i < methods.length; ++i) {
      if (methods[i].getName().equals("normalize")) {
        return methods[i];
      }
    }
    return null;
  }

  private static Object java6GetNFD() throws ClassNotFoundException, IllegalAccessException {
    Class x = Class.forName("java.text.Normalizer$Form");
    Object nfd = null;
    for (Field f : x.getDeclaredFields()) {
      if (f.getName().equals("NFD")) {
        nfd = f.get(null);
      }
    }

    return nfd;
  }
  public static class Java5Normalizer implements StringFilter {
    private final Method normalizer;
    public Java5Normalizer() throws ClassNotFoundException {
      this.normalizer = java5GetMethodNormalizer();
    }

    public String filter(String text) {
      try {
        return java5Invoke(text, normalizer);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static final Integer ZERO = Integer.valueOf(0);

  private static String java5Invoke(String text, Method normalizer)
      throws InvocationTargetException, IllegalAccessException {
    return ((String) normalizer.invoke(null, new Object[]{text, Boolean.FALSE, ZERO}));
  }

  private static Method java5GetMethodNormalizer() throws ClassNotFoundException {
    Class c = Class.forName("sun.text.Normalizer");
    Method[] methods = c.getMethods();
    for (int i = 0; i < methods.length; ++i)
      if ((methods[i].getName().equals("decompose")) && (methods[i].getGenericParameterTypes().length == 3))
        return methods[i];
    return null;
  }
}
