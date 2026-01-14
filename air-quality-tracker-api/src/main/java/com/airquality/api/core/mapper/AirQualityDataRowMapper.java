package com.airquality.api.core.mapper;

import com.airquality.api.core.model.AirQualityData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * RowMapper for converting database rows to AirQualityData objects
 */
public class AirQualityDataRowMapper implements RowMapper<AirQualityData> {

    @Override
    public AirQualityData mapRow(ResultSet rs, int rowNum) throws SQLException {
        AirQualityData data = new AirQualityData();
        
        data.setId(rs.getLong("id"));
        data.setCity(rs.getString("city"));
        data.setCountry(rs.getString("country"));
        data.setLocationId(rs.getString("location_id"));
        
        // Handle nullable integers
        int aqi = rs.getInt("aqi");
        data.setAqi(rs.wasNull() ? null : aqi);
        
        // Handle nullable doubles
        double pm25 = rs.getDouble("pm25");
        data.setPm25(rs.wasNull() ? null : pm25);
        
        double pm10 = rs.getDouble("pm10");
        data.setPm10(rs.wasNull() ? null : pm10);
        
        double no2 = rs.getDouble("no2");
        data.setNo2(rs.wasNull() ? null : no2);
        
        double o3 = rs.getDouble("o3");
        data.setO3(rs.wasNull() ? null : o3);
        
        double co = rs.getDouble("co");
        data.setCo(rs.wasNull() ? null : co);
        
        double so2 = rs.getDouble("so2");
        data.setSo2(rs.wasNull() ? null : so2);
        
        double latitude = rs.getDouble("latitude");
        data.setLatitude(rs.wasNull() ? null : latitude);
        
        double longitude = rs.getDouble("longitude");
        data.setLongitude(rs.wasNull() ? null : longitude);
        
        // Handle timestamp
        Timestamp lastUpdated = rs.getTimestamp("last_updated");
        if (lastUpdated != null) {
            data.setLastUpdated(lastUpdated.toLocalDateTime());
        }
        
        return data;
    }
}
