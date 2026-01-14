package com.airquality.api.intelligence.model;

/**
 * Response model for health advisory endpoint
 * Provides AQI-based health recommendations
 */
public class HealthAdvisoryResponse {
    
    private Integer aqi;
    private String category;
    private String advisory;

    public HealthAdvisoryResponse() {
    }

    public HealthAdvisoryResponse(Integer aqi, String category, String advisory) {
        this.aqi = aqi;
        this.category = category;
        this.advisory = advisory;
    }

    // Getters and Setters
    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAdvisory() {
        return advisory;
    }

    public void setAdvisory(String advisory) {
        this.advisory = advisory;
    }
}
