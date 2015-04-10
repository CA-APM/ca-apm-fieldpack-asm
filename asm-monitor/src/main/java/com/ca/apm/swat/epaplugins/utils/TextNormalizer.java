package com.ca.apm.swat.epaplugins.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class TextNormalizer {

    protected static final String JAVA_NORMALIZER_CLASS         = "java.text.Normalizer";
    protected static final String JAVA_NORMALIZER_FORM_CLASS    = "java.text.Normalizer$Form";
    protected static final String NORMALIZE                     = "normalize";
    protected static final String NFD                           = "NFD";
    protected static final String SUN_NORMALIZER_CLASS          = "sun.text.Normalizer";
    protected static final String DECOMPOSE                     = "decompose";
    
    /**
     * Get a normalization string filter.
     * @return a StringFilter
     * @throws ClassNotFoundException if no text normalizer class could be found
     */
    public static StringFilter getNormalizationStringFilter() throws ClassNotFoundException {
        try {
            return new Java6Normalizer();
        } catch (Exception localException2) {
            try {
                return new Java5Normalizer();
            } catch (Exception ex) {
                throw new ClassNotFoundException(
                    AsmMessages.getMessage(AsmMessages.TEXT_NORMALIZER_NOT_FOUND));
            }
        }
    }
    
    public static class Java6Normalizer implements StringFilter {
        private final Method normalizer;
        private final Object nfd;
        
        public Java6Normalizer() throws IllegalAccessException, ClassNotFoundException {
            this.normalizer = java6GetMethodNormalizer();
            this.nfd = java6GetNfd();
        }

        
        /**
         * Filter the text.
         */
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
        Class clazz = Class.forName(JAVA_NORMALIZER_CLASS);
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(NORMALIZE)) {
                return methods[i];
            }
        }
        return null;
    }

    private static Object java6GetNfd() throws ClassNotFoundException, IllegalAccessException {
        Class clazz = Class.forName(JAVA_NORMALIZER_FORM_CLASS);
        Object nfd = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(NFD)) {
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

        /**
         * Filter the text.
         */
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
        Class clazz = Class.forName(SUN_NORMALIZER_CLASS);
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            if ((methods[i].getName().equals(DECOMPOSE))
                    && (methods[i].getGenericParameterTypes().length == 3)) {
                return methods[i];
            }
        }
        return null;
    }
}
