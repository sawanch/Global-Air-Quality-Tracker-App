package com.airquality.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Global Air Quality Tracker API
 * 
 * @SpringBootApplication - Enables auto-configuration, component scanning
 * @EnableScheduling - Enables scheduled tasks for 6-hour data refresh
 */
@SpringBootApplication
@EnableScheduling
public class AirQualityTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirQualityTrackerApiApplication.class, args);
    }
}
