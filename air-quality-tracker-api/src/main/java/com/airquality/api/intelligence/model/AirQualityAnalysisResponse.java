package com.airquality.api.intelligence.model;

/**
 * Response model for air quality analysis endpoint
 * Provides AI-generated analysis and insights for a specific city
 */
public class AirQualityAnalysisResponse {
    
    private String city;
    private String analysis;

    public AirQualityAnalysisResponse() {
    }

    public AirQualityAnalysisResponse(String city, String analysis) {
        this.city = city;
        this.analysis = analysis;
    }

    // Getters and Setters
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
