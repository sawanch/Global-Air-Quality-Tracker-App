package com.airquality.api.core.initializer;

import com.airquality.api.core.repository.AirQualityRepository;
import com.airquality.api.core.service.AirQualityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes air quality data on application startup
 * Loads data from OpenAQ API if database is empty
 */
@Component
public class AirQualityDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityDataInitializer.class);

    private final AirQualityRepository airQualityRepository;
    private final AirQualityService airQualityService;

    public AirQualityDataInitializer(AirQualityRepository airQualityRepository,
                                      AirQualityService airQualityService) {
        this.airQualityRepository = airQualityRepository;
        this.airQualityService = airQualityService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Air Quality Data Initializer Started ===");
        
        try {
            if (airQualityRepository.isEmpty()) {
                logger.info("Database is empty. Loading initial data from OpenAQ API...");
                loadInitialData();
            } else {
                int count = airQualityRepository.count();
                logger.info("Database already contains {} records. Skipping initial load.", count);
            }
        } catch (Exception e) {
            logger.error("Error during data initialization: {}", e.getMessage(), e);
            logger.warn("Application will continue without initial data. Data will be loaded on first scheduled refresh.");
        }
        
        logger.info("=== Air Quality Data Initializer Completed ===");
    }

    /**
     * Load initial air quality data from OpenAQ API
     */
    private void loadInitialData() {
        logger.info("Fetching initial air quality data from OpenAQ API...");
        
        try {
            int rowsAffected = airQualityService.refreshData();
            logger.info("Initial data load completed. {} cities loaded.", rowsAffected);
        } catch (Exception e) {
            logger.error("Failed to load initial data: {}", e.getMessage(), e);
            throw e;
        }
    }
}
