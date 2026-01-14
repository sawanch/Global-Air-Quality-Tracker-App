package com.airquality.api.core.repository;

import com.airquality.api.core.mapper.AirQualityDataRowMapper;
import com.airquality.api.core.model.AirQualityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

/**
 * Repository for accessing air quality data from MySQL database
 * Uses JdbcTemplate to execute SQL queries and HikariCP to manage database connections
 */
@Repository
public class AirQualityRepository {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public AirQualityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("AirQualityRepository initialized with JDBC Template");
    }

    /**
     * Returns all air quality data from database
     * 
     * @return List of all AirQualityData objects
     */
    public List<AirQualityData> findAll() {
        logger.debug("Fetching all air quality data from database");
        String query = "SELECT id, city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, " +
                       "latitude, longitude, last_updated " +
                       "FROM air_quality_data " +
                       "ORDER BY city ASC";
        return jdbcTemplate.query(query, new AirQualityDataRowMapper());
    }

    /**
     * Find air quality data by city name (case-insensitive)
     * 
     * @param city City name to search for
     * @return AirQualityData if found, null otherwise
     */
    public AirQualityData findByCity(String city) {
        logger.debug("Searching for city: {}", city);
        String query = "SELECT id, city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, " +
                       "latitude, longitude, last_updated " +
                       "FROM air_quality_data " +
                       "WHERE LOWER(city) = LOWER(?) " +
                       "LIMIT 1";
        
        List<AirQualityData> results = jdbcTemplate.query(query, new AirQualityDataRowMapper(), city);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find all cities in a specific country
     * 
     * @param country Country name to filter by
     * @return List of AirQualityData for the country
     */
    public List<AirQualityData> findByCountry(String country) {
        logger.debug("Fetching cities for country: {}", country);
        String query = "SELECT id, city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, " +
                       "latitude, longitude, last_updated " +
                       "FROM air_quality_data " +
                       "WHERE LOWER(country) = LOWER(?) " +
                       "ORDER BY city ASC";
        
        return jdbcTemplate.query(query, new AirQualityDataRowMapper(), country);
    }

    /**
     * Get list of unique countries
     * 
     * @return List of country names
     */
    public List<String> findAllCountries() {
        logger.debug("Fetching all unique countries");
        String query = "SELECT DISTINCT country FROM air_quality_data ORDER BY country ASC";
        return jdbcTemplate.queryForList(query, String.class);
    }

    /**
     * Check if a city+country combination exists
     * 
     * @param city City name
     * @param country Country name
     * @return true if exists, false otherwise
     */
    public boolean existsByCityAndCountry(String city, String country) {
        String query = "SELECT COUNT(*) FROM air_quality_data WHERE LOWER(city) = LOWER(?) AND LOWER(country) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, city, country);
        return count != null && count > 0;
    }

    /**
     * Count total cities in database
     * 
     * @return Total count
     */
    public int count() {
        String query = "SELECT COUNT(*) FROM air_quality_data";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Count unique countries in database
     * 
     * @return Count of unique countries
     */
    public int countDistinctCountries() {
        String query = "SELECT COUNT(DISTINCT country) FROM air_quality_data";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Calculate average AQI across all cities
     * 
     * @return Average AQI value
     */
    public Double calculateAverageAqi() {
        String query = "SELECT AVG(aqi) FROM air_quality_data WHERE aqi IS NOT NULL";
        return jdbcTemplate.queryForObject(query, Double.class);
    }

    /**
     * Count cities within an AQI range
     * 
     * @param minAqi Minimum AQI (inclusive)
     * @param maxAqi Maximum AQI (inclusive)
     * @return Count of cities in range
     */
    public int countByAqiRange(int minAqi, int maxAqi) {
        String query = "SELECT COUNT(*) FROM air_quality_data WHERE aqi >= ? AND aqi <= ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, minAqi, maxAqi);
        return count != null ? count : 0;
    }

    /**
     * Find city with lowest AQI (cleanest air)
     * 
     * @return AirQualityData for cleanest city
     */
    public AirQualityData findCleanestCity() {
        String query = "SELECT id, city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, " +
                       "latitude, longitude, last_updated " +
                       "FROM air_quality_data " +
                       "WHERE aqi IS NOT NULL AND aqi > 0 " +
                       "ORDER BY aqi ASC " +
                       "LIMIT 1";
        
        List<AirQualityData> results = jdbcTemplate.query(query, new AirQualityDataRowMapper());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find city with highest AQI (most polluted)
     * 
     * @return AirQualityData for most polluted city
     */
    public AirQualityData findMostPollutedCity() {
        String query = "SELECT id, city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, " +
                       "latitude, longitude, last_updated " +
                       "FROM air_quality_data " +
                       "WHERE aqi IS NOT NULL " +
                       "ORDER BY aqi DESC " +
                       "LIMIT 1";
        
        List<AirQualityData> results = jdbcTemplate.query(query, new AirQualityDataRowMapper());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Bulk update or insert air quality data
     * Uses INSERT ... ON DUPLICATE KEY UPDATE
     * 
     * @param dataList List of AirQualityData objects to upsert
     * @return Total number of rows affected
     */
    @Transactional
    public int bulkUpsert(List<AirQualityData> dataList) {
        logger.info("Bulk upserting {} air quality records", dataList.size());
        
        String query = "INSERT INTO air_quality_data " +
                       "(city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, latitude, longitude, last_updated) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE " +
                       "location_id = VALUES(location_id), " +
                       "aqi = VALUES(aqi), " +
                       "pm25 = VALUES(pm25), " +
                       "pm10 = VALUES(pm10), " +
                       "no2 = VALUES(no2), " +
                       "o3 = VALUES(o3), " +
                       "co = VALUES(co), " +
                       "so2 = VALUES(so2), " +
                       "latitude = VALUES(latitude), " +
                       "longitude = VALUES(longitude), " +
                       "last_updated = VALUES(last_updated), " +
                       "updated_at = CURRENT_TIMESTAMP";
        
        int totalRowsAffected = 0;
        
        for (AirQualityData data : dataList) {
            try {
                Timestamp lastUpdated = data.getLastUpdated() != null 
                    ? Timestamp.valueOf(data.getLastUpdated()) 
                    : new Timestamp(System.currentTimeMillis());
                
                int rowsAffected = jdbcTemplate.update(query,
                    data.getCity(),
                    data.getCountry(),
                    data.getLocationId(),
                    data.getAqi(),
                    data.getPm25(),
                    data.getPm10(),
                    data.getNo2(),
                    data.getO3(),
                    data.getCo(),
                    data.getSo2(),
                    data.getLatitude(),
                    data.getLongitude(),
                    lastUpdated
                );
                totalRowsAffected += rowsAffected;
            } catch (Exception e) {
                logger.error("Error upserting data for city: {} in {}", data.getCity(), data.getCountry(), e);
                throw e; // Transaction will rollback
            }
        }
        
        logger.info("Bulk upsert completed. {} rows affected", totalRowsAffected);
        return totalRowsAffected;
    }

    /**
     * Check if database is empty
     * 
     * @return true if no records exist
     */
    public boolean isEmpty() {
        String query = "SELECT COUNT(*) FROM air_quality_data";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class);
        return count != null && count == 0;
    }

    /**
     * Delete all data (for testing purposes)
     */
    @Transactional
    public void deleteAll() {
        logger.warn("Deleting all air quality data");
        jdbcTemplate.update("DELETE FROM air_quality_data");
    }
}
