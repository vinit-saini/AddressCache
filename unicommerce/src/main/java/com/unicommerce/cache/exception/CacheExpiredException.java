package com.unicommerce.cache.exception;

/**
 * just another exception class
 *
 * Created by vsaini on 11/19/18.
 */
public class CacheExpiredException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public CacheExpiredException(String message) {
        super(message);
    }
}
