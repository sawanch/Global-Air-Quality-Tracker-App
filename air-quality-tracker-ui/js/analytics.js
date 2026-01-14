/**
 * API Analytics Dashboard - Main Application Logic
 */

// ============================================
// Global Variables
// ============================================
let timelineData = [];
let timelineSortDirection = 'desc';
let timelineSortColumn = 'timestamp';

// ============================================
// Application Initialization
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('[Analytics] Dashboard initialized');
    loadAnalytics();
});

/**
 * Load analytics data
 */
async function loadAnalytics() {
    try {
        showLoading(true);
        
        const [summaryResponse, timelineResponse] = await Promise.all([
            fetch(`${CONFIG.API_BASE_URL}/analytics/summary`),
            fetch(`${CONFIG.API_BASE_URL}/analytics/timeline`)
        ]);

        if (!summaryResponse.ok || !timelineResponse.ok) {
            throw new Error('Failed to fetch analytics data');
        }

        const summary = await summaryResponse.json();
        const timeline = await timelineResponse.json();

        console.log('[Analytics] Data loaded:', { 
            totalRequests: summary.totalRequests, 
            timelineCount: timeline.length 
        });

        displaySummary(summary);
        displayTimeline(timeline);
        updateLastUpdated();
        showLoading(false);
        
    } catch (error) {
        console.error('[Analytics] Error loading data:', error);
        showError('Failed to load analytics data. Make sure the API and MongoDB are running.');
        showLoading(false);
    }
}

/**
 * Refresh analytics data
 */
function refreshAnalytics() {
    console.log('[Analytics] Refreshing data...');
    const refreshBtn = document.querySelector('button[onclick="refreshAnalytics()"]');
    if (refreshBtn) {
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise spin"></i> Refreshing...';
    }
    
    loadAnalytics();
    
    setTimeout(() => {
        if (refreshBtn) {
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refresh';
        }
    }, 2000);
}

// ============================================
// Display Functions
// ============================================

function displaySummary(summary) {
    // Total requests
    document.getElementById('total-requests').textContent = 
        formatNumber(summary.totalRequests || 0);
    
    // Active endpoints
    const endpoints = Object.keys(summary.endpointStats || {});
    document.getElementById('active-endpoints').textContent = endpoints.length;
    
    // Average response time
    const responseTimes = Object.values(summary.responseTimeStats || {});
    const avgResponseTime = responseTimes.length > 0 
        ? responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length 
        : 0;
    document.getElementById('avg-response-time').textContent = 
        `${Math.round(avgResponseTime)}ms`;
    
    // Success rate
    let totalSuccess = 0;
    let totalError = 0;
    Object.values(summary.successErrorRates || {}).forEach(rates => {
        totalSuccess += rates.success || 0;
        totalError += rates.error || 0;
    });
    const successRate = (totalSuccess + totalError) > 0 
        ? (totalSuccess / (totalSuccess + totalError) * 100).toFixed(1)
        : 100;
    document.getElementById('success-rate').textContent = `${successRate}%`;
    
    // Create charts
    createEndpointStatsChart(summary.endpointStats || {});
    createResponseTimeChart(summary.responseTimeStats || {});
    createSuccessErrorChart(summary.successErrorRates || {});
}

function displayTimeline(timeline) {
    timelineData = timeline || [];
    renderTimelineTable();
}

function renderTimelineTable() {
    const tbody = document.getElementById('timeline-body');
    tbody.innerHTML = '';
    
    if (timelineData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted" style="padding: 2rem;">
                    No requests recorded yet
                </td>
            </tr>
        `;
        return;
    }
    
    // Sort data
    const sortedData = [...timelineData].sort((a, b) => {
        let aValue, bValue;
        
        switch(timelineSortColumn) {
            case 'timestamp':
                aValue = new Date(a.timestamp).getTime();
                bValue = new Date(b.timestamp).getTime();
                break;
            case 'endpoint':
            case 'method':
                aValue = (a[timelineSortColumn] || '').toLowerCase();
                bValue = (b[timelineSortColumn] || '').toLowerCase();
                return timelineSortDirection === 'asc' 
                    ? aValue.localeCompare(bValue)
                    : bValue.localeCompare(aValue);
            default:
                aValue = a[timelineSortColumn] || 0;
                bValue = b[timelineSortColumn] || 0;
        }
        
        return timelineSortDirection === 'asc' ? aValue - bValue : bValue - aValue;
    });
    
    sortedData.forEach((entry, index) => {
        const row = document.createElement('tr');
        const timestamp = new Date(entry.timestamp).toLocaleString();
        const statusClass = getStatusClass(entry.statusCode);
        const methodClass = getMethodClass(entry.method);
        
        row.innerHTML = `
            <td class="text-muted">${index + 1}</td>
            <td>${timestamp}</td>
            <td><code>${formatEndpoint(entry.endpoint)}</code></td>
            <td><span class="aqi-badge ${methodClass}">${entry.method}</span></td>
            <td class="numeric ${statusClass}">${entry.statusCode}</td>
            <td class="numeric">${entry.responseTime}ms</td>
        `;
        
        tbody.appendChild(row);
    });
}

// ============================================
// Chart Functions
// ============================================

function createEndpointStatsChart(endpointStats) {
    const endpoints = Object.keys(endpointStats).map(formatEndpoint);
    const counts = Object.values(endpointStats);
    
    Highcharts.chart('endpoint-stats-chart', {
        chart: { type: 'column', backgroundColor: 'transparent' },
        credits: { enabled: false },
        title: { text: '' },
        xAxis: { 
            categories: endpoints,
            labels: { style: { fontSize: '11px' }, rotation: -45 }
        },
        yAxis: { 
            min: 0,
            title: { text: 'Request Count' }
        },
        legend: { enabled: false },
        series: [{ 
            data: counts, 
            color: '#3b82f6',
            borderRadius: 4
        }]
    });
}

function createResponseTimeChart(responseTimeStats) {
    const endpoints = Object.keys(responseTimeStats).map(formatEndpoint);
    const times = Object.values(responseTimeStats).map(t => Math.round(t * 100) / 100);
    
    Highcharts.chart('response-time-chart', {
        chart: { type: 'bar', backgroundColor: 'transparent' },
        credits: { enabled: false },
        title: { text: '' },
        xAxis: { 
            categories: endpoints,
            labels: { style: { fontSize: '11px' } }
        },
        yAxis: { 
            title: { text: 'Response Time (ms)' }
        },
        legend: { enabled: false },
        plotOptions: {
            bar: {
                dataLabels: { enabled: true, format: '{y}ms' }
            }
        },
        series: [{ 
            data: times, 
            color: '#22c55e',
            borderRadius: 4
        }]
    });
}

function createSuccessErrorChart(successErrorRates) {
    let totalSuccess = 0;
    let totalError = 0;
    
    Object.values(successErrorRates).forEach(rates => {
        totalSuccess += rates.success || 0;
        totalError += rates.error || 0;
    });
    
    Highcharts.chart('success-error-chart', {
        chart: { type: 'pie', backgroundColor: 'transparent' },
        credits: { enabled: false },
        title: { text: '' },
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
            name: 'Requests',
            colorByPoint: true,
            data: [
                { name: 'Success (2xx)', y: totalSuccess, color: '#22c55e' },
                { name: 'Errors (4xx/5xx)', y: totalError, color: '#ef4444' }
            ]
        }]
    });
}

// ============================================
// Sorting Functions
// ============================================

function sortTimelineTable(column) {
    if (timelineSortColumn === column) {
        timelineSortDirection = timelineSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        timelineSortColumn = column;
        timelineSortDirection = column === 'timestamp' ? 'desc' : 'asc';
    }
    
    renderTimelineTable();
}

// ============================================
// Utility Functions
// ============================================

function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'flex' : 'none';
    document.getElementById('analytics-content').style.display = show ? 'none' : 'block';
}

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

function updateLastUpdated() {
    const now = new Date();
    const formattedDate = now.toLocaleString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
        timeZone: 'UTC'
    }) + ' UTC';
    
    const lastUpdatedElement = document.getElementById('last-updated');
    if (lastUpdatedElement) {
        lastUpdatedElement.textContent = `Last Updated: ${formattedDate}`;
    }
}

function formatNumber(num) {
    if (num === null || num === undefined) return '--';
    return num.toLocaleString('en-US');
}

function formatEndpoint(endpoint) {
    return endpoint.startsWith('/api') ? endpoint.substring(4) : endpoint;
}

function getStatusClass(statusCode) {
    if (statusCode >= 200 && statusCode < 300) return 'aqi-good';
    if (statusCode >= 400 && statusCode < 500) return 'aqi-moderate';
    if (statusCode >= 500) return 'aqi-unhealthy';
    return '';
}

function getMethodClass(method) {
    switch (method) {
        case 'GET': return 'good';
        case 'POST': return 'moderate';
        case 'PUT': return 'sensitive';
        case 'DELETE': return 'unhealthy';
        default: return '';
    }
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
