package io.github.jmecn.font.exception;

/**
 */
public class FtRuntimeException extends RuntimeException {

    public FtRuntimeException(String message) {
        super(message);
    }

    public FtRuntimeException(String message, Throwable ex) {
        super(message, ex);
    }
}