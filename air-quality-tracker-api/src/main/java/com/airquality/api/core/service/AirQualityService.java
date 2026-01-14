package com.airquality.api.core.service;

import com.airquality.api.core.model.AirQualityData;
import com.airquality.api.core.model.GlobalAirQualityStats;

import java.util.List;

/**
 * Service interface defining air quality data operations
 * Implementation: AirQualityServiceImpl
 */
public interface AirQualityService {

    /**
     * Get aggregated global air quality statistics
     * 
     * @return GlobalAirQualityStats with worldwide data
     */
    GlobalAirQualityStats getGlobalStats();

    /**
     * Get air quality data for all cities
     * 
     * @return List of all AirQualityData objects
     */
    List<AirQualityData> getAllCities();

    /**
     * Get air quality data for a specific city
     * 
     * @param city City name (case-insensitive)
     * @return AirQualityData if found, null otherwise
     */
    AirQualityData getCityData(String city);

    /**
     * Get all cities in a specific country
     * 
     * @param country Country name (case-insensitive)
     * @return List of AirQualityData for the country
     */
    List<AirQualityData> getCitiesByCountry(String country);

    /**
     * Get list of all unique countries
     * 
     * @return List of country names
     */
    List<String> getAllCountries();

    /**
     * Manually refresh data from OpenAQ API
     * 
     * @return Number of records updated
     */
    int refreshData();

    /**
     * Get cities with good air quality (AQI 0-50)
     * 
     * @return List of cities with good air
     */
    List<AirQualityData> getCitiesWithGoodAir();

    /**
     * Get cities with unhealthy air quality (AQI > 100)
     * 
     * @return List of cities with unhealthy air
     */
    List<AirQualityData> getCitiesWithUnhealthyAir();

    /**
     * Get top N most polluted cities
     * 
     * @param limit Number of cities to return
     * @return List of most polluted cities
     */
    List<AirQualityData> getMostPollutedCities(int limit);

    /**
     * Get top N cleanest cities
     * 
     * @param limit Number of cities to return
     * @return List of cleanest cities
     */
    List<AirQualityData> getCleanestCities(int limit);
}
