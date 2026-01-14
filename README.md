# Global Air Quality Tracker - Full Stack Web Application

A **production-ready, enterprise-grade** Global Air Quality Monitoring Platform that tracks pollution data across 500+ cities worldwide, processes it reliably, and presents it through interactive dashboards.

## 🌍 Overview

This application demonstrates a complete full-stack architecture with:
- **Real-time data ingestion** from OpenAQ API v3
- **Polyglot persistence** (MySQL for core data, MongoDB for analytics)
- **AI-powered recommendations** using OpenAI
- **MVC architecture** with proper separation of concerns
- **Interactive dashboards** with charts and visualizations

## 📁 Project Structure

```
Global-Air-Quality-Tracker-App/
├── air-quality-tracker-api/     # Java Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/airquality/api/
│   │       ├── controller/      # REST Controllers
│   │       ├── service/         # Business Logic (Interface + Impl)
│   │       ├── repository/      # Database Access Layer
│   │       ├── model/           # Data Models
│   │       ├── client/          # External API Clients (OpenAQ)
│   │       ├── analytics/       # MongoDB Analytics Module
│   │       ├── ai/              # OpenAI Integration Module
│   │       ├── scheduler/       # Scheduled Tasks
│   │       ├── config/          # Configuration Classes
│   │       └── exception/       # Exception Handling
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── sql/                     # Database Scripts
│   └── pom.xml
│
└── air-quality-tracker-ui/      # Frontend Web Application
    ├── index.html               # Main Dashboard
    ├── analytics.html           # Analytics Dashboard
    ├── css/styles.css           # Custom Styles
    └── js/
        ├── config.js            # API Configuration
        ├── app.js               # Main App Logic
        └── analytics.js         # Analytics Logic
```

## 🚀 Features

### Backend Features
- ✅ **RESTful API** with comprehensive endpoints
- ✅ **Scheduled Data Refresh** every 6 hours from OpenAQ API
- ✅ **AQI Calculation** using EPA formula
- ✅ **MongoDB Analytics** tracking all API requests
- ✅ **AI Recommendations** powered by OpenAI
- ✅ **Spring Interceptor** for request metrics
- ✅ **Swagger/OpenAPI Documentation**
- ✅ **Health Monitoring** via Spring Actuator

### Frontend Features
- ✅ **Interactive Dashboard** with global statistics
- ✅ **City-wise Data Table** with search and sort
- ✅ **Highcharts Visualizations**
- ✅ **AQI Color Coding** (Good → Hazardous)
- ✅ **AI Recommendation Cards**
- ✅ **Analytics Dashboard** with API metrics

## 🛠️ Technology Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 11, Spring Boot 2.7 |
| Database | MySQL 8.0 |
| Analytics DB | MongoDB Atlas |
| Build Tool | Maven |
| External API | OpenAQ API v3 |
| AI | OpenAI GPT-3.5 |
| Frontend | HTML5, CSS3, JavaScript |
| UI Framework | Bootstrap 5 |
| Charts | Highcharts |

## 📦 Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL 8.0
- MongoDB Atlas account (free tier)
- OpenAQ API key (free)
- OpenAI API key (optional, for AI features)

## ⚙️ Setup Instructions

### 1. Database Setup

```bash
# Connect to MySQL
mysql -u root -p

# Run database scripts
source sql/01_create_database.sql
source sql/02_create_air_quality_data_table.sql
```

### 2. Configure Application Properties

Create `application-local.properties` in `src/main/resources/`:

```properties
# MySQL Password
spring.datasource.password=your_mysql_password

# MongoDB Atlas URI
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/air_quality_analytics

# OpenAQ API Key
openaq.api.key=your_openaq_api_key

# OpenAI API Key (optional)
openai.api.key=your_openai_api_key

# CORS Origins
cors.allowed.origins=http://localhost:5500,http://127.0.0.1:5500
```

### 3. Build and Run Backend

```bash
cd air-quality-tracker-api

# Build
mvn clean package

# Run
java -jar target/air-quality-tracker-api.jar
```

### 4. Run Frontend

Open `air-quality-tracker-ui/index.html` in a browser, or use Live Server:

```bash
cd air-quality-tracker-ui
# Use VS Code Live Server or Python HTTP server
python -m http.server 5500
```

## 🔌 API Endpoints

### Air Quality Data
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/global` | Global statistics |
| GET | `/api/cities` | All cities data |
| GET | `/api/city/{name}` | Specific city data |
| GET | `/api/countries` | List of countries |
| GET | `/api/country/{name}` | Cities in country |
| GET | `/api/rankings/polluted?limit=10` | Most polluted cities |
| GET | `/api/rankings/cleanest?limit=10` | Cleanest cities |
| POST | `/api/refresh` | Manual data refresh |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/summary` | Analytics overview |
| GET | `/api/analytics/timeline` | Recent requests |
| GET | `/api/analytics/endpoints` | Endpoint stats |

### AI Recommendations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/ai/recommendations/{city}` | AI recommendations |
| GET | `/api/ai/health-advisory?aqi=50` | Health advisory |
| GET | `/api/ai/analysis/{city}` | Trend analysis |

### Health & Docs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui/index.html` | API documentation |

## 🎨 AQI Color Scale

| AQI Range | Category | Color |
|-----------|----------|-------|
| 0-50 | Good | 🟢 Green |
| 51-100 | Moderate | 🟡 Yellow |
| 101-150 | Unhealthy for Sensitive | 🟠 Orange |
| 151-200 | Unhealthy | 🔴 Red |
| 201-300 | Very Unhealthy | 🟣 Purple |
| 301-500 | Hazardous | 🟤 Maroon |

## 🌐 AWS Deployment

1. Launch EC2 instance (Ubuntu 22.04, t2.micro)
2. Install Java, Maven, MySQL
3. Configure security groups (ports 80, 443, 8080)
4. Set up Nginx reverse proxy
5. Deploy JAR and static files
6. Configure Elastic IP
7. (Optional) Set up SSL with Let's Encrypt

See `docs/aws-deployment-guide.md` for detailed instructions.

## 📊 Data Flow

```
OpenAQ API → OpenAQApiClient → AirQualityService → Repository → MySQL
                                    ↓
                            AirQualityController → REST API → Frontend
                                    ↓
                         MetricsInterceptor → MongoDB Atlas
```

## 🔄 Scheduled Tasks

- **Data Refresh**: Every 6 hours (00:00, 06:00, 12:00, 18:00)
- **Initial Load**: On application startup if database is empty

## 📝 License

MIT License - feel free to use this project for learning and reference.

## 👨‍💻 Author

Built with ❤️ for learning enterprise Java development patterns.
