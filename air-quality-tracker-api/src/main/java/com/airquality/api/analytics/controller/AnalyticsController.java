package com.airquality.api.analytics.controller;

import com.airquality.api.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * REST Controller for analytics endpoints
 * Provides metrics data for dashboard visualization
 * Only enabled when MongoDB URI is configured
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "API Usage Analytics Endpoints")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.data.mongodb.uri")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/summary - Returns aggregated analytics summary
     */
    @GetMapping("/summary")
    @Operation(summary = "Get analytics summary", description = "Returns aggregated analytics including endpoint counts, response times, and success/error rates")
    public ResponseEntity<Map<String, Object>> getSummary() {
        logger.info("GET /api/analytics/summary - Fetching analytics summary");
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get all aggregated stats
        summary.put("endpointStats", analyticsService.getEndpointStats());
        summary.put("responseTimeStats", analyticsService.getResponseTimeStats());
        summary.put("successErrorRates", analyticsService.getSuccessErrorRates());
        summary.put("totalRequests", analyticsService.getTotalRequests());
        
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/analytics/timeline - Returns recent request timeline
     */
    @GetMapping("/timeline")
    @Operation(summary = "Get request timeline", description = "Returns the last 100 API requests with timestamp, endpoint, method, status code, and response time")
    public ResponseEntity<List<Map<String, Object>>> getTimeline() {
        logger.info("GET /api/analytics/timeline - Fetching request timeline");
        return ResponseEntity.ok(analyticsService.getTimeline());
    }

    /**
     * GET /api/analytics/endpoints - Returns endpoint statistics
     */
    @GetMapping("/endpoints")
    @Operation(summary = "Get endpoint statistics", description = "Returns request counts per endpoint")
    public ResponseEntity<Map<String, Long>> getEndpointStats() {
        logger.info("GET /api/analytics/endpoints - Fetching endpoint statistics");
        return ResponseEntity.ok(analyticsService.getEndpointStats());
    }

    /**
     * GET /api/analytics/response-times - Returns response time statistics
     */
    @GetMapping("/response-times")
    @Operation(summary = "Get response time statistics", description = "Returns average response times per endpoint")
    public ResponseEntity<Map<String, Double>> getResponseTimeStats() {
        logger.info("GET /api/analytics/response-times - Fetching response time statistics");
        return ResponseEntity.ok(analyticsService.getResponseTimeStats());
    }
}
