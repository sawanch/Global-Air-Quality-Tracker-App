package com.airquality.api.shared.external.openaq.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenAQ API v3 Measurement result
 * Represents a single air quality measurement
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAQMeasurement {
    
    @JsonProperty("locationId")
    private Long locationId;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("country")
    private OpenAQCountry country;
    
    @JsonProperty("coordinates")
    private OpenAQCoordinates coordinates;
    
    @JsonProperty("parameter")
    private OpenAQParameter parameter;
    
    @JsonProperty("value")
    private Double value;
    
    @JsonProperty("datetime")
    private OpenAQDatetime datetime;
    
    @JsonProperty("unit")
    private String unit;

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    public OpenAQParameter getParameter() {
        return parameter;
    }

    public void setParameter(OpenAQParameter parameter) {
        this.parameter = parameter;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public OpenAQDatetime getDatetime() {
        return datetime;
    }

    public void setDatetime(OpenAQDatetime datetime) {
        this.datetime = datetime;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Get parameter name (pollutant type)
     */
    public String getParameterName() {
        return parameter != null ? parameter.getName() : null;
    }

    /**
     * Get country name
     */
    public String getCountryName() {
        return country != null ? country.getName() : "Unknown";
    }

    /**
     * Get city name
     */
    public String getCityName() {
        return city != null && !city.isEmpty() ? city : location;
    }
}
