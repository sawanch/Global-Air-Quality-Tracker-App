package com.airquality.api.core.service.impl;

import com.airquality.api.shared.exception.CityNotFoundException;
import com.airquality.api.shared.exception.CountryNotFoundException;
import com.airquality.api.shared.exception.DataRefreshException;
import com.airquality.api.shared.external.openaq.OpenAQApiClient;
import com.airquality.api.core.model.AirQualityData;
import com.airquality.api.core.model.GlobalAirQualityStats;
import com.airquality.api.core.repository.AirQualityRepository;
import com.airquality.api.core.service.AirQualityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for air quality data operations
 * Acts as a middle layer between controller and repository
 */
@Service
public class AirQualityServiceImpl implements AirQualityService {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityServiceImpl.class);

    private final AirQualityRepository airQualityRepository;
    private final OpenAQApiClient openAQApiClient;

    public AirQualityServiceImpl(AirQualityRepository airQualityRepository, 
                                  OpenAQApiClient openAQApiClient) {
        this.airQualityRepository = airQualityRepository;
        this.openAQApiClient = openAQApiClient;
    }

    /**
     * Calculate and return global air quality statistics
     */
    @Override
    @Cacheable(value = "globalStats", key = "'global'")
    public GlobalAirQualityStats getGlobalStats() {
        logger.debug("Calculating global air quality statistics");

        GlobalAirQualityStats stats = new GlobalAirQualityStats();

        // Get counts
        stats.setTotalCities(airQualityRepository.count());
        stats.setTotalCountries(airQualityRepository.countDistinctCountries());

        // Calculate average AQI
        Double avgAqi = airQualityRepository.calculateAverageAqi();
        stats.setAverageGlobalAqi(avgAqi != null ? Math.round(avgAqi * 10) / 10.0 : 0);

        // Count cities by AQI category
        stats.setCitiesWithGoodAir(airQualityRepository.countByAqiRange(0, 50));
        stats.setCitiesWithModerateAir(airQualityRepository.countByAqiRange(51, 100));
        stats.setCitiesWithUnhealthyAir(airQualityRepository.countByAqiRange(101, 500));

        // Get cleanest city
        AirQualityData cleanest = airQualityRepository.findCleanestCity();
        if (cleanest != null) {
            stats.setCleanestCity(cleanest.getCity());
            stats.setCleanestCountry(cleanest.getCountry());
            stats.setCleanestAqi(cleanest.getAqi());
        }

        // Get most polluted city
        AirQualityData mostPolluted = airQualityRepository.findMostPollutedCity();
        if (mostPolluted != null) {
            stats.setMostPollutedCity(mostPolluted.getCity());
            stats.setMostPollutedCountry(mostPolluted.getCountry());
            stats.setMostPollutedAqi(mostPolluted.getAqi());
        }

        // Set timestamp
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a 'UTC'"));
        stats.setLastUpdated(timestamp);

        logger.debug("Global stats: {} cities across {} countries, avg AQI: {}", 
            stats.getTotalCities(), stats.getTotalCountries(), stats.getAverageGlobalAqi());

        return stats;
    }

    /**
     * Get all cities with air quality data
     */
    @Override
    @Cacheable(value = "cities", key = "'all'")
    public List<AirQualityData> getAllCities() {
        logger.debug("Fetching all cities from database");
        return airQualityRepository.findAll();
    }

    /**
     * Get air quality data for a specific city
     */
    @Override
    @Cacheable(value = "city", key = "#city.toLowerCase()")
    public AirQualityData getCityData(String city) {
        logger.debug("Fetching data for city: {}", city);
        AirQualityData cityData = airQualityRepository.findByCity(city);
        
        if (cityData == null) {
            throw new CityNotFoundException("No air quality data available for city: " + city);
        }
        
        return cityData;
    }

    /**
     * Get all cities in a country
     */
    @Override
    @Cacheable(value = "country", key = "#country.toLowerCase()")
    public List<AirQualityData> getCitiesByCountry(String country) {
        logger.debug("Fetching cities for country: {}", country);
        List<AirQualityData> cities = airQualityRepository.findByCountry(country);
        
        if (cities.isEmpty()) {
            throw new CountryNotFoundException("No air quality data available for country: " + country);
        }
        
        return cities;
    }

    /**
     * Get list of all unique countries
     */
    @Override
    @Cacheable(value = "countries", key = "'all'")
    public List<String> getAllCountries() {
        logger.debug("Fetching all unique countries");
        return airQualityRepository.findAllCountries();
    }

    /**
     * Refresh data from OpenAQ API
     */
    @Override
    @CacheEvict(value = {"globalStats", "cities", "city", "country", "countries"}, allEntries = true)
    public int refreshData() {
        logger.info("Refreshing air quality data from OpenAQ API");
        
        try {
            // Fetch data from OpenAQ API (limit 50 locations to avoid rate limits)
            List<AirQualityData> newData = openAQApiClient.fetchAirQualityData(50);
            
            if (newData.isEmpty()) {
                logger.warn("No data received from OpenAQ API");
                return 0;
            }
            
            logger.info("Received {} records from OpenAQ API, upserting to database", newData.size());
            
            // Bulk upsert to database
            int rowsAffected = airQualityRepository.bulkUpsert(newData);
            
            logger.info("Data refresh completed. {} rows affected", rowsAffected);
            return rowsAffected;
            
        } catch (Exception e) {
            logger.error("Error refreshing air quality data", e);
            throw new DataRefreshException("Failed to refresh air quality data: " + e.getMessage(), e);
        }
    }

    /**
     * Get cities with good air quality (AQI 0-50)
     */
    @Override
    public List<AirQualityData> getCitiesWithGoodAir() {
        return getAllCities().stream()
            .filter(city -> city.getAqi() != null && city.getAqi() <= 50)
            .sorted(Comparator.comparingInt(AirQualityData::getAqi))
            .collect(Collectors.toList());
    }

    /**
     * Get cities with unhealthy air quality (AQI > 100)
     */
    @Override
    public List<AirQualityData> getCitiesWithUnhealthyAir() {
        return getAllCities().stream()
            .filter(city -> city.getAqi() != null && city.getAqi() > 100)
            .sorted((a, b) -> b.getAqi().compareTo(a.getAqi()))
            .collect(Collectors.toList());
    }

    /**
     * Get top N most polluted cities
     */
    @Override
    public List<AirQualityData> getMostPollutedCities(int limit) {
        return getAllCities().stream()
            .filter(city -> city.getAqi() != null)
            .sorted((a, b) -> b.getAqi().compareTo(a.getAqi()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get top N cleanest cities
     */
    @Override
    public List<AirQualityData> getCleanestCities(int limit) {
        return getAllCities().stream()
            .filter(city -> city.getAqi() != null && city.getAqi() > 0)
            .sorted(Comparator.comparingInt(AirQualityData::getAqi))
            .limit(limit)
            .collect(Collectors.toList());
    }
}
