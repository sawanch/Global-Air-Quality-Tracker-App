# Global Air Quality Tracker - High-Level System Design

```
┌───────────────────────────────────────────────────────────┐
│ UI Client (Web)                                           │
│ [HTML5, CSS3, JavaScript, Bootstrap 5, Highcharts]        │
│                                                           │
│ Pages:                                                    │
│  • index.html       → Main Dashboard (Global Stats)      │
│  • analytics.html   → Analytics Dashboard (Metrics)      │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│ Load Balancer                                             │
│ [AWS ALB / NGINX]                                         │
│                                                           │
│ • SSL Termination                                         │
│ • Reverse Proxy                                           │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│ Backend Service (Module-based Architecture)               │
│ [Java 11, Spring Boot 2.7, Maven]                        │
│ [Separation of Concerns - 4 Modules]                     │
│                                                           │
│ ├─ Authentication / Authorization                         │
│ │  [Future: OAuth, JWT - Currently Open]                 │
│ │                                                         │
│ ├─ Core Module (Business Logic)                          │
│ │  ├─ Controller  → REST API endpoints                   │
│ │  ├─ Service     → Business logic layer                 │
│ │  ├─ Repository  → JDBC data access                     │
│ │  ├─ Model       → Domain objects                       │
│ │  ├─ Mapper      → Row mappers                          │
│ │  └─ Initializer → Startup data loader                  │
│ │                                                         │
│ ├─ Analytics Module (Observability)                      │
│ │  ├─ Interceptor → Capture API metrics                  │
│ │  ├─ Repository  → MongoDB data access                  │
│ │  ├─ Service     → Analytics aggregation                │
│ │  └─ Model       → Metric objects                       │
│ │                                                         │
│ ├─ Intelligence Module (AI/ML Integration)               │
│ │  ├─ Service     → OpenAI integration                   │
│ │  ├─ Controller  → AI endpoints                         │
│ │  └─ Model       → Recommendation objects               │
│ │                                                         │
│ └─ Shared Module (Cross-cutting Concerns)                │
│    ├─ External   → OpenAQ API client                     │
│    ├─ Config     → CORS, MongoDB, Cache                  │
│    ├─ Exception  → Global error handling                 │
│    ├─ Scheduler  → Scheduled data refresh                │
│    └─ Util       → AQI calculator, helpers               │
│                                                           │
│ Logging / Monitoring (Observability)                     │
│ [SLF4J + Logback, Spring Actuator]                       │
│  • File logging (10MB rolling)                           │
│  • Health checks (/actuator/health)                      │
│  • Metrics endpoint (/actuator/metrics)                  │
│  • Swagger/OpenAPI docs                                  │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│ Cache (Optional - Currently Disabled)                     │
│ [Redis]                                                   │
│                                                           │
│ • Configured but not active                               │
│ • Ready for future performance optimization               │
└───────────────────────────────────────────────────────────┘
                              ↓
        ┌─────────────────────┴─────────────────────┐
        │                                           │
        ↓                                           ↓
┌──────────────────────────┐       ┌──────────────────────────┐
│ Database (MySQL 8.0)     │       │ Database (MongoDB Atlas) │
│ [SQL - Transactional]    │       │ [NoSQL - Analytics]      │
│                          │       │                          │
│ Schema: air_quality_db   │       │ Database:                │
│                          │       │  air_quality_analytics   │
│ Tables:                  │       │                          │
│  • air_quality_data      │       │ Collections:             │
│    - id (PK)             │       │  • api_request_metrics   │
│    - city                │       │    - requestPath         │
│    - country             │       │    - method              │
│    - aqi                 │       │    - responseTime        │
│    - pm25, pm10, o3, etc.│       │    - statusCode          │
│    - last_updated        │       │    - timestamp           │
│                          │       │    - clientIp            │
│ Use Case:                │       │    - userAgent           │
│  • Store air quality     │       │                          │
│    measurements          │       │ Use Case:                │
│  • CRUD operations       │       │  • API analytics         │
│  • City/country queries  │       │  • Performance metrics   │
│  • Rankings (top/worst)  │       │  • Usage tracking        │
│                          │       │  • Dashboards            │
│ HikariCP Connection Pool:│       │                          │
│  • max-pool-size: 20     │       │ (Behind MongoDB Atlas    │
│  • min-idle: 5           │       │  managed firewall)       │
└──────────────────────────┘       └──────────────────────────┘

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Side Systems (Off the Critical Request Path)
Core system responds in milliseconds;
these systems can take more time
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌───────────────────────────────────────────────────────────┐
│ External APIs (Data Sources)                              │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐  │
│ │ OpenAQ API v3                                       │  │
│ │ [https://api.openaq.org/v3]                        │  │
│ │                                                     │  │
│ │ • Global air quality measurements                   │  │
│ │ • 500+ cities worldwide                             │  │
│ │ • PM2.5, PM10, O3, NO2, SO2, CO data               │  │
│ │ • Rate limited: 500ms delay between requests        │  │
│ │                                                     │  │
│ │ Endpoints Used:                                     │  │
│ │  • GET /locations (fetch location IDs)             │  │
│ │  • GET /locations/{id}/latest (measurements)       │  │
│ │  • GET /locations/{id} (location details)          │  │
│ │                                                     │  │
│ │ Fetched via:                                        │  │
│ │  • Scheduled task (every 6 hours)                   │  │
│ │  • Manual refresh endpoint                          │  │
│ │  • Application startup (if DB empty)                │  │
│ └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
                              ↓
                    Data Flow: OpenAQ API
                              ↓
            OpenAQApiClient → AirQualityService
                              ↓
                    Repository → MySQL DB
                              ↓
            (Available via REST API to Frontend)

┌───────────────────────────────────────────────────────────┐
│ Scheduled Tasks (Spring @Scheduled)                       │
│ [AirQualityScheduler]                                     │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐  │
│ │ Data Refresh Job                                    │  │
│ │ • Frequency: Every 6 hours (21,600,000 ms)          │  │
│ │ • Times: 00:00, 06:00, 12:00, 18:00                │  │
│ │                                                     │  │
│ │ Tasks:                                              │  │
│ │  1. Fetch latest data from OpenAQ API              │  │
│ │  2. Calculate AQI using EPA formula                │  │
│ │  3. Update MySQL database                          │  │
│ │  4. Log sync status                                │  │
│ └─────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│ AI / ML Engine                                            │
│ [AI (goal) → ML (learning)]                               │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐  │
│ │ OpenAI Integration                                  │  │
│ │ [External Service: api.openai.com]                 │  │
│ │                                                     │  │
│ │ Model: GPT-3.5-Turbo (Cost-optimized)             │  │
│ │                                                     │  │
│ │ AI Capabilities:                                    │  │
│ │  ├─ Personalized Health Recommendations            │  │
│ │  │   • Based on current AQI levels                 │  │
│ │  │   • Sensitive groups considerations             │  │
│ │  │   • Activity recommendations                    │  │
│ │  │                                                  │  │
│ │  ├─ Health Advisory Generation                     │  │
│ │  │   • Real-time risk assessment                   │  │
│ │  │   • Protective measures                         │  │
│ │  │   • Medical advice for high AQI                 │  │
│ │  │                                                  │  │
│ │  └─ Trend Analysis & Insights                      │  │
│ │      • Historical pattern analysis                 │  │
│ │      • Prediction insights                         │  │
│ │      • Comparative city analysis                   │  │
│ │                                                     │  │
│ │ Fallback:                                           │  │
│ │  • Rule-based recommendations if OpenAI unavailable │  │
│ │  • Graceful degradation                            │  │
│ │                                                     │  │
│ │ API Endpoints:                                      │  │
│ │  • GET /api/ai/recommendations/{city}              │  │
│ │  • GET /api/ai/health-advisory?aqi={value}         │  │
│ │  • GET /api/ai/analysis/{city}                     │  │
│ └─────────────────────────────────────────────────────┘  │
│                                                           │
│ (Optional insights/predictions can be sent back to        │
│  Backend for caching or to Frontend for display)         │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│ Analytics Platform                                        │
│ [MongoDB Atlas - Managed Cloud Service]                   │
│                                                           │
│ Purpose: API Request Metrics & Usage Analytics            │
│                                                           │
│ Receives data from:                                       │
│  • MetricsInterceptor (async, non-blocking)              │
│  • Captures every /api/** request                        │
│                                                           │
│ Stored Data:                                              │
│  • Request path & method                                  │
│  • Response time (performance)                            │
│  • Status codes (success/error rates)                     │
│  • Timestamps (time-series analysis)                      │
│  • Client IP & User-Agent (usage patterns)               │
│                                                           │
│ Analytics Endpoints:                                      │
│  • GET /api/analytics/summary                             │
│    → Total requests, avg response time, error rate        │
│  • GET /api/analytics/timeline                            │
│    → Recent requests timeline                             │
│  • GET /api/analytics/endpoints                           │
│    → Per-endpoint statistics                              │
│                                                           │
│ Visualization:                                            │
│  • Analytics Dashboard (analytics.html)                   │
│  • Highcharts time-series graphs                         │
│  • Real-time metrics display                             │
└───────────────────────────────────────────────────────────┘

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Data Flow Summary
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Data Ingestion Flow:
   OpenAQ API → OpenAQApiClient → AirQualityService
                                        ↓
                            AQI Calculation (AqiCalculator)
                                        ↓
                            Repository → MySQL Database

2. Client Request Flow:
   Frontend → NGINX/ALB → Controller → Service → Repository → MySQL
                            ↓                          
                      MetricsInterceptor (async)
                            ↓
                      MongoDB Atlas

3. AI Recommendation Flow:
   Frontend → Controller → AiService → OpenAI API
                            ↓
                    (with fallback to rule-based)
                            ↓
                      Response to Frontend

4. Analytics Flow:
   MetricsInterceptor → MongoDB Atlas → AnalyticsService → Frontend

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Key Architectural Patterns
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Separation of Concerns
   • Modular architecture (core, analytics, intelligence, shared)
   • Clear layer separation (Controller → Service → Repository)

2. Polyglot Persistence
   • MySQL for transactional data (air quality measurements)
   • MongoDB for analytics (metrics, time-series)

3. Asynchronous Processing
   • Non-blocking metrics collection
   • Background data refresh
   • CompletableFuture for analytics writes

4. External Service Integration
   • OpenAQ API for real-time data
   • OpenAI for AI capabilities
   • Graceful degradation on failure

5. Observability
   • Structured logging (SLF4J + Logback)
   • Health checks (Spring Actuator)
   • Request metrics tracking
   • Performance monitoring

6. Caching Strategy (Planned)
   • Redis ready for future optimization
   • Can cache frequent queries
   • Can cache AI responses

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Deployment Architecture (AWS)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

┌─────────────────────────────────────────────────────┐
│ AWS Cloud                                           │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │ Route 53 (DNS)                                │ │
│  └───────────────┬───────────────────────────────┘ │
│                  ↓                                  │
│  ┌───────────────────────────────────────────────┐ │
│  │ CloudFront (CDN - Optional)                   │ │
│  └───────────────┬───────────────────────────────┘ │
│                  ↓                                  │
│  ┌───────────────────────────────────────────────┐ │
│  │ Application Load Balancer (ALB)               │ │
│  │ • SSL/TLS termination                         │ │
│  │ • Health checks                               │ │
│  └───────────────┬───────────────────────────────┘ │
│                  ↓                                  │
│  ┌───────────────────────────────────────────────┐ │
│  │ EC2 Instance (Ubuntu 22.04, t2.micro)         │ │
│  │                                               │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │ NGINX (Reverse Proxy)                   │ │ │
│  │  └─────────────┬───────────────────────────┘ │ │
│  │                ↓                              │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │ Spring Boot App (Port 8080)             │ │ │
│  │  │ • JAR deployment                        │ │ │
│  │  │ • Java 11                               │ │ │
│  │  └─────────────┬───────────────────────────┘ │ │
│  │                ↓                              │ │
│  │  ┌─────────────────────────────────────────┐ │ │
│  │  │ MySQL 8.0 (Local or RDS)                │ │ │
│  │  └─────────────────────────────────────────┘ │ │
│  │                                               │ │
│  │ Static Files:                                 │ │
│  │  /var/www/html (Frontend)                    │ │
│  │                                               │ │
│  │ Security Groups:                              │ │
│  │  • Port 80 (HTTP)                            │ │
│  │  • Port 443 (HTTPS)                          │ │
│  │  • Port 8080 (Internal)                      │ │
│  │  • Port 3306 (MySQL - if RDS)                │ │
│  └───────────────────────────────────────────────┘ │
│                                                     │
│  ┌───────────────────────────────────────────────┐ │
│  │ External Services                             │ │
│  │  • MongoDB Atlas (Analytics)                 │ │
│  │  • OpenAQ API (Air Quality Data)             │ │
│  │  • OpenAI API (AI Recommendations)           │ │
│  └───────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Technology Stack Summary
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Frontend:
  • HTML5, CSS3, JavaScript (ES6+)
  • Bootstrap 5 (UI framework)
  • Highcharts (Data visualization)

Backend:
  • Java 11
  • Spring Boot 2.7
  • Spring Web MVC
  • Spring Data JDBC
  • Spring Data MongoDB
  • Spring Actuator
  • Maven (Build tool)
  • SLF4J + Logback (Logging)
  • SpringDoc OpenAPI (Swagger)

Databases:
  • MySQL 8.0 (Primary data store)
  • MongoDB Atlas (Analytics store)
  • Redis (Future caching - optional)

External APIs:
  • OpenAQ API v3 (Air quality data)
  • OpenAI API (GPT-3.5-Turbo)

Infrastructure:
  • AWS EC2 (Compute)
  • AWS ALB (Load balancing)
  • NGINX (Reverse proxy)
  • Ubuntu 22.04 (OS)

Monitoring & Tools:
  • Spring Actuator (Health checks)
  • File-based logging
  • MongoDB Atlas metrics
  • Swagger UI (API documentation)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Non-Functional Requirements
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Performance:
  • API response time: < 200ms (cached)
  • Database connection pooling (HikariCP)
  • Async metrics collection (non-blocking)
  • Rate-limited external API calls

Scalability:
  • Modular architecture (easy to scale modules)
  • Stateless backend (horizontal scaling ready)
  • Cloud-native databases (managed scaling)
  • Load balancer ready

Reliability:
  • Health monitoring (Actuator)
  • Graceful degradation (AI fallback)
  • Connection pool management
  • Scheduled data sync
  • Error handling & logging

Security:
  • CORS configuration
  • API key management (environment variables)
  • Security groups (AWS)
  • HTTPS ready (SSL termination)
  • Input validation

Maintainability:
  • Separation of concerns
  • Clean architecture
  • Comprehensive logging
  • API documentation (Swagger)
  • Modular codebase

Observability:
  • Request metrics tracking
  • Performance monitoring
  • Health check endpoints
  • Analytics dashboard
  • File-based logging with rotation
```

## Architecture Highlights

### 1. **Modular Monolith Pattern**
   - Clear module boundaries (core, analytics, intelligence, shared)
   - Easy to extract into microservices if needed
   - Shared infrastructure reduces complexity

### 2. **Polyglot Persistence**
   - MySQL: ACID-compliant transactional data
   - MongoDB: Flexible schema for analytics
   - Right database for the right use case

### 3. **AI-Powered Intelligence**
   - OpenAI integration for smart recommendations
   - Fallback to rule-based logic
   - Cost-optimized model selection

### 4. **Real-time Data Pipeline**
   - Scheduled refresh from OpenAQ API
   - AQI calculation using EPA formula
   - Data freshness: 6-hour cycle

### 5. **Enterprise-Grade Observability**
   - Request interceptor for metrics
   - Health monitoring endpoints
   - Analytics dashboard
   - Structured logging

### 6. **Cloud-Ready Deployment**
   - AWS-optimized architecture
   - Scalable infrastructure
   - Managed services (MongoDB Atlas)
   - Load balancer ready

---

**Document Version:** 1.0  
**Last Updated:** January 14, 2026  
**Author:** Generated based on codebase analysis
