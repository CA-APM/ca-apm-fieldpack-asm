package com.ca.apm.swat.epaplugins.asm.error;

/**
 * A LoginError is thrown when ASM Monitor cannot login to App Synthetic Monitor.
 * This typically happens if wrong credentials are provided.
 *
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class LoginError extends Error {

    private static final long serialVersionUID = 3505324730780232110L;

    /**
     * Constructs a new LoginException with the specified detail message.
     * @param message error message
     */
    public LoginError(String message) {
        super(message);
    }
}
