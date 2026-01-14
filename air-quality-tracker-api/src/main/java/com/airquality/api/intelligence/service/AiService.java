package com.airquality.api.intelligence.service;

import com.airquality.api.intelligence.model.AirQualityRecommendation;
import com.airquality.api.core.model.AirQualityData;

/**
 * Service interface for AI-powered air quality analysis
 */
public interface AiService {

    /**
     * Generate personalized recommendations based on air quality data
     * 
     * @param airQualityData Air quality data for the location
     * @return AI-generated recommendations
     */
    AirQualityRecommendation generateRecommendations(AirQualityData airQualityData);

    /**
     * Generate recommendations for a city by name
     * 
     * @param cityName Name of the city
     * @return AI-generated recommendations
     */
    AirQualityRecommendation getRecommendationsForCity(String cityName);

    /**
     * Generate a health advisory based on current AQI
     * 
     * @param aqi Current AQI value
     * @return Health advisory text
     */
    String generateHealthAdvisory(int aqi);

    /**
     * Analyze air quality trends and provide insights
     * 
     * @param cityName Name of the city
     * @return Analysis insights
     */
    String analyzeAirQualityTrends(String cityName);
}
