package com.airquality.api.core.model;

/**
 * Model representing aggregated global air quality statistics
 * Calculated by aggregating data from all cities
 */
public class GlobalAirQualityStats {
    
    private int totalCities;
    private int totalCountries;
    private double averageGlobalAqi;
    private int citiesWithGoodAir;        // AQI 0-50
    private int citiesWithModerateAir;    // AQI 51-100
    private int citiesWithUnhealthyAir;   // AQI 101+
    private String cleanestCity;
    private String cleanestCountry;
    private Integer cleanestAqi;
    private String mostPollutedCity;
    private String mostPollutedCountry;
    private Integer mostPollutedAqi;
    private String lastUpdated;

    public GlobalAirQualityStats() {
    }

    // Getters and Setters
    public int getTotalCities() {
        return totalCities;
    }

    public void setTotalCities(int totalCities) {
        this.totalCities = totalCities;
    }

    public int getTotalCountries() {
        return totalCountries;
    }

    public void setTotalCountries(int totalCountries) {
        this.totalCountries = totalCountries;
    }

    public double getAverageGlobalAqi() {
        return averageGlobalAqi;
    }

    public void setAverageGlobalAqi(double averageGlobalAqi) {
        this.averageGlobalAqi = averageGlobalAqi;
    }

    public int getCitiesWithGoodAir() {
        return citiesWithGoodAir;
    }

    public void setCitiesWithGoodAir(int citiesWithGoodAir) {
        this.citiesWithGoodAir = citiesWithGoodAir;
    }

    public int getCitiesWithModerateAir() {
        return citiesWithModerateAir;
    }

    public void setCitiesWithModerateAir(int citiesWithModerateAir) {
        this.citiesWithModerateAir = citiesWithModerateAir;
    }

    public int getCitiesWithUnhealthyAir() {
        return citiesWithUnhealthyAir;
    }

    public void setCitiesWithUnhealthyAir(int citiesWithUnhealthyAir) {
        this.citiesWithUnhealthyAir = citiesWithUnhealthyAir;
    }

    public String getCleanestCity() {
        return cleanestCity;
    }

    public void setCleanestCity(String cleanestCity) {
        this.cleanestCity = cleanestCity;
    }

    public String getCleanestCountry() {
        return cleanestCountry;
    }

    public void setCleanestCountry(String cleanestCountry) {
        this.cleanestCountry = cleanestCountry;
    }

    public Integer getCleanestAqi() {
        return cleanestAqi;
    }

    public void setCleanestAqi(Integer cleanestAqi) {
        this.cleanestAqi = cleanestAqi;
    }

    public String getMostPollutedCity() {
        return mostPollutedCity;
    }

    public void setMostPollutedCity(String mostPollutedCity) {
        this.mostPollutedCity = mostPollutedCity;
    }

    public String getMostPollutedCountry() {
        return mostPollutedCountry;
    }

    public void setMostPollutedCountry(String mostPollutedCountry) {
        this.mostPollutedCountry = mostPollutedCountry;
    }

    public Integer getMostPollutedAqi() {
        return mostPollutedAqi;
    }

    public void setMostPollutedAqi(Integer mostPollutedAqi) {
        this.mostPollutedAqi = mostPollutedAqi;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "GlobalAirQualityStats{" +
                "totalCities=" + totalCities +
                ", totalCountries=" + totalCountries +
                ", averageGlobalAqi=" + averageGlobalAqi +
                ", citiesWithGoodAir=" + citiesWithGoodAir +
                ", citiesWithUnhealthyAir=" + citiesWithUnhealthyAir +
                ", cleanestCity='" + cleanestCity + '\'' +
                ", mostPollutedCity='" + mostPollutedCity + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }
}
