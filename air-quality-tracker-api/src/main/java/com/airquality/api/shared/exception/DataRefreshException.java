package com.airquality.api.shared.exception;

/**
 * Exception thrown when data refresh from external API fails
 */
public class DataRefreshException extends RuntimeException {
    
    public DataRefreshException(String message) {
        super(message);
    }
    
    public DataRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
