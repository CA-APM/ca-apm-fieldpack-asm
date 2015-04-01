package com.ca.apm.swat.epaplugins.asm.error;

public class LoginException extends Exception {

    private static final long serialVersionUID = 3505324730780232110L;

    /**
     * Constructs a new LoginException with the specified detail message.
     * @param message
     */
    public LoginException(String message) {
        super(message);
    }
}
