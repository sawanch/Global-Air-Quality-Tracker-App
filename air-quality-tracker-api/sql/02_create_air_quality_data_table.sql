-- ============================================
-- Air Quality Data Table Creation Script
-- Run after 01_create_database.sql
-- ============================================

USE air_quality_db;

-- Drop table if exists (for fresh setup)
DROP TABLE IF EXISTS air_quality_data;

-- Create air quality data table
CREATE TABLE air_quality_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    location_id VARCHAR(100),
    aqi INT,
    pm25 DOUBLE,
    pm10 DOUBLE,
    no2 DOUBLE,
    o3 DOUBLE,
    co DOUBLE,
    so2 DOUBLE,
    latitude DOUBLE,
    longitude DOUBLE,
    last_updated DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Unique constraint on city + country combination
    UNIQUE KEY uk_city_country (city, country),
    
    -- Indexes for common queries
    INDEX idx_country (country),
    INDEX idx_aqi (aqi),
    INDEX idx_city (city),
    INDEX idx_last_updated (last_updated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Display confirmation
SELECT 'Table air_quality_data created successfully!' AS status;

-- Show table structure
DESCRIBE air_quality_data;
