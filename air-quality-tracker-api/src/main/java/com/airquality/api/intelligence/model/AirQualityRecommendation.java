package com.airquality.api.intelligence.model;

import java.util.List;

/**
 * Model representing AI-generated air quality recommendations
 * Contains overall assessment and specific actionable recommendations
 */
public class AirQualityRecommendation {
    
    private String city;
    private String country;
    private Integer aqi;
    private String aqiCategory;
    private String overallAssessment;
    private List<RecommendationCard> recommendations;
    private String generatedAt;

    public AirQualityRecommendation() {
    }

    // Getters and Setters
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public String getAqiCategory() {
        return aqiCategory;
    }

    public void setAqiCategory(String aqiCategory) {
        this.aqiCategory = aqiCategory;
    }

    public String getOverallAssessment() {
        return overallAssessment;
    }

    public void setOverallAssessment(String overallAssessment) {
        this.overallAssessment = overallAssessment;
    }

    public List<RecommendationCard> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationCard> recommendations) {
        this.recommendations = recommendations;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
