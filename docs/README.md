# Documentation Index

Welcome to the Global Air Quality Tracker documentation.

## 📖 Available Guides

### 🚀 [Quick Start Guide](quick-start-guide.md)
**Start here!** Complete setup instructions to get the application running in 10-15 minutes.

**Covers:**
- Prerequisites and installation
- Database setup
- Configuration
- Building and running the application
- Verification steps
- Troubleshooting

**Estimated time:** 10-15 minutes

---

### 💰 [OpenAI Cost Management Guide](openai-cost-management.md)
**Essential reading for AI features!** Comprehensive guide to understanding, monitoring, and controlling OpenAI API costs.

**Covers:**
- Cost breakdown and examples
- Setting up spending limits (step-by-step with screenshots description)
- Built-in cost protection features
- Monitoring usage
- Alert workflow
- Optimization tips
- Security best practices

**Key takeaway:** The app costs ~$0.15/month with normal use and has built-in fallback for zero-cost mode.

---

### 🌍 [Character Encoding Fix](character-encoding-fix.md)
**Technical reference for UTF-8 handling.** Explains how the application handles international city names with special characters.

**Covers:**
- UTF-8 configuration at all layers
- Common encoding issues and solutions
- Testing international characters
- Troubleshooting garbled text

**When to read:** If you see `�` or wrong characters in city names, or if you're adding multilingual support.

---

## 📁 Additional Documentation

### Main Project Documentation
- **[Main README](../README.md)** - Project overview, features, and architecture
- **[Backend API README](../air-quality-tracker-api/README.md)** - Spring Boot API details
- **[Frontend README](../air-quality-tracker-ui/README.md)** - UI documentation

---

## 🎯 Documentation by Use Case

### I want to...

**Get started quickly**
→ Read: [Quick Start Guide](quick-start-guide.md)

**Understand OpenAI costs**
→ Read: [OpenAI Cost Management Guide](openai-cost-management.md) - Section: "Understanding the Costs"

**Set up spending limits**
→ Read: [OpenAI Cost Management Guide](openai-cost-management.md) - Section: "Setting Up OpenAI Spending Limits"

**Monitor my usage**
→ Read: [OpenAI Cost Management Guide](openai-cost-management.md) - Section: "Monitoring Usage"

**Reduce costs**
→ Read: [OpenAI Cost Management Guide](openai-cost-management.md) - Section: "Cost Optimization Tips"

**Disable AI temporarily**
→ Read: [OpenAI Cost Management Guide](openai-cost-management.md) - Section: "Adjusting AI Usage"

**Troubleshoot issues**
→ Read: [Quick Start Guide](quick-start-guide.md) - Section: "Troubleshooting"

**Fix garbled/wrong characters in city names**
→ Read: [Character Encoding Fix](character-encoding-fix.md)

**Deploy to AWS**
→ Coming soon: `aws-deployment-guide.md`

**Understand the code**
→ Read: [Backend API README](../air-quality-tracker-api/README.md) - Section: "Architecture"

---

## 🔗 Quick Links

### Application URLs (when running locally)
- **Main Dashboard**: http://localhost:5500/index.html
- **Analytics Dashboard**: http://localhost:5500/analytics.html
- **API Base**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health

### External Resources
- **OpenAQ API**: https://explore.openaq.org/
- **OpenAQ Documentation**: https://docs.openaq.org/
- **OpenAI Platform**: https://platform.openai.com/
- **OpenAI Pricing**: https://openai.com/pricing
- **MongoDB Atlas**: https://www.mongodb.com/cloud/atlas
- **Spring Boot Docs**: https://spring.io/projects/spring-boot

---

## 📊 Cost Summary

| Component | Cost | Notes |
|-----------|------|-------|
| OpenAQ API | **Free** | Up to 10,000 requests/day |
| OpenAI API | **$0.15/month** | Based on 300 requests/month |
| MongoDB Atlas | **Free** | M0 tier (512MB) |
| MySQL | **Free** | Self-hosted |
| AWS EC2 (optional) | **$0-5/month** | t2.micro free tier eligible |

**Total Monthly Cost (development):** ~$0.15

---

## 🆘 Need Help?

### Common Issues

**Backend won't start**
→ [Quick Start Guide](quick-start-guide.md) - "Troubleshooting" → "Backend Issues"

**Frontend shows errors**
→ [Quick Start Guide](quick-start-guide.md) - "Troubleshooting" → "Frontend Issues"

**AI not working**
→ [OpenAI Cost Management Guide](openai-cost-management.md) - "Troubleshooting"

**High costs**
→ [OpenAI Cost Management Guide](openai-cost-management.md) - "Cost Alert Workflow"

**Database connection failed**
→ [Quick Start Guide](quick-start-guide.md) - "Step 1: Database Setup"

---

## 📋 Checklists

### Pre-Launch Checklist
Before deploying to production:

- [ ] Read Quick Start Guide completely
- [ ] Set up OpenAI spending limits ($2/month recommended)
- [ ] Configure email alerts
- [ ] Test fallback mode (AI disabled)
- [ ] Review application logs
- [ ] Test all API endpoints
- [ ] Verify frontend loads correctly
- [ ] Check MongoDB connection (if using analytics)
- [ ] Review security best practices
- [ ] Document your configuration

### Monthly Maintenance Checklist
Do this on the 1st of each month:

- [ ] Check OpenAI usage dashboard
- [ ] Review application logs for errors
- [ ] Verify data is being refreshed (check timestamps)
- [ ] Check database size (MySQL)
- [ ] Review MongoDB analytics (if enabled)
- [ ] Update dependencies if needed
- [ ] Backup database (recommended)

---

## 🔐 Security Notes

**NEVER commit these files to Git:**
- `application-local.properties` (already in .gitignore)
- Any file containing API keys
- Database passwords

**Best practices:**
- Rotate API keys every 90 days
- Use environment variables in production
- Set up OpenAI spending limits
- Monitor usage regularly
- Delete unused API keys

See: [OpenAI Cost Management Guide](openai-cost-management.md) - Section: "Security Note"

---

## 📝 Document Versions

| Document | Version | Last Updated |
|----------|---------|--------------|
| Quick Start Guide | 1.0 | January 2026 |
| OpenAI Cost Management | 1.0 | January 2026 |
| Character Encoding Fix | 1.0 | January 2026 |
| Main README | 1.0 | January 2026 |

---

## 🎓 Learning Path

### For Beginners
1. Read Main README (overview)
2. Follow Quick Start Guide (setup)
3. Explore Swagger UI (API endpoints)
4. Read Frontend README (UI structure)

### For Developers
1. Read Backend API README (architecture)
2. Review code structure in `src/main/java/`
3. Study OpenAQ API integration
4. Understand MVC pattern implementation

### For Production Deployment
1. Complete Quick Start Guide
2. Read OpenAI Cost Management Guide
3. Set up MongoDB Atlas
4. Follow AWS Deployment Guide (coming soon)
5. Configure monitoring and alerts

---

*Documentation maintained by: Sawan Chakraborty*
*Project: Global Air Quality Tracker*
*Last Updated: January 2026*
