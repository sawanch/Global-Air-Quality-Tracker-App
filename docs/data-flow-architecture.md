# Data Flow Architecture: API to Database

## Complete Code Flow from OpenAQ API to MySQL Database

This document explains the complete data flow in the Global Air Quality Tracker application, from fetching data from the OpenAQ API to storing it in the MySQL database.

---

## 🔄 High-Level Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│          STEP 1: TRIGGER (Scheduled or Manual)              │
└─────────────────────────────────────────────────────────────┘

Option A: Scheduled (every 6 hours - 00:00, 06:00, 12:00, 18:00)
┌────────────────────────────────────────┐
│ AirQualityScheduler.java              │
│ @Scheduled(cron = "0 0 */6 * * *")    │
└────────────────────────────────────────┘
         ↓
    refreshDataScheduled()
         ↓
    airQualityService.refreshData()


Option B: Manual Refresh (via API endpoint)
┌────────────────────────────────────────┐
│ Controller: POST /api/refresh         │
└────────────────────────────────────────┘
         ↓
    airQualityService.refreshData()


┌─────────────────────────────────────────────────────────────┐
│          STEP 2: SERVICE LAYER - Orchestration              │
└─────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────┐
│ AirQualityServiceImpl.java                        │
│                                                    │
│ public int refreshData() {                        │
│    // 1. Call external API client                 │
│    List<AirQualityData> newData =                 │
│        openAQApiClient.fetchAirQualityData(50);   │
│                                                    │
│    // 2. Bulk upsert to database                  │
│    int rowsAffected =                             │
│        repository.bulkUpsert(newData);            │
│                                                    │
│    return rowsAffected;                           │
│ }                                                  │
└────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────┐
│          STEP 3: EXTERNAL API CLIENT - Fetch & Transform    │
└─────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│ OpenAQApiClient.java                                  │
│                                                        │
│ public List<AirQualityData> fetchAirQualityData() {   │
│                                                        │
│   // 3A. Fetch location IDs from OpenAQ             │
│   List<Long> locationIds = fetchLocationIds(50);     │
│   // Makes: GET https://api.openaq.org/v3/locations  │
│                                                        │
│   // 3B. For each location, fetch latest measurements│
│   for (Long locationId : locationIds) {              │
│       AirQualityData data =                          │
│           fetchLocationWithLatest(locationId);       │
│       // Makes: GET /v3/locations/{id}/latest        │
│       // Makes: GET /v3/locations/{id}               │
│                                                        │
│       // 3C. Transform OpenAQ JSON → AirQualityData  │
│       data = transformToAirQualityData(response);    │
│                                                        │
│       // 3D. Calculate AQI using EPA formula         │
│       int aqi = AqiCalculator.calculateAqi(pm25);    │
│       data.setAqi(aqi);                              │
│                                                        │
│       allData.add(data);                             │
│                                                        │
│       Thread.sleep(500); // Rate limiting            │
│   }                                                    │
│                                                        │
│   return allData; // List<AirQualityData>            │
│ }                                                      │
└────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────┐
│          STEP 4: DATA MODEL - Java Object                   │
└─────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────┐
│ AirQualityData.java (Model)                       │
│                                                    │
│ - id: Long                                        │
│ - city: String                    ← From OpenAQ   │
│ - country: String                 ← From OpenAQ   │
│ - locationId: Long                ← From OpenAQ   │
│ - aqi: Integer                    ← Calculated    │
│ - pm25: Double                    ← From OpenAQ   │
│ - pm10: Double                    ← From OpenAQ   │
│ - no2: Double                     ← From OpenAQ   │
│ - o3: Double                      ← From OpenAQ   │
│ - co: Double                      ← From OpenAQ   │
│ - so2: Double                     ← From OpenAQ   │
│ - latitude: Double                ← From OpenAQ   │
│ - longitude: Double               ← From OpenAQ   │
│ - lastUpdated: LocalDateTime      ← From OpenAQ   │
└────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────┐
│          STEP 5: REPOSITORY - Database Persistence           │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│ AirQualityRepository.java                               │
│                                                          │
│ @Transactional                                          │
│ public int bulkUpsert(List<AirQualityData> dataList) { │
│                                                          │
│   String query = """                                    │
│     INSERT INTO air_quality_data                       │
│     (city, country, location_id, aqi, pm25, pm10,      │
│      no2, o3, co, so2, latitude, longitude,            │
│      last_updated)                                      │
│     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)    │
│     ON DUPLICATE KEY UPDATE                            │
│       aqi = VALUES(aqi),                               │
│       pm25 = VALUES(pm25),                             │
│       ... (all fields updated)                         │
│       updated_at = CURRENT_TIMESTAMP                   │
│   """;                                                  │
│                                                          │
│   for (AirQualityData data : dataList) {               │
│       int rowsAffected = jdbcTemplate.update(query,    │
│           data.getCity(),                              │
│           data.getCountry(),                           │
│           data.getLocationId(),                        │
│           data.getAqi(),                               │
│           data.getPm25(),                              │
│           // ... all other fields                      │
│       );                                                │
│       totalRowsAffected += rowsAffected;               │
│   }                                                     │
│                                                          │
│   return totalRowsAffected;                            │
│ }                                                        │
└──────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────┐
│          STEP 6: MYSQL DATABASE - Final Storage              │
└─────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ MySQL Database: air_quality_db                      │
│ Table: air_quality_data                              │
│                                                      │
│ CREATE TABLE air_quality_data (                     │
│   id BIGINT AUTO_INCREMENT PRIMARY KEY,             │
│   city VARCHAR(255) NOT NULL,                       │
│   country VARCHAR(255) NOT NULL,                    │
│   location_id BIGINT,                               │
│   aqi INT,                                          │
│   pm25 DOUBLE,                                      │
│   pm10 DOUBLE,                                      │
│   no2 DOUBLE,                                       │
│   o3 DOUBLE,                                        │
│   co DOUBLE,                                        │
│   so2 DOUBLE,                                       │
│   latitude DOUBLE,                                  │
│   longitude DOUBLE,                                 │
│   last_updated TIMESTAMP,                           │
│   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   │
│   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP    │
│       ON UPDATE CURRENT_TIMESTAMP,                  │
│   UNIQUE KEY unique_city_country (city, country)    │
│ );                                                   │
└──────────────────────────────────────────────────────┘
```

---

## 📋 Detailed Step-by-Step Breakdown

### Step 1: Trigger Points

The data refresh process can be triggered in two ways:

#### A) Scheduled Refresh (Automatic)

**File:** `AirQualityScheduler.java`

```java
@Component
public class AirQualityScheduler {
    
    private final AirQualityService airQualityService;
    
    @Scheduled(cron = "0 0 */6 * * *")
    public void refreshDataScheduled() {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        logger.info("=== Scheduled data refresh started at {} ===", timestamp);
        
        try {
            int rowsAffected = airQualityService.refreshData();
            logger.info("=== Scheduled data refresh completed. {} rows affected ===", 
                rowsAffected);
        } catch (Exception e) {
            logger.error("=== Scheduled data refresh FAILED: {} ===", e.getMessage(), e);
        }
    }
}
```

**Schedule:** Runs every 6 hours (00:00, 06:00, 12:00, 18:00)

#### B) Manual Refresh (API Endpoint)

**Endpoint:** `POST /api/refresh`

```java
@PostMapping("/refresh")
public ResponseEntity<Map<String, Object>> refreshData() {
    int rowsAffected = airQualityService.refreshData();
    return ResponseEntity.ok(Map.of(
        "message", "Data refresh completed",
        "rowsAffected", rowsAffected
    ));
}
```

---

### Step 2: Service Layer Orchestration

**File:** `AirQualityServiceImpl.java`

The service layer orchestrates the entire data refresh process.

```java
@Service
public class AirQualityServiceImpl implements AirQualityService {
    
    private final AirQualityRepository airQualityRepository;
    private final OpenAQApiClient openAQApiClient;
    
    @Override
    @CacheEvict(value = {"globalStats", "cities", "city", "country", "countries"}, 
                allEntries = true)
    public int refreshData() {
        logger.info("Refreshing air quality data from OpenAQ API");
        
        try {
            // STEP 1: Fetch data from OpenAQ API
            List<AirQualityData> newData = openAQApiClient.fetchAirQualityData(50);
            
            if (newData.isEmpty()) {
                logger.warn("No data received from OpenAQ API");
                return 0;
            }
            
            logger.info("Received {} records from OpenAQ API, upserting to database", 
                newData.size());
            
            // STEP 2: Bulk upsert to database
            int rowsAffected = airQualityRepository.bulkUpsert(newData);
            
            logger.info("Data refresh completed. {} rows affected", rowsAffected);
            return rowsAffected;
            
        } catch (Exception e) {
            logger.error("Error refreshing air quality data", e);
            throw new DataRefreshException(
                "Failed to refresh air quality data: " + e.getMessage(), e);
        }
    }
}
```

**Key Responsibilities:**
- Calls external API client
- Handles empty responses
- Persists data to database
- Clears all caches after update
- Exception handling and logging

---

### Step 3: External API Client - Data Fetching & Transformation

**File:** `OpenAQApiClient.java`

This is the most complex step, involving multiple API calls and data transformation.

```java
@Component
public class OpenAQApiClient {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public List<AirQualityData> fetchAirQualityData(int limit) {
        logger.info("Fetching air quality data from OpenAQ API (limit: {})", limit);
        
        try {
            // STEP 3A: Fetch location IDs
            List<Long> locationIds = fetchLocationIds(50);
            logger.debug("Fetched {} location IDs", locationIds.size());
            
            if (locationIds.isEmpty()) {
                logger.warn("No locations found");
                return new ArrayList<>();
            }
            
            // STEP 3B: Fetch measurements for each location
            List<AirQualityData> allData = new ArrayList<>();
            for (int i = 0; i < locationIds.size(); i++) {
                try {
                    AirQualityData data = fetchLocationWithLatest(locationIds.get(i));
                    if (data != null && data.getAqi() > 0) {
                        allData.add(data);
                    }
                    
                    // STEP 3C: Rate limiting (500ms delay between requests)
                    if (i < locationIds.size() - 1) {
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    logger.debug("Error fetching data for location {}: {}", 
                        locationIds.get(i), e.getMessage());
                }
            }
            
            logger.info("Successfully fetched {} locations with measurements", 
                allData.size());
            return allData;
            
        } catch (Exception e) {
            logger.error("Error fetching data from OpenAQ API: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Fetch location IDs from OpenAQ
    private List<Long> fetchLocationIds(int limit) {
        String response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/locations")
                .queryParam("limit", limit)
                .queryParam("order_by", "lastUpdated")
                .queryParam("sort", "desc")
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .block();
        
        // Parse response and extract location IDs
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        List<Map<String, Object>> results = (List) responseMap.get("results");
        
        return results.stream()
            .map(loc -> ((Number) loc.get("id")).longValue())
            .collect(Collectors.toList());
    }
    
    // Fetch location with latest measurements
    private AirQualityData fetchLocationWithLatest(Long locationId) {
        // STEP 1: Fetch latest measurements
        String response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/locations/" + locationId + "/latest")
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .block();
        
        // Parse measurements
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        List<Map<String, Object>> results = (List) responseMap.get("results");
        
        if (results == null || results.isEmpty()) {
            return null;
        }
        
        // STEP 2: Fetch location details
        String locationResponse = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/locations/" + locationId)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .block();
        
        Map<String, Object> locationMap = objectMapper.readValue(
            locationResponse, Map.class);
        Map<String, Object> locationResults = (Map) locationMap.get("results");
        
        // STEP 3: Extract data and build model
        String city = (String) locationResults.get("city");
        Map<String, Object> countryMap = (Map) locationResults.get("country");
        String country = (String) countryMap.get("name");
        
        // Extract pollutant values
        Map<String, Double> pollutants = extractPollutants(results);
        
        // STEP 4: Calculate AQI using EPA formula
        int aqi = AqiCalculator.calculateAqi(
            pollutants.get("pm25"),
            pollutants.get("pm10"),
            pollutants.get("o3"),
            pollutants.get("no2"),
            pollutants.get("co"),
            pollutants.get("so2")
        );
        
        // STEP 5: Build and return AirQualityData object
        AirQualityData data = new AirQualityData();
        data.setCity(city);
        data.setCountry(country);
        data.setLocationId(locationId);
        data.setAqi(aqi);
        data.setPm25(pollutants.get("pm25"));
        data.setPm10(pollutants.get("pm10"));
        data.setNo2(pollutants.get("no2"));
        data.setO3(pollutants.get("o3"));
        data.setCo(pollutants.get("co"));
        data.setSo2(pollutants.get("so2"));
        // Set coordinates and timestamp...
        
        return data;
    }
}
```

**API Endpoints Called:**
1. `GET https://api.openaq.org/v3/locations` - Get location IDs
2. `GET https://api.openaq.org/v3/locations/{id}/latest` - Get latest measurements
3. `GET https://api.openaq.org/v3/locations/{id}` - Get location details

**Key Operations:**
- Fetch location IDs (50 locations)
- For each location:
  - Fetch latest measurements (PM2.5, PM10, O3, NO2, CO, SO2)
  - Fetch location details (city, country, coordinates)
  - Calculate AQI using EPA formula
  - Transform JSON → Java object
  - Rate limiting (500ms delay)

---

### Step 4: Data Model

**File:** `AirQualityData.java`

The model class represents air quality data for a single city.

```java
public class AirQualityData {
    private Long id;
    private String city;              // From OpenAQ
    private String country;           // From OpenAQ
    private Long locationId;          // From OpenAQ
    private Integer aqi;              // Calculated using EPA formula
    private Double pm25;              // From OpenAQ (µg/m³)
    private Double pm10;              // From OpenAQ (µg/m³)
    private Double no2;               // From OpenAQ (ppb)
    private Double o3;                // From OpenAQ (ppb)
    private Double co;                // From OpenAQ (ppm)
    private Double so2;               // From OpenAQ (ppb)
    private Double latitude;          // From OpenAQ
    private Double longitude;         // From OpenAQ
    private LocalDateTime lastUpdated; // From OpenAQ
    
    // Getters and setters...
}
```

**Data Sources:**
- **From OpenAQ API**: City, country, location ID, pollutant values, coordinates
- **Calculated**: AQI (Air Quality Index) using EPA formula

---

### Step 5: Repository - Database Persistence

**File:** `AirQualityRepository.java`

The repository handles database operations using Spring JDBC Template.

```java
@Repository
public class AirQualityRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional
    public int bulkUpsert(List<AirQualityData> dataList) {
        logger.info("Bulk upserting {} air quality records", dataList.size());
        
        String query = "INSERT INTO air_quality_data " +
                       "(city, country, location_id, aqi, pm25, pm10, no2, o3, co, so2, " +
                       " latitude, longitude, last_updated) " +
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
                logger.error("Error upserting data for city: {} in {}", 
                    data.getCity(), data.getCountry(), e);
                throw e; // Transaction will rollback
            }
        }
        
        logger.info("Bulk upsert completed. {} rows affected", totalRowsAffected);
        return totalRowsAffected;
    }
}
```

**Key Features:**
- **UPSERT operation**: `INSERT ... ON DUPLICATE KEY UPDATE`
  - If city+country exists → UPDATE all fields
  - If city+country doesn't exist → INSERT new record
- **Transactional**: All-or-nothing approach
- **Batch processing**: Processes all records in one transaction
- **HikariCP connection pooling**: Efficient database connections

---

### Step 6: MySQL Database Storage

**Database:** `air_quality_db`  
**Table:** `air_quality_data`

```sql
CREATE TABLE air_quality_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    location_id BIGINT,
    aqi INT,
    pm25 DOUBLE,
    pm10 DOUBLE,
    no2 DOUBLE,
    o3 DOUBLE,
    co DOUBLE,
    so2 DOUBLE,
    latitude DOUBLE,
    longitude DOUBLE,
    last_updated TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_city_country (city, country)
);
```

**Key Features:**
- **Primary Key**: Auto-incrementing ID
- **Unique Constraint**: city + country combination (prevents duplicates)
- **Timestamps**: 
  - `created_at`: Set once on insert
  - `updated_at`: Auto-updates on every modification
  - `last_updated`: From OpenAQ (when measurement was taken)
- **Indexes**: Unique key on (city, country) for fast lookups

---

## 🔍 Data Transformation Example

### OpenAQ JSON Response → Java Object → SQL Record

#### 1. OpenAQ API Response (JSON)

```json
{
  "results": [
    {
      "locationId": 12345,
      "location": "US Diplomatic Post: Mumbai",
      "city": "Mumbai",
      "country": {
        "id": "IN",
        "name": "India"
      },
      "coordinates": {
        "latitude": 19.0760,
        "longitude": 72.8777
      },
      "parameter": {
        "id": 2,
        "name": "pm25"
      },
      "value": 45.5,
      "unit": "µg/m³",
      "datetime": {
        "utc": "2026-01-14T10:00:00Z"
      }
    }
  ]
}
```

#### 2. Java Object (AirQualityData)

```java
AirQualityData data = new AirQualityData();
data.setCity("Mumbai");
data.setCountry("India");
data.setLocationId(12345L);
data.setPm25(45.5);
data.setAqi(125); // Calculated from PM2.5 value
data.setLatitude(19.0760);
data.setLongitude(72.8777);
data.setLastUpdated(LocalDateTime.parse("2026-01-14T10:00:00"));
```

#### 3. SQL Record

```sql
INSERT INTO air_quality_data 
(city, country, location_id, aqi, pm25, latitude, longitude, last_updated)
VALUES 
('Mumbai', 'India', 12345, 125, 45.5, 19.0760, 72.8777, '2026-01-14 10:00:00');
```

---

## ⚙️ Configuration

### Connection Pool (HikariCP)

```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Scheduler Configuration

```properties
# Data refresh interval (6 hours)
scheduler.data.refresh.rate=21600000
```

### OpenAQ API Configuration

```properties
openaq.api.url=https://api.openaq.org/v3
openaq.api.key=${OPENAQ_API_KEY}
```

---

## 🎯 Key Design Patterns & Best Practices

### 1. **Separation of Concerns**
- **Scheduler**: Triggers only
- **Service**: Business logic orchestration
- **Client**: External API communication
- **Repository**: Database operations

### 2. **Transaction Management**
- `@Transactional` ensures all-or-nothing database operations
- Rollback on any exception

### 3. **Rate Limiting**
- 500ms delay between API requests
- Prevents hitting OpenAQ rate limits
- Configurable limit (50 locations)

### 4. **Error Handling**
- Try-catch at each layer
- Graceful degradation
- Comprehensive logging

### 5. **Cache Management**
- `@CacheEvict` clears all caches after refresh
- Ensures fresh data is served to clients

### 6. **UPSERT Pattern**
- Insert new records
- Update existing records
- Single query for both operations

### 7. **Connection Pooling**
- HikariCP manages database connections
- Reuses connections for efficiency
- Configurable pool size

---

## 📊 Performance Considerations

### Bottlenecks & Solutions

| Bottleneck | Solution |
|------------|----------|
| External API rate limits | 500ms delay between requests, limit to 50 locations |
| Database write performance | Bulk upsert, connection pooling |
| Memory usage | Stream processing, garbage collection |
| Network latency | WebClient with timeout configuration |

### Expected Performance

- **API fetch time**: ~30-40 seconds (50 locations × 500ms + API response time)
- **Database upsert time**: ~2-3 seconds (50 records)
- **Total refresh time**: ~35-45 seconds
- **Database query time**: <100ms (with proper indexing)

---

## 🔧 Troubleshooting

### Common Issues

**1. OpenAQ API Timeout**
```
Error: Read timed out
Solution: Check network connectivity, increase timeout configuration
```

**2. Database Connection Pool Exhausted**
```
Error: Connection pool exhausted
Solution: Increase maximum-pool-size or check for connection leaks
```

**3. Duplicate Key Violation**
```
Error: Duplicate entry for key 'unique_city_country'
Solution: This should be handled by UPSERT, check unique constraint
```

**4. AQI Calculation Returns 0**
```
Issue: All pollutant values are null
Solution: OpenAQ might not have measurements for that location, filtered out
```

---

## 📝 Summary

### Complete Flow in One Sentence

**Scheduler triggers** → **Service orchestrates** → **Client fetches from OpenAQ** → **Transform JSON to Java** → **Calculate AQI** → **Repository upserts to MySQL** → **Data available via REST API**

### Key Components

1. **AirQualityScheduler** - Triggers refresh every 6 hours
2. **AirQualityServiceImpl** - Orchestrates the process
3. **OpenAQApiClient** - Fetches and transforms external data
4. **AirQualityData** - Data model
5. **AirQualityRepository** - Database operations
6. **MySQL Database** - Final storage

### Technologies Used

- **Spring Boot** - Application framework
- **Spring JDBC** - Database access
- **WebClient** - HTTP client for external API
- **HikariCP** - Connection pooling
- **Jackson** - JSON parsing
- **MySQL** - Relational database

---

**Document Version:** 1.0  
**Last Updated:** January 14, 2026  
**Author:** Sawan Chakraborty
