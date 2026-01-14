package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAQ API Latest measurement object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQLatest {
    
    @JsonProperty("value")
    private Double value;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
