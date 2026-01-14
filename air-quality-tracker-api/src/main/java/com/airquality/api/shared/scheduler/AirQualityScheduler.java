package com.airquality.api.shared.scheduler;

import com.airquality.api.core.service.AirQualityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler for periodic air quality data refresh
 * Fetches data from OpenAQ API every 6 hours
 */
@Component
public class AirQualityScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityScheduler.class);

    private final AirQualityService airQualityService;

    public AirQualityScheduler(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    /**
     * Scheduled task to refresh air quality data every 6 hours
     * Runs at 00:00, 06:00, 12:00, 18:00 every day
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void refreshDataScheduled() {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        logger.info("=== Scheduled data refresh started at {} ===", timestamp);
        
        try {
            int rowsAffected = airQualityService.refreshData();
            logger.info("=== Scheduled data refresh completed. {} rows affected ===", rowsAffected);
        } catch (Exception e) {
            logger.error("=== Scheduled data refresh FAILED: {} ===", e.getMessage(), e);
            // Don't rethrow - scheduler will retry in 6 hours
        }
    }

    /**
     * Alternative fixed-rate scheduling (commented out - using cron instead)
     * Runs every 6 hours starting from application startup
     * 
     * @Scheduled(fixedRate = 21600000, initialDelay = 60000) // 6 hours = 21600000ms, initial delay 1 minute
     */
    public void refreshDataFixedRate() {
        // Alternative to cron-based scheduling
        // Uncomment @Scheduled annotation above if you prefer fixed-rate
        refreshDataScheduled();
    }
}
