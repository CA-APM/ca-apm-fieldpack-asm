package com.ca.apm.swat.epaplugins.asm.error;

/**
 * An InitializationError is thrown when ASM Monitor has an unrecoverable problem during
 * initialization such as a <code>ClassNotFoundExcpetion</code>.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class InitializationError extends Error {

    private static final long serialVersionUID = -145943986005629088L;

    /**
     * Constructs a new InitializationException with the specified detail message.
     * @param message error message
     */
    public InitializationError(String message) {
        super(message);
    }
}
