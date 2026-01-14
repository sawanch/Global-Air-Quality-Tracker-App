package com.airquality.api.analytics.interceptor;

import com.airquality.api.analytics.model.ApiRequestMetric;
import com.airquality.api.analytics.repository.AnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Interceptor to capture API request metrics and store them in MongoDB
 * Runs asynchronously to avoid blocking API responses
 * Only enabled when MongoDB URI is configured
 */
@Component
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
public class MetricsInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);
    
    private final AnalyticsRepository analyticsRepository;

    public MetricsInterceptor(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Store start time in request attribute
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        String requestPath = request.getRequestURI();
        
        // Only track /api/** endpoints, exclude /actuator/** and /api/analytics/**
        if (!requestPath.startsWith("/api/") || requestPath.startsWith("/api/analytics")) {
            return;
        }

        try {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime == null) {
                return;
            }

            long responseTime = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            int statusCode = response.getStatus();
            LocalDateTime timestamp = LocalDateTime.now();

            // Create metric object
            ApiRequestMetric metric = new ApiRequestMetric(
                requestPath,
                method,
                responseTime,
                statusCode,
                timestamp
            );

            // Add optional fields
            metric.setClientIp(getClientIp(request));
            metric.setUserAgent(request.getHeader("User-Agent"));

            // Save to MongoDB asynchronously (non-blocking)
            CompletableFuture.runAsync(() -> {
                try {
                    analyticsRepository.save(metric);
                    logger.debug("Saved metric: {} {} - {}ms - {}", method, requestPath, responseTime, statusCode);
                } catch (Exception e) {
                    logger.error("Error saving metric to MongoDB", e);
                }
            });

        } catch (Exception e) {
            logger.error("Error capturing metrics", e);
        }
    }

    /**
     * Get client IP address, handling proxy headers
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
