package com.arm.mbed.aceauth.cose;
/**
 *
 * @author jimsch
 */
public class CoseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CoseException(String message) {
        super(message);
    }

    public CoseException(String message, Throwable t) {
        super(message, t);
    }
}
