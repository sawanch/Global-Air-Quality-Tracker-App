package com.airquality.api.core.controller;

import com.airquality.api.core.model.AirQualityData;
import com.airquality.api.core.model.GlobalAirQualityStats;
import com.airquality.api.core.model.RefreshDataResponse;
import com.airquality.api.core.service.AirQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Air Quality data endpoints
 * Handles all HTTP requests for air quality statistics
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Air Quality", description = "Air Quality Monitoring Endpoints")
public class AirQualityController {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityController.class);

    private final AirQualityService airQualityService;

    public AirQualityController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    /**
     * GET /api/global - Returns aggregated global air quality statistics
     */
    @GetMapping("/global")
    @Operation(summary = "Get global statistics", description = "Returns aggregated worldwide air quality statistics")
    public ResponseEntity<GlobalAirQualityStats> getGlobalStats() {
        logger.info("GET /api/global - Fetching global air quality statistics");
        GlobalAirQualityStats stats = airQualityService.getGlobalStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/cities - Returns air quality data for all cities
     */
    @GetMapping("/cities")
    @Operation(summary = "Get all cities", description = "Returns air quality data for all monitored cities")
    public ResponseEntity<List<AirQualityData>> getAllCities() {
        logger.info("GET /api/cities - Fetching air quality data for all cities");
        List<AirQualityData> cities = airQualityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    /**
     * GET /api/city/{name} - Returns air quality data for a specific city
     */
    @GetMapping("/city/{name}")
    @Operation(summary = "Get city data", description = "Returns air quality data for a specific city (case-insensitive)")
    public ResponseEntity<AirQualityData> getCityByName(@PathVariable String name) {
        logger.info("GET /api/city/{} - Fetching air quality data for city", name);
        AirQualityData cityData = airQualityService.getCityData(name);
        return ResponseEntity.ok(cityData);
    }

    /**
     * GET /api/countries - Returns list of all countries
     */
    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Returns list of all unique countries with air quality data")
    public ResponseEntity<List<String>> getAllCountries() {
        logger.info("GET /api/countries - Fetching list of all countries");
        List<String> countries = airQualityService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    /**
     * GET /api/country/{name} - Returns cities in a specific country
     */
    @GetMapping("/country/{name}")
    @Operation(summary = "Get cities by country", description = "Returns all cities with air quality data in a specific country")
    public ResponseEntity<List<AirQualityData>> getCitiesByCountry(@PathVariable String name) {
        logger.info("GET /api/country/{} - Fetching cities for country", name);
        List<AirQualityData> cities = airQualityService.getCitiesByCountry(name);
        return ResponseEntity.ok(cities);
    }

    /**
     * GET /api/rankings/polluted - Returns most polluted cities
     */
    @GetMapping("/rankings/polluted")
    @Operation(summary = "Get most polluted cities", description = "Returns the top 10 most polluted cities by AQI")
    public ResponseEntity<List<AirQualityData>> getMostPollutedCities(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("GET /api/rankings/polluted - Fetching top {} most polluted cities", limit);
        List<AirQualityData> cities = airQualityService.getMostPollutedCities(limit);
        return ResponseEntity.ok(cities);
    }

    /**
     * GET /api/rankings/cleanest - Returns cleanest cities
     */
    @GetMapping("/rankings/cleanest")
    @Operation(summary = "Get cleanest cities", description = "Returns the top 10 cleanest cities by AQI")
    public ResponseEntity<List<AirQualityData>> getCleanestCities(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("GET /api/rankings/cleanest - Fetching top {} cleanest cities", limit);
        List<AirQualityData> cities = airQualityService.getCleanestCities(limit);
        return ResponseEntity.ok(cities);
    }

    /**
     * GET /api/filter/good - Returns cities with good air quality
     */
    @GetMapping("/filter/good")
    @Operation(summary = "Get cities with good air", description = "Returns cities with AQI 0-50 (good air quality)")
    public ResponseEntity<List<AirQualityData>> getCitiesWithGoodAir() {
        logger.info("GET /api/filter/good - Fetching cities with good air quality");
        List<AirQualityData> cities = airQualityService.getCitiesWithGoodAir();
        return ResponseEntity.ok(cities);
    }

    /**
     * GET /api/filter/unhealthy - Returns cities with unhealthy air quality
     */
    @GetMapping("/filter/unhealthy")
    @Operation(summary = "Get cities with unhealthy air", description = "Returns cities with AQI > 100 (unhealthy air quality)")
    public ResponseEntity<List<AirQualityData>> getCitiesWithUnhealthyAir() {
        logger.info("GET /api/filter/unhealthy - Fetching cities with unhealthy air quality");
        List<AirQualityData> cities = airQualityService.getCitiesWithUnhealthyAir();
        return ResponseEntity.ok(cities);
    }

    /**
     * POST /api/refresh - Manually refresh data from OpenAQ API
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh data", description = "Manually triggers data refresh from OpenAQ API")
    public ResponseEntity<RefreshDataResponse> refreshData() {
        logger.info("POST /api/refresh - Manually refreshing air quality data");
        int rowsAffected = airQualityService.refreshData();
        RefreshDataResponse response = new RefreshDataResponse(
            "success", 
            "Air quality data refreshed successfully", 
            rowsAffected
        );
        return ResponseEntity.ok(response);
    }
}
