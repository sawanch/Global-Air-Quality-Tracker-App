package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAQ API v3 Response wrapper
 * Represents the top-level response from OpenAQ API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQResponse {
    
    @JsonProperty("results")
    private List<OpenAQLocation> results;

    public List<OpenAQLocation> getResults() {
        return results;
    }

    public void setResults(List<OpenAQLocation> results) {
        this.results = results;
    }
}
