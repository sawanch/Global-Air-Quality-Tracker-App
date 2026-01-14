package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAQ API Parameter object
 * Represents a pollutant type (pm25, pm10, no2, etc.)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQParameter {
    
    @JsonProperty("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
