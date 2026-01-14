/**
 * Global Air Quality Tracker - Main Application Logic
 * 
 * This file handles:
 * - Fetching data from REST API endpoints
 * - Rendering global statistics and city data
 * - Search, filter, and sort functionality
 * - Charts rendering with Highcharts
 * - AI recommendations display
 */

// ============================================
// Global Variables
// ============================================
let citiesData = [];
let sortDirection = 'asc';
let sortColumn = '';

// ============================================
// Application Initialization
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('[AirQuality] Dashboard initialized');
    loadAllData();
});

/**
 * Load all data on page load
 */
function loadAllData() {
    fetchGlobalStats();
    fetchCitiesData();
}

/**
 * Refresh data on button click
 */
function refreshData() {
    const refreshBtn = document.getElementById('refresh-btn');
    refreshBtn.disabled = true;
    refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i> Refreshing...';
    
    // Call refresh endpoint first, then reload data
    fetch(`${CONFIG.API_BASE_URL}/refresh`, { method: 'POST' })
        .then(() => loadAllData())
        .catch(err => console.error('Refresh failed:', err))
        .finally(() => {
            setTimeout(() => {
                refreshBtn.disabled = false;
                refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refresh';
            }, 2000);
        });
}

// ============================================
// Global Statistics Functions
// ============================================

async function fetchGlobalStats() {
    const loadingDiv = document.getElementById('global-loading');
    const cardsDiv = document.getElementById('global-stats-cards');
    const chartsSection = document.getElementById('charts-section');
    const aiSection = document.getElementById('ai-section');
    
    loadingDiv.style.display = 'flex';
    cardsDiv.style.display = 'none';
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/global`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        renderGlobalStats(data);
        
        loadingDiv.style.display = 'none';
        cardsDiv.style.display = 'grid';
        chartsSection.style.display = 'block';
        aiSection.style.display = 'block';
        
    } catch (error) {
        console.error('[AirQuality] Error fetching global stats:', error);
        loadingDiv.style.display = 'none';
        showError('Failed to load global statistics. Please check if the API server is running.');
    }
}

function renderGlobalStats(data) {
    document.getElementById('total-cities').textContent = formatNumber(data.totalCities);
    document.getElementById('total-countries').textContent = formatNumber(data.totalCountries);
    document.getElementById('avg-aqi').textContent = Math.round(data.averageGlobalAqi);
    document.getElementById('cities-good').textContent = formatNumber(data.citiesWithGoodAir);
    document.getElementById('cities-unhealthy').textContent = formatNumber(data.citiesWithUnhealthyAir);
    
    // Cleanest city
    document.getElementById('cleanest-city').textContent = data.cleanestCity || '--';
    document.getElementById('cleanest-aqi').textContent = data.cleanestAqi || '--';
    
    // Most polluted city
    document.getElementById('polluted-city').textContent = data.mostPollutedCity || '--';
    document.getElementById('polluted-aqi').textContent = data.mostPollutedAqi || '--';
    
    // Last updated
    if (data.lastUpdated) {
        document.getElementById('last-updated').textContent = `Last Updated: ${data.lastUpdated}`;
    }
    
    // Create charts
    createAqiDistributionChart(data);
}

// ============================================
// Cities Data Functions
// ============================================

async function fetchCitiesData() {
    const loadingDiv = document.getElementById('cities-loading');
    const tableContainer = document.getElementById('cities-table-container');
    
    loadingDiv.style.display = 'flex';
    tableContainer.style.display = 'none';
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/cities`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        citiesData = data;
        renderCitiesTable(citiesData);
        createTopPollutedChart(citiesData);
        
        loadingDiv.style.display = 'none';
        tableContainer.style.display = 'block';
        
    } catch (error) {
        console.error('[AirQuality] Error fetching cities data:', error);
        loadingDiv.style.display = 'none';
        showError('Failed to load city data. Please check if the API server is running.');
    }
}

function renderCitiesTable(cities) {
    const tbody = document.getElementById('cities-table-body');
    const noResults = document.getElementById('no-results');
    const cityCount = document.getElementById('city-count');
    
    tbody.innerHTML = '';
    
    if (cities.length === 0) {
        noResults.style.display = 'block';
        cityCount.textContent = '0 cities';
        return;
    }
    
    noResults.style.display = 'none';
    cityCount.textContent = `${cities.length} cities`;
    
    cities.forEach((city, index) => {
        const row = document.createElement('tr');
        const aqiClass = getAqiClass(city.aqi);
        const aqiCategory = getAqiCategory(city.aqi);
        
        row.innerHTML = `
            <td class="text-muted">${index + 1}</td>
            <td>
                <span class="city-name">${escapeHtml(city.city)}</span>
            </td>
            <td class="country-name">${escapeHtml(city.country)}</td>
            <td class="numeric ${aqiClass}">${city.aqi || '--'}</td>
            <td class="numeric">${city.pm25 ? city.pm25.toFixed(1) : '--'}</td>
            <td class="numeric">${city.pm10 ? city.pm10.toFixed(1) : '--'}</td>
            <td><span class="aqi-badge ${aqiClass}">${aqiCategory}</span></td>
        `;
        
        tbody.appendChild(row);
    });
}

// ============================================
// Search and Filter Functions
// ============================================

function filterCities() {
    const searchInput = document.getElementById('search-input').value.toLowerCase();
    
    const filteredCities = citiesData.filter(city => 
        city.city.toLowerCase().includes(searchInput) ||
        city.country.toLowerCase().includes(searchInput)
    );
    
    renderCitiesTable(filteredCities);
}

// ============================================
// Sorting Functions
// ============================================

function sortTable(column) {
    if (sortColumn === column) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        sortColumn = column;
        sortDirection = 'asc';
    }
    
    citiesData.sort((a, b) => {
        let aValue = a[column];
        let bValue = b[column];
        
        // Handle null values
        if (aValue === null || aValue === undefined) aValue = '';
        if (bValue === null || bValue === undefined) bValue = '';
        
        // String comparison for city and country
        if (column === 'city' || column === 'country') {
            aValue = aValue.toLowerCase();
            bValue = bValue.toLowerCase();
            return sortDirection === 'asc' 
                ? aValue.localeCompare(bValue)
                : bValue.localeCompare(aValue);
        }
        
        // Numeric comparison
        return sortDirection === 'asc' 
            ? (aValue || 0) - (bValue || 0)
            : (bValue || 0) - (aValue || 0);
    });
    
    renderCitiesTable(citiesData);
}

// ============================================
// Charts Functions
// ============================================

function createAqiDistributionChart(stats) {
    Highcharts.chart('aqi-distribution-chart', {
        chart: {
            type: 'pie',
            backgroundColor: 'transparent'
        },
        title: { text: '' },
        credits: { enabled: false },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.y}'
                }
            }
        },
        series: [{
            name: 'Cities',
            colorByPoint: true,
            data: [
                { name: 'Good (0-50)', y: stats.citiesWithGoodAir, color: '#22c55e' },
                { name: 'Moderate (51-100)', y: stats.citiesWithModerateAir || 0, color: '#eab308' },
                { name: 'Unhealthy (101+)', y: stats.citiesWithUnhealthyAir, color: '#ef4444' }
            ]
        }]
    });
}

function createTopPollutedChart(cities) {
    // Sort by AQI descending and take top 10
    const topPolluted = [...cities]
        .filter(c => c.aqi != null)
        .sort((a, b) => b.aqi - a.aqi)
        .slice(0, 10);
    
    Highcharts.chart('top-polluted-chart', {
        chart: {
            type: 'bar',
            backgroundColor: 'transparent'
        },
        title: { text: '' },
        credits: { enabled: false },
        xAxis: {
            categories: topPolluted.map(c => c.city),
            labels: { style: { fontSize: '11px' } }
        },
        yAxis: {
            title: { text: 'AQI' }
        },
        legend: { enabled: false },
        plotOptions: {
            bar: {
                colorByPoint: true,
                colors: topPolluted.map(c => getAqiColor(c.aqi))
            }
        },
        series: [{
            name: 'AQI',
            data: topPolluted.map(c => c.aqi)
        }]
    });
}

// ============================================
// AI Recommendations Functions
// ============================================

async function getAiRecommendations() {
    const cityInput = document.getElementById('ai-city-input').value.trim();
    
    if (!cityInput) {
        showError('Please enter a city name to get AI recommendations.');
        return;
    }
    
    const container = document.getElementById('ai-recommendations');
    container.innerHTML = '<div class="loading-container"><div class="spinner"></div><p class="loading-text">Generating AI recommendations...</p></div>';
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/ai/recommendations/${encodeURIComponent(cityInput)}`);
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error(`City "${cityInput}" not found`);
            }
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        renderAiRecommendations(data);
        
    } catch (error) {
        console.error('[AirQuality] Error fetching AI recommendations:', error);
        container.innerHTML = `
            <div class="error-alert show">
                <i class="bi bi-exclamation-triangle"></i>
                ${error.message}
            </div>
        `;
    }
}

function renderAiRecommendations(data) {
    const container = document.getElementById('ai-recommendations');
    
    let html = `
        <div class="recommendation-card" style="grid-column: 1 / -1; border-left-color: var(--color-sky-light);">
            <h4>${data.city}, ${data.country}</h4>
            <p style="font-size: 1.25rem; margin: 0.5rem 0;">
                AQI: <strong class="${getAqiClass(data.aqi)}">${data.aqi}</strong> 
                <span class="aqi-badge ${getAqiClass(data.aqi)}">${data.aqiCategory}</span>
            </p>
            <p style="color: var(--neutral-600);">${data.overallAssessment}</p>
        </div>
    `;
    
    if (data.recommendations && data.recommendations.length > 0) {
        data.recommendations.forEach(rec => {
            html += `
                <div class="recommendation-card ${rec.severity}">
                    <div class="recommendation-icon">${rec.icon}</div>
                    <h5 class="recommendation-title">${rec.title}</h5>
                    <p class="recommendation-description">${rec.description}</p>
                </div>
            `;
        });
    }
    
    container.innerHTML = html;
}

// ============================================
// Error Handling Functions
// ============================================

function showError(message) {
    const errorAlert = document.getElementById('error-alert');
    const errorMessage = document.getElementById('error-message');
    
    errorMessage.textContent = message;
    errorAlert.classList.add('show');
    
    setTimeout(() => closeErrorAlert(), 10000);
}

function closeErrorAlert() {
    const errorAlert = document.getElementById('error-alert');
    errorAlert.classList.remove('show');
}

// ============================================
// Utility Functions
// ============================================

function formatNumber(num) {
    if (num === null || num === undefined) return '--';
    return num.toLocaleString('en-US');
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getAqiClass(aqi) {
    if (aqi === null || aqi === undefined) return '';
    if (aqi <= 50) return 'good';
    if (aqi <= 100) return 'moderate';
    if (aqi <= 150) return 'sensitive';
    if (aqi <= 200) return 'unhealthy';
    if (aqi <= 300) return 'very-unhealthy';
    return 'hazardous';
}

function getAqiCategory(aqi) {
    if (aqi === null || aqi === undefined) return 'Unknown';
    if (aqi <= 50) return 'Good';
    if (aqi <= 100) return 'Moderate';
    if (aqi <= 150) return 'Sensitive';
    if (aqi <= 200) return 'Unhealthy';
    if (aqi <= 300) return 'Very Unhealthy';
    return 'Hazardous';
}

function getAqiColor(aqi) {
    if (aqi === null || aqi === undefined) return '#94a3b8';
    if (aqi <= 50) return '#22c55e';
    if (aqi <= 100) return '#eab308';
    if (aqi <= 150) return '#f97316';
    if (aqi <= 200) return '#ef4444';
    if (aqi <= 300) return '#a855f7';
    return '#7f1d1d';
}

// Add spinning animation for refresh button
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        to { transform: rotate(360deg); }
    }
    .spin {
        animation: spin 1s linear infinite;
    }
`;
document.head.appendChild(style);
