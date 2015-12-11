package com.ca.apm.swat.epaplugins.asm.error;

/**
 * An AsmException is thrown when ASM Monitor has a recoverable problem.
 * 
 * @author Guenter Grossberger - CA APM SWAT Team
 *
 */
public class AsmException extends Exception {

    private static final long serialVersionUID = -1570303790658472020L;

    public static int UNDEFINED_ERROR = -1;
    
    private int errorCode = UNDEFINED_ERROR;
    
    /**
     * Constructs a new AsmException with the specified detail message.
     * @param message error message
     */
    public AsmException(String message) {
        super(message);
    }

    /**
     * Constructs a new AsmException with the specified detail message.
     * @param message error message
     * @param cause original exception
     */
    public AsmException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AsmException with the specified detail message.
     * @param message error message
     * @param errorCode error code, UNDEFINED_ERROR if not set
     */
    public AsmException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new AsmException with the specified detail message.
     * @param message error message
     * @param cause original exception
     * @param errorCode error code, UNDEFINED_ERROR if not set
     */
    public AsmException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Get ASM error code.
     * @return ASM error code or UNDEFINED_ERROR
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Set ASM error code.
     * @param errorCode ASM error code
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
