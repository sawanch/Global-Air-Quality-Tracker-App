package com.airquality.api.analytics.repository;

import com.airquality.api.analytics.model.ApiRequestMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for API request metrics
 * Provides query methods for analytics
 * 
 * Note: Spring Data MongoDB automatically implements these methods using query derivation.
 * Method names are parsed to generate MongoDB queries.
 */
@Repository
public interface AnalyticsRepository extends MongoRepository<ApiRequestMetric, String> {

    /**
     * Get all API request metrics from MongoDB
     * Spring generates: db.api_metrics.find({})
     */
    @Override
    List<ApiRequestMetric> findAll();

    /**
     * Find all metrics for a specific endpoint
     * Spring generates: db.api_metrics.find({ endpoint: ? })
     */
    List<ApiRequestMetric> findByEndpoint(String endpoint);

    /**
     * Count total requests for a specific endpoint
     * Spring generates: db.api_metrics.count({ endpoint: ? })
     */
    long countByEndpoint(String endpoint);

    /**
     * Find metrics within a time range
     * Spring generates: db.api_metrics.find({ timestamp: { $gte: start, $lte: end } })
     */
    List<ApiRequestMetric> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find metrics by status code
     */
    List<ApiRequestMetric> findByStatusCode(int statusCode);

    /**
     * Find metrics by method type
     */
    List<ApiRequestMetric> findByMethod(String method);

    /**
     * Find recent metrics ordered by timestamp
     */
    List<ApiRequestMetric> findTop100ByOrderByTimestampDesc();
}
