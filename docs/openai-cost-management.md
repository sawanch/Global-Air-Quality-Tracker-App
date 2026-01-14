# OpenAI Cost Management & Protection Guide

This guide explains how to control and monitor OpenAI API costs for the Air Quality Tracker application.

## 📊 Understanding the Costs

### Current Configuration
- **Model Used**: `gpt-3.5-turbo` (cheapest option)
- **Cost**: $0.0015 per 1K input tokens, $0.002 per 1K output tokens
- **Typical Request**: ~200 input + ~300 output tokens = **$0.0005 per AI recommendation**

### Real-World Usage Examples

| Usage Pattern | Monthly Requests | Monthly Cost |
|---------------|------------------|--------------|
| Light Use (5 cities/week) | ~20 requests | **$0.01** |
| Moderate Use (2 cities/day) | ~60 requests | **$0.03** |
| Heavy Use (10 cities/day) | ~300 requests | **$0.15** |
| Very Heavy (100 cities/day) | ~3,000 requests | **$1.50** |

**Note:** AI recommendations are triggered ONLY when users manually click "Get Recommendations" button - not automatic!

---

## 🛡️ Built-in Cost Protection Features

### 1. On-Demand Only (Not Automatic)
```java
// AI is ONLY called when user explicitly requests it
// Located in: AiController.java
@GetMapping("/recommendations/{city}")
public ResponseEntity<?> getRecommendations(@PathVariable String city) {
    // User must click button to trigger this
    AirQualityRecommendation recommendation = aiService.getRecommendationsForCity(city);
    return ResponseEntity.ok(recommendation);
}
```

### 2. Token Limit Cap
```java
// Located in: AiServiceImpl.java (line ~159)
requestBody.put("max_tokens", 500);  // Hard limit prevents runaway costs
requestBody.put("temperature", 0.7);  // Controlled randomness
```

This ensures:
- Maximum 500 tokens per response
- Even if API goes wild, cost capped at $0.001 per request

### 3. Automatic Fallback (Zero-Cost Mode)
```java
// Located in: AiServiceImpl.java (line ~68)
if (openaiApiKey != null && !openaiApiKey.isEmpty()) {
    try {
        String aiResponse = callOpenAI(buildPrompt(airQualityData));
        parseAiResponse(aiResponse, recommendation);
        return recommendation;
    } catch (Exception e) {
        logger.warn("Failed to get AI recommendations, using fallback: {}", e.getMessage());
    }
}

// Fallback to rule-based recommendations (NO COST)
recommendation.setOverallAssessment(generateFallbackAssessment(airQualityData));
recommendation.setRecommendations(generateFallbackRecommendations(airQualityData.getAqi()));
```

**What this means:**
- If OpenAI API fails → Free rule-based recommendations
- If API key is missing → Free rule-based recommendations  
- If you hit spending limit → Free rule-based recommendations
- **App never crashes due to AI costs!**

### 4. Error Handling & Retry Prevention
```java
// No automatic retries - if API fails, uses fallback immediately
// No queuing - each request is independent
// Logs errors for monitoring without retrying
```

---

## 🔧 Setting Up OpenAI Spending Limits (Step-by-Step)

### Step 1: Log into OpenAI Dashboard
1. Go to: https://platform.openai.com/
2. Sign in with your account
3. Click on your profile (top-right corner)

### Step 2: Navigate to Billing Settings
1. Click **"Settings"** from the dropdown menu
2. Select **"Billing"** from the left sidebar
3. You'll see your current usage and balance

### Step 3: Set Up Usage Limits

#### Option A: Hard Limit (Recommended for Safety)
```
1. Click "Usage limits" or "Set limits"
2. Set "Hard limit" to a safe amount:
   - Conservative: $1.00/month (enough for ~2,000 requests)
   - Moderate: $5.00/month (enough for ~10,000 requests)
   - Liberal: $10.00/month (enough for ~20,000 requests)

3. Set "Soft limit" (warning threshold):
   - Set to 50% of hard limit
   - Example: Hard=$5, Soft=$2.50
```

**What happens when you hit the limit:**
- API calls will fail gracefully
- App automatically switches to free fallback mode
- You receive email notification

#### Option B: Budget Alerts (Monitoring Only)
```
1. Go to "Notifications" under Billing
2. Add your email address
3. Set budget alert thresholds:
   - $0.50 (50 cents)
   - $1.00 (1 dollar)
   - $5.00 (5 dollars)
```

**What happens:**
- You get email alerts at each threshold
- API continues working until hard limit (if set)
- Good for monitoring without blocking

### Step 4: Enable Auto-Recharge Protection
```
1. Under "Payment methods"
2. If using credit card:
   - Disable "Auto-recharge" (recommended)
   - Or set maximum auto-recharge amount ($5-10)
```

This prevents unexpected charges if you forget to check usage.

### Step 5: Set Up Monthly Budget (Recommended Setup)

**For Peace of Mind:**
```
Hard Limit:      $2.00/month
Soft Limit:      $1.00/month  
Email Alert 1:   $0.50
Email Alert 2:   $1.50
Auto-recharge:   DISABLED
```

**Why this works:**
- $2/month = ~4,000 AI recommendations
- You get warned at $0.50 (25% usage)
- App continues working even if limit hit (uses fallback)
- No surprise charges

---

## 📈 Monitoring Usage

### Check Usage in OpenAI Dashboard
```
1. Go to: https://platform.openai.com/usage
2. View usage by:
   - Date range
   - Model (filter to gpt-3.5-turbo)
   - Cost breakdown
```

### Check Usage in Your Application Logs
```bash
# View AI-related logs
cd /Users/sawan/Documents/24-Intuit/Global-Air-Quality-Tracker-App/air-quality-tracker-api
tail -f logs/air-quality-tracker-api.log | grep "AI"

# Look for lines like:
# "Generating AI recommendations for city: New York"
# "Failed to get AI recommendations, using fallback"
# "Calling OpenAI API"
```

### Monthly Cost Calculation
```python
# Calculate your monthly cost:
Total Requests = (Number of AI button clicks per day) × 30 days
Estimated Cost = Total Requests × $0.0005

Example:
- 10 clicks/day × 30 days = 300 requests
- 300 × $0.0005 = $0.15/month
```

---

## 🎛️ Adjusting AI Usage in Application

### Option 1: Change Model (Cost vs Quality)

Edit `application.properties`:

```properties
# CHEAPEST - Recommended for most users
openai.api.model=gpt-3.5-turbo

# PREMIUM - Better quality, 20x more expensive
# openai.api.model=gpt-4

# BALANCED - Good quality, moderate cost
# openai.api.model=gpt-4o-mini
```

### Option 2: Reduce Max Tokens (Lower Cost per Request)

Edit `AiServiceImpl.java` (line ~159):

```java
// Current setting (balanced)
requestBody.put("max_tokens", 500);

// More economical (shorter responses)
requestBody.put("max_tokens", 300);  // $0.0003 per request

// Very economical (brief responses)
requestBody.put("max_tokens", 150);  // $0.0002 per request
```

### Option 3: Disable AI Temporarily

**Method A: Remove API Key (Easiest)**
```properties
# In application-local.properties
openai.api.key=
```

**Method B: Environment Variable**
```bash
# When running the app
unset OPENAI_API_KEY
java -jar target/air-quality-tracker-api.jar
```

**Method C: Comment Out in Config**
```properties
# openai.api.key=sk-...
```

**App will automatically use free fallback recommendations!**

---

## 🚨 Cost Alert Workflow

### When You Receive a Spending Alert

**Alert at $0.50 (Soft Limit):**
```
✅ Action: Review usage in dashboard
✅ Check if usage is expected
✅ No action needed if within budget
✅ Consider reducing usage if unexpected
```

**Alert at $1.00 (Warning):**
```
⚠️ Action: Decide if you want to continue
⚠️ Review which cities are being queried
⚠️ Consider temporarily disabling AI
⚠️ Wait until next month if over budget
```

**Hard Limit Reached ($2.00):**
```
🛑 API calls will fail (app continues with fallback)
🛑 You'll see "Failed to get AI recommendations" in logs
🛑 Users will see rule-based recommendations
🛑 Wait until next month or increase limit
```

---

## 📋 Best Practices

### 1. Start Conservative
- Set hard limit to $1.00 for first month
- Monitor actual usage
- Increase if needed

### 2. Review Monthly
- Check usage dashboard on 1st of each month
- Adjust limits based on actual needs
- Delete old API keys if not using

### 3. Development vs Production
```properties
# Development (application-local.properties)
openai.api.key=sk-your-key-here  # Use AI for testing

# Production (application-prod.properties)  
openai.api.key=${OPENAI_API_KEY}  # Use environment variable
```

### 4. User Education
Consider adding to UI:
```html
<!-- Inform users about AI feature -->
<p class="text-muted small">
  💡 AI recommendations use OpenAI API. 
  Click "Get Recommendations" only when needed.
</p>
```

---

## 🔍 Troubleshooting

### Issue: "Failed to get AI recommendations"

**Possible Causes:**
1. **Spending limit reached** → Check OpenAI dashboard
2. **Invalid API key** → Verify key in application-local.properties
3. **Network issue** → Check internet connection
4. **OpenAI API down** → Check status.openai.com

**Solution:**
```bash
# App automatically uses fallback - no action needed!
# Users still get recommendations (rule-based)
```

### Issue: Higher costs than expected

**Check:**
1. **Usage dashboard** → See which days had high usage
2. **Application logs** → Count "Calling OpenAI API" entries
3. **User behavior** → Are users clicking AI button frequently?

**Solutions:**
- Reduce max_tokens to 300
- Add rate limiting on frontend (1 request per minute per user)
- Switch to rule-based mode during testing

---

## 💡 Cost Optimization Tips

### 1. Cache AI Responses (Optional Enhancement)
```java
// Cache recommendations for 1 hour per city
// Reduces duplicate API calls for same city
@Cacheable(value = "aiRecommendations", key = "#cityName", ttl = 3600)
public AirQualityRecommendation getRecommendationsForCity(String cityName) {
    // Implementation
}
```

### 2. Batch Multiple Cities (Future Enhancement)
```java
// Instead of 5 separate calls, combine into 1 call
// "Generate recommendations for: NYC, LA, Chicago, Boston, Miami"
```

### 3. Smart Fallback Logic
Current implementation already does this:
- Uses AI for detailed analysis
- Falls back to rules for reliability
- Zero downtime even if budget exceeded

---

## 📊 Cost Comparison: AI vs Fallback

| Feature | AI Mode | Fallback Mode |
|---------|---------|---------------|
| **Cost** | $0.0005/request | $0.00 |
| **Response Quality** | Personalized, contextual | Generic, rule-based |
| **Speed** | 2-3 seconds | Instant |
| **Reliability** | 99.9% (depends on API) | 100% (local) |
| **Data Needed** | Internet + API key | None |
| **Scalability** | Limited by budget | Unlimited |

**Recommendation:** Use AI for public-facing features, fallback for internal testing.

---

## 🎯 Summary Checklist

Before running in production:

- [ ] Set OpenAI hard limit: $2.00/month (or your budget)
- [ ] Set soft limit: $1.00/month (50% of hard limit)
- [ ] Enable email alerts at $0.50 and $1.50
- [ ] Disable auto-recharge
- [ ] Test fallback mode works (remove API key temporarily)
- [ ] Monitor usage for first week
- [ ] Document expected monthly cost
- [ ] Add user notification about AI feature (optional)

---

## 📞 Support Resources

- **OpenAI Pricing**: https://openai.com/pricing
- **Usage Dashboard**: https://platform.openai.com/usage
- **API Status**: https://status.openai.com/
- **Billing Help**: https://help.openai.com/en/collections/3742473-billing

---

## 🔒 Security Note

**NEVER commit API keys to Git!**
- ✅ `application-local.properties` is in `.gitignore`
- ✅ Use environment variables in production
- ✅ Rotate keys periodically
- ✅ Delete unused keys from OpenAI dashboard

---

*Last Updated: January 2026*
*For Application: Global Air Quality Tracker*
*OpenAI Model: gpt-3.5-turbo*
