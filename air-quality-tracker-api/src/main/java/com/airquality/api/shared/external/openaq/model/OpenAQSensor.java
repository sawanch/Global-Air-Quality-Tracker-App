package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAQ API Sensor object
 * Contains measurement data for a specific pollutant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQSensor {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("parameter")
    private OpenAQParameter parameter;
    
    @JsonProperty("latest")
    private OpenAQLatest latest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpenAQParameter getParameter() {
        return parameter;
    }

    public void setParameter(OpenAQParameter parameter) {
        this.parameter = parameter;
    }

    public OpenAQLatest getLatest() {
        return latest;
    }

    public void setLatest(OpenAQLatest latest) {
        this.latest = latest;
    }

    /**
     * Get the parameter name (pollutant type)
     */
    public String getParameterName() {
        return parameter != null ? parameter.getName() : null;
    }

    /**
     * Get the latest value for this sensor
     */
    public Double getLatestValue() {
        return latest != null ? latest.getValue() : null;
    }
}
