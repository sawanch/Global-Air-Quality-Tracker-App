package com.airquality.api.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing air quality data for a single city/location
 * Maps to the air_quality_data database table
 */
public class AirQualityData {
    
    private Long id;
    private String city;
    private String country;
    private String locationId;
    private Integer aqi;           // Air Quality Index (calculated from PM2.5)
    private Double pm25;           // Fine particles (μg/m³)
    private Double pm10;           // Coarse particles (μg/m³)
    private Double no2;            // Nitrogen dioxide (ppb)
    private Double o3;             // Ozone (ppb)
    private Double co;             // Carbon monoxide (ppm)
    private Double so2;            // Sulfur dioxide (ppb)
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;

    public AirQualityData() {
    }

    public AirQualityData(String city, String country, Integer aqi, Double pm25, 
                          Double pm10, Double no2, Double o3, Double latitude, 
                          Double longitude, LocalDateTime lastUpdated) {
        this.city = city;
        this.country = country;
        this.aqi = aqi;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.no2 = no2;
        this.o3 = o3;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = lastUpdated;
    }

    // Builder pattern for convenience
    public static AirQualityDataBuilder builder() {
        return new AirQualityDataBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getO3() {
        return o3;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Returns the AQI category based on EPA standards
     */
    public String getAqiCategory() {
        if (aqi == null) return "Unknown";
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirQualityData that = (AirQualityData) o;
        return Objects.equals(city, that.city) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, country);
    }

    @Override
    public String toString() {
        return "AirQualityData{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", aqi=" + aqi +
                ", pm25=" + pm25 +
                ", pm10=" + pm10 +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    // Builder class
    public static class AirQualityDataBuilder {
        private final AirQualityData data = new AirQualityData();

        public AirQualityDataBuilder id(Long id) {
            data.setId(id);
            return this;
        }

        public AirQualityDataBuilder city(String city) {
            data.setCity(city);
            return this;
        }

        public AirQualityDataBuilder country(String country) {
            data.setCountry(country);
            return this;
        }

        public AirQualityDataBuilder locationId(String locationId) {
            data.setLocationId(locationId);
            return this;
        }

        public AirQualityDataBuilder aqi(Integer aqi) {
            data.setAqi(aqi);
            return this;
        }

        public AirQualityDataBuilder pm25(Double pm25) {
            data.setPm25(pm25);
            return this;
        }

        public AirQualityDataBuilder pm10(Double pm10) {
            data.setPm10(pm10);
            return this;
        }

        public AirQualityDataBuilder no2(Double no2) {
            data.setNo2(no2);
            return this;
        }

        public AirQualityDataBuilder o3(Double o3) {
            data.setO3(o3);
            return this;
        }

        public AirQualityDataBuilder co(Double co) {
            data.setCo(co);
            return this;
        }

        public AirQualityDataBuilder so2(Double so2) {
            data.setSo2(so2);
            return this;
        }

        public AirQualityDataBuilder latitude(Double latitude) {
            data.setLatitude(latitude);
            return this;
        }

        public AirQualityDataBuilder longitude(Double longitude) {
            data.setLongitude(longitude);
            return this;
        }

        public AirQualityDataBuilder lastUpdated(LocalDateTime lastUpdated) {
            data.setLastUpdated(lastUpdated);
            return this;
        }

        public AirQualityData build() {
            return data;
        }
    }
}
