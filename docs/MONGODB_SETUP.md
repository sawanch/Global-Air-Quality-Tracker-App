# MongoDB Atlas Setup Guide

## 🎯 Quick Setup

Your MongoDB connection string has been configured! Just replace the password placeholder.

---

## ✅ Step 1: Get Your Password

1. Go to [MongoDB Atlas](https://cloud.mongodb.com/)
2. Navigate to: **Database Access** (left sidebar)
3. Find user: `sc_db_user`
4. Click **Edit** → **Edit Password**
5. Either:
   - View existing password (if saved)
   - Generate new password and **SAVE IT**

---

## ✅ Step 2: Update Configuration

Edit: `air-quality-tracker-api/src/main/resources/application-local.properties`

**Find this line:**
```properties
spring.data.mongodb.uri=mongodb+srv://sc_db_user:<db_password>@covid-analytics-cluster.eo1nj5c.mongodb.net/air_quality_analytics?retryWrites=true&w=majority&appName=covid-analytics-cluster-sc
```

**Replace `<db_password>` with your actual password:**
```properties
spring.data.mongodb.uri=mongodb+srv://sc_db_user:YOUR_ACTUAL_PASSWORD@covid-analytics-cluster.eo1nj5c.mongodb.net/air_quality_analytics?retryWrites=true&w=majority&appName=covid-analytics-cluster-sc
```

**Example:**
```properties
# If your password is: MySecurePass123
spring.data.mongodb.uri=mongodb+srv://sc_db_user:MySecurePass123@covid-analytics-cluster.eo1nj5c.mongodb.net/air_quality_analytics?retryWrites=true&w=majority&appName=covid-analytics-cluster-sc
```

---

## ✅ Step 3: Verify Connection

### **Option A: Using MongoDB Compass (GUI)**

1. Download [MongoDB Compass](https://www.mongodb.com/try/download/compass)
2. Paste your connection string (with password replaced)
3. Click **Connect**
4. You should see the `air_quality_analytics` database

### **Option B: Using Application**

1. Start your application:
   ```bash
   cd air-quality-tracker-api
   ./run.sh
   ```

2. Look for these log messages:
   ```
   ✅ GOOD: "Cluster created with settings"
   ✅ GOOD: "Opened connection"
   ❌ BAD:  "Exception authenticating" (wrong password)
   ```

3. Test the analytics endpoint:
   ```bash
   curl http://localhost:8080/api/analytics/summary
   ```

---

## 📊 What Gets Stored in MongoDB?

### **Collection: `api_request_metrics`**

Every API call is tracked:

```json
{
  "_id": "507f1f77bcf86cd799439011",
  "endpoint": "/api/global",
  "method": "GET",
  "statusCode": 200,
  "responseTime": 45,
  "timestamp": "2026-01-14T08:45:00Z",
  "userAgent": "Mozilla/5.0...",
  "ipAddress": "192.168.1.1"
}
```

### **Database: `air_quality_analytics`**

- Auto-created on first API call
- No manual setup needed
- Stores all API usage metrics

---

## 🎨 Frontend Analytics Dashboard

Once MongoDB is configured, the analytics dashboard will work:

**URL:** http://localhost:5500/analytics.html

**Features:**
- 📊 Total API requests
- ⚡ Average response time
- 📈 Requests over time (chart)
- 🔝 Most popular endpoints
- 📉 Error rate tracking

---

## 🔧 Troubleshooting

### **Issue 1: Authentication Failed**

```
MongoSecurityException: Exception authenticating
```

**Solution:**
- ✅ Check password is correct (no typos)
- ✅ Password doesn't contain special characters like `@`, `:`, `/`
  - If it does, URL-encode them:
    - `@` → `%40`
    - `:` → `%3A`
    - `/` → `%2F`

**Example:**
```properties
# Password: MyPass@123
spring.data.mongodb.uri=mongodb+srv://sc_db_user:MyPass%40123@...
```

---

### **Issue 2: Network Timeout**

```
MongoTimeoutException: Timed out after 30000 ms
```

**Solution:**
1. Check MongoDB Atlas → **Network Access**
2. Add your IP address:
   - Click **Add IP Address**
   - Choose **Add Current IP Address**
   - Or use `0.0.0.0/0` (allow from anywhere - for development only)

---

### **Issue 3: Database Not Created**

**This is normal!** MongoDB creates the database automatically on first write.

To trigger creation:
1. Start the application
2. Make any API call: `curl http://localhost:8080/api/global`
3. Check MongoDB Atlas → Browse Collections
4. You should see `air_quality_analytics` database

---

## 🔐 Security Best Practices

### **✅ DO:**
- ✅ Use strong passwords (12+ characters)
- ✅ Rotate passwords every 90 days
- ✅ Keep `application-local.properties` in `.gitignore`
- ✅ Use IP whitelisting in production
- ✅ Enable MongoDB Atlas monitoring

### **❌ DON'T:**
- ❌ Commit passwords to Git
- ❌ Use `0.0.0.0/0` in production
- ❌ Share connection strings publicly
- ❌ Use weak passwords

---

## 📊 MongoDB Atlas Free Tier Limits

| Resource | Limit | Your Usage (estimated) |
|----------|-------|------------------------|
| **Storage** | 512 MB | ~10 MB/month |
| **RAM** | Shared | Sufficient |
| **Connections** | 500 | ~1-10 |
| **Cost** | **FREE** | $0 |

**You're well within limits!** 🎉

---

## 🎯 Quick Commands

### **Check Connection:**
```bash
# From terminal
mongo "mongodb+srv://covid-analytics-cluster.eo1nj5c.mongodb.net/air_quality_analytics" --username sc_db_user
```

### **View Data:**
```bash
# Using mongosh
use air_quality_analytics
db.api_request_metrics.find().limit(5).pretty()
```

### **Count Documents:**
```bash
db.api_request_metrics.countDocuments()
```

---

## ✅ Verification Checklist

- [ ] Got MongoDB password from Atlas
- [ ] Updated `application-local.properties` with password
- [ ] Removed `<db_password>` placeholder
- [ ] Added IP address to Network Access
- [ ] Started application successfully
- [ ] Saw MongoDB connection logs
- [ ] Made test API call
- [ ] Checked MongoDB Atlas → Browse Collections
- [ ] Saw `air_quality_analytics` database
- [ ] Opened analytics dashboard
- [ ] Saw metrics displayed

---

## 🆘 Still Having Issues?

### **Check Application Logs:**
```bash
tail -f air-quality-tracker-api/logs/air-quality-tracker-api.log
```

Look for:
- ✅ `Cluster created with settings`
- ✅ `Opened connection`
- ❌ `MongoSecurityException`
- ❌ `MongoTimeoutException`

### **Test Connection Directly:**
```bash
# Install mongosh if needed
brew install mongosh

# Test connection
mongosh "mongodb+srv://sc_db_user:YOUR_PASSWORD@covid-analytics-cluster.eo1nj5c.mongodb.net/air_quality_analytics"
```

---

## 🎉 Success!

Once configured, your analytics will:
- ✅ Track every API call automatically
- ✅ Store metrics in MongoDB Atlas
- ✅ Display beautiful charts in the dashboard
- ✅ Help you understand API usage patterns

**No further action needed - it just works!** 🚀

---

**Connection String Details:**
- **Cluster:** `covid-analytics-cluster.eo1nj5c.mongodb.net`
- **User:** `sc_db_user`
- **Database:** `air_quality_analytics`
- **App Name:** `covid-analytics-cluster-sc`
