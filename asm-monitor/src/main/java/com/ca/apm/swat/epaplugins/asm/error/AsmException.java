package com.ca.apm.swat.epaplugins.asm.error;

/**
 * An AsmException is thrown when ASM Monitor has a recoverable problem.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmException extends Exception {

    private static final long serialVersionUID = -1570303790658472020L;

    /**
     * Constructs a new AsmException with the specified detail message.
     * @param message error message
     */
    public AsmException(String message) {
        super(message);
    }
}
