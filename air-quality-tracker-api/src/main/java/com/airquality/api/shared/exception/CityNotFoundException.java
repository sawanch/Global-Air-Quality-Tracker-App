package com.airquality.api.shared.exception;

/**
 * Exception thrown when a requested city is not found
 */
public class CityNotFoundException extends RuntimeException {
    
    public CityNotFoundException(String cityName) {
        super("City not found: " + cityName);
    }
    
    public CityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
