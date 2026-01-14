package com.airquality.api.shared.exception;

/**
 * Exception thrown when a country is not found in the air quality database
 */
public class CountryNotFoundException extends RuntimeException {
    
    public CountryNotFoundException(String message) {
        super(message);
    }
    
    public CountryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
