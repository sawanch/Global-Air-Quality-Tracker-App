# Air Quality Tracker - Frontend

A modern, responsive web dashboard for monitoring global air quality data.

## 🎨 Features

- **Interactive Dashboard** with real-time statistics
- **City Data Table** with search and sorting
- **Highcharts Visualizations** for data analysis
- **AI Recommendations** section
- **Analytics Dashboard** for API usage metrics
- **AQI Color Coding** following EPA standards
- **Responsive Design** for mobile and desktop

## 📁 Files

```
air-quality-tracker-ui/
├── index.html          # Main dashboard
├── analytics.html      # API analytics dashboard
├── css/
│   └── styles.css      # Custom styling
└── js/
    ├── config.js       # API configuration
    ├── app.js          # Main application logic
    └── analytics.js    # Analytics page logic
```

## 🚀 Quick Start

### Option 1: Using Live Server (VS Code)
1. Install "Live Server" extension in VS Code
2. Right-click `index.html` → "Open with Live Server"

### Option 2: Python HTTP Server
```bash
cd air-quality-tracker-ui
python -m http.server 5500
```
Open: http://localhost:5500

### Option 3: Node.js HTTP Server
```bash
npx http-server -p 5500
```

## ⚙️ Configuration

Edit `js/config.js` to switch between environments:

```javascript
const CONFIG = {
    // Local Development
    API_BASE_URL: 'http://localhost:8080/api',
    SWAGGER_UI_URL: 'http://localhost:8080/swagger-ui/index.html'
    
    // AWS Production (uncomment when deploying)
    // API_BASE_URL: 'http://YOUR_EC2_IP:8080/api',
    // SWAGGER_UI_URL: 'http://YOUR_EC2_IP:8080/swagger-ui/index.html'
};
```

## 🎨 Design Features

- **Custom Typography**: Outfit font for modern, clean text
- **Sky/Air Theme**: Light blue gradient backgrounds
- **Atmospheric Effects**: Subtle gradient overlays
- **Card-based Layout**: Information organized in cards
- **AQI Color Scale**: Green (Good) → Maroon (Hazardous)
- **Smooth Animations**: Hover effects and transitions

## 📱 Responsive Breakpoints

- **Desktop**: 1400px max-width container
- **Tablet**: 768px breakpoint
- **Mobile**: 576px breakpoint

## 🔗 Dependencies (CDN)

- Bootstrap 5.3.2
- Bootstrap Icons 1.11.1
- Highcharts (latest)

No npm install required - all dependencies loaded via CDN.
