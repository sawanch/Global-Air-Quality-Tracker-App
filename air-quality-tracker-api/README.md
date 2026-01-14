# Air Quality Tracker - Backend API

Spring Boot REST API for the Global Air Quality Monitoring Platform.

## 🏗️ Architecture

```
com.airquality.api/
├── AirQualityTrackerApiApplication.java   # Main entry point
├── controller/                            # REST Controllers
│   └── AirQualityController.java         # Air quality endpoints
├── service/                               # Business Logic
│   ├── AirQualityService.java            # Interface
│   └── impl/AirQualityServiceImpl.java   # Implementation
├── repository/                            # Data Access
│   └── AirQualityRepository.java         # JdbcTemplate operations
├── model/                                 # Data Models
│   ├── AirQualityData.java               # City air quality
│   ├── GlobalAirQualityStats.java        # Aggregated stats
│   └── ErrorResponse.java                # Error format
├── client/openaq/                         # External API
│   ├── OpenAQApiClient.java              # API client
│   └── model/                            # Response models
├── analytics/                             # MongoDB Module
│   ├── controller/AnalyticsController.java
│   ├── service/AnalyticsService.java
│   ├── repository/AnalyticsRepository.java
│   ├── model/ApiRequestMetric.java
│   └── interceptor/MetricsInterceptor.java
├── ai/                                    # OpenAI Module
│   ├── controller/AiController.java
│   ├── service/AiService.java
│   └── model/AirQualityRecommendation.java
├── scheduler/                             # Scheduled Tasks
│   └── AirQualityScheduler.java
├── initializer/                           # Startup Tasks
│   └── AirQualityDataInitializer.java
├── mapper/                                # Database Mappers
│   └── AirQualityDataRowMapper.java
├── config/                                # Configuration
│   └── CorsConfig.java
├── exception/                             # Error Handling
│   ├── GlobalExceptionHandler.java
│   ├── CityNotFoundException.java
│   └── ApiServiceException.java
└── util/                                  # Utilities
    └── AqiCalculator.java                # EPA AQI formula
```

## 📦 Dependencies

| Dependency | Purpose |
|------------|---------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-jdbc | MySQL connectivity |
| mysql-connector-j | MySQL driver |
| spring-boot-starter-data-mongodb | MongoDB analytics |
| spring-boot-starter-actuator | Health monitoring |
| springdoc-openapi-ui | Swagger documentation |

## 🔧 Configuration

### application.properties
Key configuration properties:

```properties
# Server
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/air_quality_db
spring.datasource.username=root

# MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://...

# OpenAQ API
openaq.api.url=https://api.openaq.org/v3
openaq.api.key=${OPENAQ_API_KEY:}

# OpenAI API
openai.api.url=https://api.openai.com/v1/chat/completions
openai.api.key=${OPENAI_API_KEY:}
```

### Environment-Specific Configuration
Create `application-local.properties` for local development secrets.

## 🚀 Build & Run

```bash
# Build
mvn clean package

# Run
java -jar target/air-quality-tracker-api.jar

# Run with profile
java -jar target/air-quality-tracker-api.jar --spring.profiles.active=prod
```

## 🔌 API Endpoints

### Air Quality
- `GET /api/global` - Global statistics
- `GET /api/cities` - All cities
- `GET /api/city/{name}` - Specific city
- `GET /api/countries` - List of countries
- `GET /api/country/{name}` - Cities in country
- `GET /api/rankings/polluted` - Most polluted
- `GET /api/rankings/cleanest` - Cleanest cities
- `POST /api/refresh` - Refresh data

### Analytics
- `GET /api/analytics/summary` - Analytics overview
- `GET /api/analytics/timeline` - Request timeline
- `GET /api/analytics/endpoints` - Endpoint stats

### AI
- `GET /api/ai/recommendations/{city}` - AI recommendations
- `GET /api/ai/health-advisory` - Health advisory
- `GET /api/ai/analysis/{city}` - Trend analysis

### Actuator
- `GET /actuator/health` - Health check
- `GET /actuator/info` - App info
- `GET /actuator/metrics` - Metrics

## 🔄 Scheduled Tasks

| Task | Schedule | Description |
|------|----------|-------------|
| Data Refresh | Every 6 hours | Fetches latest data from OpenAQ |
| Initial Load | On startup | Loads data if database is empty |

## 📊 AQI Calculation

Using EPA formula to convert PM2.5 to AQI:

| PM2.5 (μg/m³) | AQI Range | Category |
|---------------|-----------|----------|
| 0-12.0 | 0-50 | Good |
| 12.1-35.4 | 51-100 | Moderate |
| 35.5-55.4 | 101-150 | Unhealthy for Sensitive |
| 55.5-150.4 | 151-200 | Unhealthy |
| 150.5-250.4 | 201-300 | Very Unhealthy |
| 250.5+ | 301-500 | Hazardous |

## 📝 Database Schema

```sql
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
    UNIQUE KEY uk_city_country (city, country)
);
```
