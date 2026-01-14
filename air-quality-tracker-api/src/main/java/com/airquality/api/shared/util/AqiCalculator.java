package com.airquality.api.shared.util;

/**
 * Converts pollutant concentrations to Air Quality Index (AQI) using EPA standards.
 * 
 * Simple explanation: 
 * - Takes raw pollution measurements (like PM2.5 = 35.2 μg/m³)
 * - Converts to easy-to-understand AQI number (like AQI = 100)
 * - Higher AQI = Worse air quality
 */
public class AqiCalculator {

    // EPA's official breakpoints for PM2.5
    // Format: {minConcentration, maxConcentration, minAQI, maxAQI}
    private static final double[][] PM25_BREAKPOINTS = {
        {0.0, 12.0, 0, 50},        // Good
        {12.1, 35.4, 51, 100},     // Moderate
        {35.5, 55.4, 101, 150},    // Unhealthy for Sensitive Groups
        {55.5, 150.4, 151, 200},   // Unhealthy
        {150.5, 250.4, 201, 300},  // Very Unhealthy
        {250.5, 500.4, 301, 500}   // Hazardous
    };

    // Converts PM2.5 measurement to AQI
    // Example: PM2.5 of 35.2 → AQI of 99
    public static int calculateAqiFromPm25(Double pm25) {
        if (pm25 == null || pm25 < 0) {
            return 0;
        }

        pm25 = Math.floor(pm25 * 10) / 10; // Round to 1 decimal (EPA rule)

        // Find which range the PM2.5 value falls into
        for (double[] range : PM25_BREAKPOINTS) {
            if (pm25 >= range[0] && pm25 <= range[1]) {
                return interpolate(pm25, range[0], range[1], range[2], range[3]);
            }
        }

        return (pm25 > 500.4) ? 500 : 0; // Cap at 500 or return 0
    }

    // Converts PM10 measurement to AQI
    public static int calculateAqiFromPm10(Double pm10) {
        if (pm10 == null || pm10 < 0) {
            return 0;
        }

        double[][] breakpoints = {
            {0, 54, 0, 50},
            {55, 154, 51, 100},
            {155, 254, 101, 150},
            {255, 354, 151, 200},
            {355, 424, 201, 300},
            {425, 604, 301, 500}
        };

        for (double[] range : breakpoints) {
            if (pm10 >= range[0] && pm10 <= range[1]) {
                return interpolate(pm10, range[0], range[1], range[2], range[3]);
            }
        }

        return (pm10 > 604) ? 500 : 0;
    }

    // Linear interpolation - converts concentration to AQI
    // Think of it like: "If 12.0 PM2.5 = AQI 50, and 35.4 PM2.5 = AQI 100,
    //                    what's the AQI for 23.7 PM2.5?" Answer: ~75
    private static int interpolate(double value, double concLow, double concHigh, 
                                   double aqiLow, double aqiHigh) {
        double aqi = ((aqiHigh - aqiLow) / (concHigh - concLow)) * (value - concLow) + aqiLow;
        return (int) Math.round(aqi);
    }

    // Returns category: "Good", "Moderate", etc.
    public static String getAqiCategory(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    // Returns color code for UI display
    public static String getAqiColor(int aqi) {
        if (aqi <= 50) return "#00E400";    // Green
        if (aqi <= 100) return "#FFFF00";   // Yellow
        if (aqi <= 150) return "#FF7E00";   // Orange
        if (aqi <= 200) return "#FF0000";   // Red
        if (aqi <= 300) return "#8F3F97";   // Purple
        return "#7E0023";                    // Maroon
    }

    // Takes worst AQI from multiple pollutants
    public static int calculateCombinedAqi(Double pm25, Double pm10) {
        int aqiPm25 = calculateAqiFromPm25(pm25);
        int aqiPm10 = calculateAqiFromPm10(pm10);
        return Math.max(aqiPm25, aqiPm10);
    }
}
