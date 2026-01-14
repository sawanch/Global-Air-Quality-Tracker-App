package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAQ API v3 Location result
 * Represents a single monitoring location
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQLocation {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("locality")
    private String locality;
    
    @JsonProperty("country")
    private OpenAQCountry country;
    
    @JsonProperty("coordinates")
    private OpenAQCoordinates coordinates;
    
    @JsonProperty("sensors")
    private List<OpenAQSensor> sensors;
    
    @JsonProperty("datetimeLast")
    private OpenAQDatetime datetimeLast;

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

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public OpenAQCountry getCountry() {
        return country;
    }

    public void setCountry(OpenAQCountry country) {
        this.country = country;
    }

    public OpenAQCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(OpenAQCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public List<OpenAQSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<OpenAQSensor> sensors) {
        this.sensors = sensors;
    }

    public OpenAQDatetime getDatetimeLast() {
        return datetimeLast;
    }

    public void setDatetimeLast(OpenAQDatetime datetimeLast) {
        this.datetimeLast = datetimeLast;
    }

    /**
     * Get the city name (uses locality or name)
     */
    public String getCityName() {
        if (locality != null && !locality.isEmpty()) {
            return locality;
        }
        return name != null ? name : "Unknown";
    }

    /**
     * Get the country name
     */
    public String getCountryName() {
        return country != null ? country.getName() : "Unknown";
    }
}
