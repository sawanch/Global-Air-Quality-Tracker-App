# Quick Start Guide - Global Air Quality Tracker

This guide will get you up and running in 10 minutes.

## ✅ Prerequisites Checklist

Before starting, ensure you have:

- [ ] Java 11 or higher installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] MySQL 8.0 running locally
- [ ] OpenAQ API key (from https://explore.openaq.org/register)
- [ ] OpenAI API key (from https://platform.openai.com/api-keys)
- [ ] MongoDB Atlas account (optional - for analytics only)

---

## 🚀 Step-by-Step Setup

### Step 1: Database Setup (2 minutes)

```bash
# 1. Connect to MySQL
mysql -u root -p
# Enter your password when prompted

# 2. Run database creation script
source /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api/sql/01_create_database.sql

# 3. Run table creation script
source /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api/sql/02_create_air_quality_data_table.sql

# 4. Verify database created
SHOW DATABASES;
USE air_quality_db;
SHOW TABLES;

# 5. Exit MySQL
exit;
```

**Expected Output:**
```
Database air_quality_db created successfully!
Table air_quality_data created successfully!
```

---

### Step 2: Configure Application (1 minute)

Your configuration is already set up in `application-local.properties`:

```properties
# Configure these in application-local.properties:
spring.datasource.password=YOUR_MYSQL_PASSWORD
openaq.api.key=YOUR_OPENAQ_API_KEY
openai.api.key=YOUR_OPENAI_API_KEY

# ⚠️ OPTIONAL (for analytics dashboard):
# spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/air_quality_analytics
```

**Note:** MongoDB is optional. The app works perfectly without it (analytics dashboard just won't load data).

---

### Step 3: Build the Application (2 minutes)

```bash
# Navigate to project directory
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api

# Clean and build
mvn clean package

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] ------------------------------------------------------------------------
```

**Troubleshooting:**
- If build fails, check Java version: `java -version` (must be 11+)
- If Maven not found: `brew install maven` (macOS)

---

### Step 4: Run the Backend (1 minute)

```bash
# Start the Spring Boot application
java -jar target/air-quality-tracker-api.jar
```

**What to expect:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.7.14)

...
=== Air Quality Data Initializer Started ===
Database is empty. Loading initial data from OpenAQ API...
Fetching air quality data from OpenAQ API (limit: 500)
...
Initial data load completed. XXX cities loaded.
=== Air Quality Data Initializer Completed ===
...
Tomcat started on port(s): 8080 (http)
Started AirQualityTrackerApiApplication in X.XXX seconds
```

**Key Indicators of Success:**
- ✅ "Started AirQualityTrackerApiApplication"
- ✅ "Tomcat started on port(s): 8080"
- ✅ "Initial data load completed"

**Common Issues:**

1. **Port 8080 already in use:**
```bash
# Kill existing process
lsof -ti:8080 | xargs kill -9
# Or change port in application.properties: server.port=8081
```

2. **MySQL connection error:**
```
Error: Access denied for user 'root'@'localhost'
Solution: Check password in application-local.properties
```

3. **OpenAQ API error (404/403):**
```
Warning: Failed to fetch data from OpenAQ API
Solution: Verify API key is correct and active
Note: App will continue running, just without initial data
```

---

### Step 5: Verify Backend is Running (30 seconds)

Open browser and test these endpoints:

```bash
# Health check
http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# API Documentation
http://localhost:8080/swagger-ui/index.html
# Expected: Swagger UI page

# Global statistics
http://localhost:8080/api/global
# Expected: JSON with global air quality stats

# All cities
http://localhost:8080/api/cities
# Expected: JSON array with city data
```

---

### Step 6: Run the Frontend (1 minute)

**Option A: Using Live Server (VS Code)**
```bash
1. Open VS Code
2. Install "Live Server" extension (if not installed)
3. Right-click on: air-quality-tracker-ui/index.html
4. Select "Open with Live Server"
5. Browser opens automatically at http://localhost:5500
```

**Option B: Using Python**
```bash
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-ui

# Python 3
python3 -m http.server 5500

# Browser will open at http://localhost:5500
```

**Option C: Using Node.js**
```bash
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-ui

# One-time install (if not already installed)
npm install -g http-server

# Start server
http-server -p 5500

# Open browser at http://localhost:5500
```

---

### Step 7: Test the Application (3 minutes)

**Main Dashboard (index.html):**
1. ✅ Global statistics cards load with data
2. ✅ City data table displays 500+ cities
3. ✅ Search box filters cities
4. ✅ Sorting works on table columns
5. ✅ Charts display (AQI distribution, top polluted)

**AI Recommendations:**
1. Enter a city name (e.g., "New York")
2. Click "Get Recommendations"
3. Wait 2-3 seconds
4. ✅ See AI-generated recommendation cards

**Analytics Dashboard (analytics.html):**
1. Click "Analytics" in navigation
2. If MongoDB configured: ✅ See API usage metrics
3. If MongoDB not configured: ⚠️ Shows error (expected, not critical)

---

## 🎯 Verification Checklist

After setup, verify:

- [ ] Backend running on http://localhost:8080
- [ ] `/actuator/health` returns `{"status":"UP"}`
- [ ] `/api/global` returns JSON with statistics
- [ ] `/api/cities` returns array of cities (500+)
- [ ] Frontend running on http://localhost:5500
- [ ] Dashboard loads with data
- [ ] Search and sort work on table
- [ ] Charts display correctly
- [ ] AI recommendations work (when clicking button)
- [ ] Swagger UI accessible at `/swagger-ui/index.html`

---

## 🔧 Optional: MongoDB Setup for Analytics (5 minutes)

If you want the analytics dashboard to work:

### 1. Create MongoDB Atlas Account
```
1. Go to: https://www.mongodb.com/cloud/atlas/register
2. Sign up (free)
3. Choose "Shared" (free tier - M0)
4. Select cloud provider and region (any)
5. Cluster name: "AirQualityCluster" (or any name)
6. Click "Create Cluster" (takes 3-5 minutes)
```

### 2. Create Database User
```
1. Click "Database Access" (left sidebar)
2. Click "Add New Database User"
3. Username: airqualityuser
4. Password: (generate strong password or create your own)
5. Database User Privileges: "Read and write to any database"
6. Click "Add User"
```

### 3. Whitelist Your IP
```
1. Click "Network Access" (left sidebar)
2. Click "Add IP Address"
3. Click "Allow Access from Anywhere" (for development)
   Or add your specific IP
4. Click "Confirm"
```

### 4. Get Connection String
```
1. Click "Database" (left sidebar)
2. Click "Connect" on your cluster
3. Click "Connect your application"
4. Copy the connection string:
   mongodb+srv://airqualityuser:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
5. Replace <password> with your actual password
```

### 5. Update Configuration
```bash
# Edit application-local.properties
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api/src/main/resources

# Add MongoDB URI:
spring.data.mongodb.uri=mongodb+srv://airqualityuser:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/air_quality_analytics?retryWrites=true&w=majority
```

### 6. Restart Application
```bash
# Stop current application (Ctrl+C)
# Restart
java -jar target/air-quality-tracker-api.jar

# Verify MongoDB connection in logs:
# "Successfully connected to MongoDB"
```

### 7. Test Analytics Dashboard
```
1. Open http://localhost:5500/analytics.html
2. Should now see API usage metrics
3. Charts populate with request data
4. Timeline shows recent requests
```

---

## 📊 What You Should See

### Main Dashboard
![Expected View]
- Global statistics: Total cities, average AQI, cleanest/most polluted
- Interactive charts
- Searchable/sortable city table with 500+ cities
- AI recommendations section (click to use)

### Analytics Dashboard
![Expected View]
- Total requests count
- Active endpoints
- Average response time
- Success rate
- Charts: Request counts, response times, success/error rates
- Timeline of last 100 API requests

---

## 🐛 Troubleshooting

### Backend Issues

**Problem: Application won't start**
```bash
# Check Java version
java -version  # Must be 11+

# Check if port is in use
lsof -ti:8080

# Check MySQL is running
mysql.server status  # or: brew services list
```

**Problem: Database connection error**
```bash
# Verify MySQL credentials
mysql -u root -p

# Check database exists
mysql -u root -p -e "SHOW DATABASES;" | grep air_quality_db

# Recreate database if needed
mysql -u root -p < sql/01_create_database.sql
mysql -u root -p < sql/02_create_air_quality_data_table.sql
```

**Problem: OpenAQ API returns no data**
```bash
# Check API key is valid
curl -H "X-API-Key: YOUR_KEY" https://api.openaq.org/v3/locations?limit=1

# App will still run, just without initial data
# Use POST /api/refresh to retry loading data
```

### Frontend Issues

**Problem: "Failed to load data" error**
```javascript
// Check API_BASE_URL in js/config.js
API_BASE_URL: 'http://localhost:8080/api',  // Must match backend port

// Check CORS in browser console (F12)
// If CORS error, verify backend is running
```

**Problem: Charts not displaying**
```html
<!-- Verify Highcharts loaded -->
<!-- Open browser console (F12) and check for errors -->
<!-- Check network tab to see if CDN resources loaded -->
```

**Problem: AI recommendations not working**
```bash
# Check OpenAI API key in application-local.properties
# Check browser console for errors
# Verify backend logs show "Calling OpenAI API"
# If API key invalid, fallback recommendations will display
```

---

## 📝 Daily Usage

### Starting the Application
```bash
# Terminal 1: Start Backend
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api
java -jar target/air-quality-tracker-api.jar

# Terminal 2: Start Frontend (VS Code Live Server)
# Or use: cd air-quality-tracker-ui && python3 -m http.server 5500
```

### Stopping the Application
```bash
# Backend: Press Ctrl+C in terminal
# Frontend: Press Ctrl+C (if using Python server) or stop Live Server
```

### Checking Logs
```bash
# View application logs
tail -f logs/air-quality-tracker-api.log

# Search for errors
grep ERROR logs/air-quality-tracker-api.log

# View OpenAQ API calls
grep "OpenAQ" logs/air-quality-tracker-api.log
```

---

## 🎓 Next Steps

After successful setup:

1. **Explore API endpoints** via Swagger UI: http://localhost:8080/swagger-ui/index.html
2. **Review cost management**: See `docs/openai-cost-management.md`
3. **Set OpenAI spending limits**: $2/month recommended for testing
4. **Monitor usage**: Check logs and OpenAI dashboard regularly
5. **Test AI features**: Try different cities for recommendations
6. **Customize**: Modify AQI thresholds, colors, or add new features

---

## 📚 Additional Documentation

- **Main README**: `README.md` - Project overview
- **API README**: `air-quality-tracker-api/README.md` - Backend details
- **UI README**: `air-quality-tracker-ui/README.md` - Frontend details
- **Cost Management**: `docs/openai-cost-management.md` - OpenAI cost control

---

## 🆘 Need Help?

**Common Commands:**
```bash
# Rebuild after code changes
mvn clean package

# Run with different profile
java -jar target/air-quality-tracker-api.jar --spring.profiles.active=prod

# Check if backend is running
curl http://localhost:8080/actuator/health

# Test specific endpoint
curl http://localhost:8080/api/global

# Force data refresh
curl -X POST http://localhost:8080/api/refresh
```

**Useful Logs Locations:**
```
Backend logs: air-quality-tracker-api/logs/air-quality-tracker-api.log
Browser console: Press F12 → Console tab
Network requests: F12 → Network tab
```

---

*Estimated Total Setup Time: 10-15 minutes*
*Last Updated: January 2026*
