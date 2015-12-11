package com.ca.apm.swat.epaplugins.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ErrorUtils {

    /**
     * Get the stack trace from an exception.
     * @param exception the exception
     * @return the stack trace
     */
    public static String getStackTrace(Exception exception) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);
        exception.printStackTrace(stream);
        return out.toString();
    }
}
