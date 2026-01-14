package com.airquality.api.intelligence.controller;

import com.airquality.api.intelligence.model.AirQualityAnalysisResponse;
import com.airquality.api.intelligence.model.AirQualityRecommendation;
import com.airquality.api.intelligence.model.HealthAdvisoryResponse;
import com.airquality.api.intelligence.service.AiService;
import com.airquality.api.shared.util.AqiCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-powered air quality recommendations
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Recommendations", description = "AI-Powered Air Quality Analysis Endpoints")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * GET /api/ai/recommendations/{city} - Get AI recommendations for a city
     */
    @GetMapping("/recommendations/{city}")
    @Operation(summary = "Get AI recommendations", description = "Returns AI-generated recommendations based on air quality for a specific city")
    public ResponseEntity<AirQualityRecommendation> getRecommendations(@PathVariable String city) {
        logger.info("GET /api/ai/recommendations/{} - Fetching AI recommendations", city);
        
        AirQualityRecommendation recommendation = aiService.getRecommendationsForCity(city);
        
        return ResponseEntity.ok(recommendation);
    }

    /**
     * GET /api/ai/health-advisory - Get health advisory based on AQI
     */
    @GetMapping("/health-advisory")
    @Operation(summary = "Get health advisory", description = "Returns a health advisory based on the provided AQI value")
    public ResponseEntity<HealthAdvisoryResponse> getHealthAdvisory(
            @RequestParam(defaultValue = "50") int aqi) {
        logger.info("GET /api/ai/health-advisory - Generating health advisory for AQI: {}", aqi);
        
        String advisory = aiService.generateHealthAdvisory(aqi);
        String category = AqiCalculator.getAqiCategory(aqi);
        
        HealthAdvisoryResponse response = new HealthAdvisoryResponse(aqi, category, advisory);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/ai/analysis/{city} - Get AI analysis for a city
     */
    @GetMapping("/analysis/{city}")
    @Operation(summary = "Get AI analysis", description = "Returns AI-powered analysis of air quality trends for a city")
    public ResponseEntity<AirQualityAnalysisResponse> getAnalysis(@PathVariable String city) {
        
        logger.info("GET /api/ai/analysis/{} - Generating AI analysis", city);
        
        String analysis = aiService.analyzeAirQualityTrends(city);
        
        AirQualityAnalysisResponse response = new AirQualityAnalysisResponse(city, analysis);
        return ResponseEntity.ok(response);
    }
}
