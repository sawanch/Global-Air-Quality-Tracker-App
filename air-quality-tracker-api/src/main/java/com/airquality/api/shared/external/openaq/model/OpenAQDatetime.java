package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAQ API Datetime object
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQDatetime {
    
    @JsonProperty("utc")
    private String utc;

    public String getUtc() {
        return utc;
    }

    public void setUtc(String utc) {
        this.utc = utc;
    }
}
