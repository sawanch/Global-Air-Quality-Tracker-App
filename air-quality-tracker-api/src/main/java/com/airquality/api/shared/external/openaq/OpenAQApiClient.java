package com.airquality.api.shared.external.openaq;

import com.airquality.api.shared.external.openaq.model.*;
import com.airquality.api.core.model.AirQualityData;
import com.airquality.api.shared.util.AqiCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Fetches air quality data from OpenAQ API and transforms it to AirQualityData objects.
 */
@Component
public class OpenAQApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAQApiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAQApiClient(@Value("${openaq.api.url}") String apiUrl,
                           @Value("${openaq.api.key:}") String apiKey) {
        
        this.objectMapper = new ObjectMapper();
        
        // Build WebClient with base URL and optional API key
        // Configure with UTF-8 charset to handle special characters properly (é, ñ, etc.)
        MediaType jsonUtf8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
        
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, jsonUtf8.toString())
                .defaultHeader(HttpHeaders.ACCEPT, jsonUtf8.toString())
                .defaultHeader(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
        
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            builder.defaultHeader("X-API-Key", apiKey);
            logger.info("OpenAQ client initialized with API key and UTF-8 encoding");
        } else {
            logger.info("OpenAQ client initialized without API key and UTF-8 encoding");
        }
        
        this.webClient = builder.build();
    }

    // Fetches air quality data from OpenAQ API using locations with latest measurements
    public List<AirQualityData> fetchAirQualityData(int limit) {
        logger.info("Fetching air quality data from OpenAQ API (limit: {})", limit);
        
        try {
            // Limit to avoid rate limits (50 locations = 100 requests with delays)
            int safeLimit = Math.min(limit, 50);
            
            // First, fetch location IDs
            List<Long> locationIds = fetchLocationIds(safeLimit);
            logger.debug("Fetched {} location IDs", locationIds.size());
            
            if (locationIds.isEmpty()) {
                logger.warn("No locations found");
                return new ArrayList<>();
            }
            
            // Then fetch latest measurements for each location with delay
            List<AirQualityData> allData = new ArrayList<>();
            for (int i = 0; i < locationIds.size(); i++) {
                try {
                    AirQualityData data = fetchLocationWithLatest(locationIds.get(i));
                    if (data != null && data.getAqi() > 0) {
                        allData.add(data);
                    }
                    
                    // Add delay to avoid rate limits (wait 500ms between requests)
                    if (i < locationIds.size() - 1) {
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    logger.debug("Error fetching data for location {}: {}", locationIds.get(i), e.getMessage());
                }
            }
            
            logger.info("Successfully fetched {} locations with measurements from OpenAQ API", allData.size());
            return allData;
            
        } catch (Exception e) {
            logger.error("Error fetching data from OpenAQ API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Fetches location IDs
    private List<Long> fetchLocationIds(int limit) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/locations")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (response == null) {
                return new ArrayList<>();
            }
            
            OpenAQResponse openAQResponse = objectMapper.readValue(response, OpenAQResponse.class);
            
            if (openAQResponse.getResults() == null) {
                return new ArrayList<>();
            }
            
            List<Long> locationIds = new ArrayList<>();
            for (Object obj : openAQResponse.getResults()) {
                try {
                    String json = objectMapper.writeValueAsString(obj);
                    OpenAQLocation location = objectMapper.readValue(json, OpenAQLocation.class);
                    if (location.getId() != null) {
                        locationIds.add(location.getId());
                    }
                } catch (Exception e) {
                    logger.debug("Error parsing location: {}", e.getMessage());
                }
            }
            
            return locationIds;
            
        } catch (Exception e) {
            logger.error("Error fetching locations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Fetches location with latest measurements
    private AirQualityData fetchLocationWithLatest(Long locationId) {
        try {
            // Fetch latest measurements
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/locations/" + locationId + "/latest")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (response == null) {
                return null;
            }
            
            // Parse response
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
            
            if (results == null || results.isEmpty()) {
                return null;
            }
            
            // Extract location info from first measurement
            Map<String, Object> firstMeasurement = results.get(0);
            
            // Get coordinates
            @SuppressWarnings("unchecked")
            Map<String, Object> coordinates = (Map<String, Object>) firstMeasurement.get("coordinates");
            Double latitude = coordinates != null ? ((Number) coordinates.get("latitude")).doubleValue() : null;
            Double longitude = coordinates != null ? ((Number) coordinates.get("longitude")).doubleValue() : null;
            
            // Fetch location details to get city and country
            String locationResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/locations/" + locationId)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (locationResponse == null) {
                return null;
            }
            
            OpenAQResponse locationOpenAQResponse = objectMapper.readValue(locationResponse, OpenAQResponse.class);
            if (locationOpenAQResponse.getResults() == null || locationOpenAQResponse.getResults().isEmpty()) {
                return null;
            }
            
            String locationJson = objectMapper.writeValueAsString(locationOpenAQResponse.getResults().get(0));
            OpenAQLocation location = objectMapper.readValue(locationJson, OpenAQLocation.class);
            
            // Build air quality data
            AirQualityData data = AirQualityData.builder()
                .city(location.getCityName())
                .country(location.getCountryName())
                .locationId(locationId.toString())
                .latitude(latitude)
                .longitude(longitude)
                .lastUpdated(LocalDateTime.now())
                .build();
            
            // Create sensor ID to parameter map
            Map<Long, String> sensorIdToParameter = new HashMap<>();
            if (location.getSensors() != null) {
                for (OpenAQSensor sensor : location.getSensors()) {
                    if (sensor.getId() != null && sensor.getParameterName() != null) {
                        sensorIdToParameter.put(sensor.getId(), sensor.getParameterName());
                    }
                }
            }
            
            // Extract pollutant values from measurements
            for (Map<String, Object> measurement : results) {
                try {
                    Object sensorsIdObj = measurement.get("sensorsId");
                    Object valueObj = measurement.get("value");
                    
                    if (sensorsIdObj != null && valueObj != null) {
                        Long sensorsId = ((Number) sensorsIdObj).longValue();
                        Double value = ((Number) valueObj).doubleValue();
                        
                        if (sensorIdToParameter.containsKey(sensorsId)) {
                            String parameterName = sensorIdToParameter.get(sensorsId);
                            setPollutantValue(data, parameterName, value);
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Error parsing measurement: {}", e.getMessage());
                }
            }
            
            // Calculate AQI
            data.setAqi(calculateAqi(data));
            
            // Only return if valid and has AQI
            if (data.getCity() != null && !data.getCity().isEmpty() &&
                data.getCountry() != null && !data.getCountry().isEmpty() &&
                !"Unknown".equals(data.getCity()) && !"Unknown".equals(data.getCountry()) &&
                data.getAqi() > 0) {
                return data;
            }
            
            return null;
            
        } catch (Exception e) {
            logger.debug("Error fetching location with latest for {}: {}", locationId, e.getMessage());
            return null;
        }
    }
    
    // Sets pollutant value based on parameter name
    private void setPollutantValue(AirQualityData data, String param, Double value) {
        if (param == null || value == null) {
            return;
        }
        
        switch (param.toLowerCase()) {
            case "pm25":
                data.setPm25(value);
                break;
            case "pm10":
                data.setPm10(value);
                break;
            case "no2":
                data.setNo2(value);
                break;
            case "o3":
                data.setO3(value);
                break;
            case "co":
                data.setCo(value);
                break;
            case "so2":
                data.setSo2(value);
                break;
        }
    }

    // Calculates AQI from PM2.5 or PM10
    private int calculateAqi(AirQualityData data) {
        if (data.getPm25() != null) {
            return AqiCalculator.calculateAqiFromPm25(data.getPm25());
        } else if (data.getPm10() != null) {
            return AqiCalculator.calculateAqiFromPm10(data.getPm10());
        }
        return 0;
    }

    // Parses timestamp from OpenAQ datetime object
    private LocalDateTime parseTimestamp(OpenAQDatetime datetime) {
        if (datetime != null && datetime.getUtc() != null) {
            try {
                return LocalDateTime.parse(
                    datetime.getUtc().replace("Z", ""),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
            } catch (Exception e) {
                logger.debug("Error parsing timestamp: {}", e.getMessage());
            }
        }
        return LocalDateTime.now();
    }
}
