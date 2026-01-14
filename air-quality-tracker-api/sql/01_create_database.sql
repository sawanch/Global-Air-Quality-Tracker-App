-- ============================================
-- Database Creation Script for Air Quality Tracker
-- Run this first to create the database
-- ============================================

-- Create database with UTF-8 support
CREATE DATABASE IF NOT EXISTS air_quality_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE air_quality_db;

-- Display confirmation
SELECT 'Database air_quality_db created successfully!' AS status;
