package com.airquality.api.intelligence.service.impl;

import com.airquality.api.intelligence.model.AirQualityRecommendation;
import com.airquality.api.intelligence.model.RecommendationCard;
import com.airquality.api.intelligence.service.AiService;
import com.airquality.api.shared.exception.CityNotFoundException;
import com.airquality.api.core.model.AirQualityData;
import com.airquality.api.core.service.AirQualityService;
import com.airquality.api.shared.util.AqiCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI-powered air quality analysis using OpenAI API.
 * Generates recommendations and health advisories based on air quality data.
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    private final WebClient webClient;
    private final String model;
    private final ObjectMapper objectMapper;
    private final AirQualityService airQualityService;
    private final boolean aiEnabled;

    public AiServiceImpl(@Value("${openai.api.key:}") String apiKey,
                         @Value("${openai.api.model:gpt-3.5-turbo}") String model,
                         AirQualityService airQualityService) {
        
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.airQualityService = airQualityService;
        
        // Check if AI is enabled
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("OpenAI API key not set. AI features will use fallback recommendations.");
            this.aiEnabled = false;
            this.webClient = null;
        } else {
            this.aiEnabled = true;
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            logger.info("AI service initialized with model: {}", model);
        }
    }

    @Override
    public AirQualityRecommendation generateRecommendations(AirQualityData airQualityData) {
        logger.info("Generating recommendations for city: {}", airQualityData.getCity());

        AirQualityRecommendation recommendation = new AirQualityRecommendation();
        recommendation.setCity(airQualityData.getCity());
        recommendation.setCountry(airQualityData.getCountry());
        recommendation.setAqi(airQualityData.getAqi());
        recommendation.setAqiCategory(airQualityData.getAqiCategory());
        recommendation.setGeneratedAt(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a")));

        if (aiEnabled) {
            try {
                String prompt = buildRecommendationPrompt(airQualityData);
                String aiResponse = callOpenAI(prompt, 0.7);
                parseRecommendationResponse(aiResponse, recommendation);
                return recommendation;
            } catch (Exception e) {
                logger.warn("AI request failed, using fallback: {}", e.getMessage());
            }
        }

        // Fallback recommendations
        recommendation.setOverallAssessment(generateFallbackAssessment(airQualityData));
        recommendation.setRecommendations(generateFallbackRecommendations(airQualityData.getAqi()));
        return recommendation;
    }

    @Override
    public AirQualityRecommendation getRecommendationsForCity(String cityName) {
        AirQualityData cityData = airQualityService.getCityData(cityName);
        
        if (cityData == null) {
            throw new CityNotFoundException("No air quality data available for city: " + cityName);
        }
        
        return generateRecommendations(cityData);
    }

    @Override
    public String generateHealthAdvisory(int aqi) {
        String category = AqiCalculator.getAqiCategory(aqi);
        
        if (aiEnabled) {
            try {
                String prompt = String.format(
                    "Generate a brief health advisory (2-3 sentences) for air quality with AQI of %d (%s). " +
                    "Include specific recommendations for outdoor activities and sensitive groups.",
                    aqi, category);
                return callOpenAI(prompt, 0.5);
            } catch (Exception e) {
                logger.warn("AI request failed for health advisory, using fallback");
            }
        }
        
        return generateFallbackHealthAdvisory(aqi, category);
    }

    @Override
    public String analyzeAirQualityTrends(String cityName) {
        AirQualityData cityData = airQualityService.getCityData(cityName);
        
        if (cityData == null) {
            return "Unable to analyze trends for " + cityName + ": city not found.";
        }
        
        if (aiEnabled) {
            try {
                String prompt = String.format(
                    "Analyze the air quality in %s, %s with current AQI of %d (PM2.5: %.1f μg/m³). " +
                    "Provide insights on potential sources of pollution and recommendations for improvement.",
                    cityData.getCity(), cityData.getCountry(), 
                    cityData.getAqi(), cityData.getPm25() != null ? cityData.getPm25() : 0.0);
                return callOpenAI(prompt, 0.7);
            } catch (Exception e) {
                logger.warn("AI request failed for trend analysis, using fallback");
            }
        }
        
        return String.format(
            "Air quality in %s, %s is currently %s with an AQI of %d. " +
            "Monitor local conditions and follow health guidelines for your activity level.",
            cityData.getCity(), cityData.getCountry(), 
            cityData.getAqiCategory(), cityData.getAqi());
    }

    // Calls OpenAI API using WebClient
    private String callOpenAI(String prompt, double temperature) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 500);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        requestBody.put("messages", new Object[]{userMessage});

        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    // Builds prompt for air quality recommendations
    private String buildRecommendationPrompt(AirQualityData data) {
        return String.format(
            "You are an air quality expert. Based on the following air quality data for %s, %s, " +
            "provide 4 specific recommendations as JSON. " +
            "Current conditions: AQI=%d (%s), PM2.5=%.1f μg/m³, PM10=%.1f μg/m³. " +
            "Return JSON format: {\"assessment\": \"brief overall assessment\", " +
            "\"recommendations\": [{\"title\": \"title\", \"description\": \"description\", " +
            "\"icon\": \"emoji\", \"severity\": \"low/medium/high\"}]}",
            data.getCity(), data.getCountry(),
            data.getAqi(), data.getAqiCategory(),
            data.getPm25() != null ? data.getPm25() : 0.0,
            data.getPm10() != null ? data.getPm10() : 0.0
        );
    }

    // Parses AI response into recommendation object
    private void parseRecommendationResponse(String response, AirQualityRecommendation recommendation) {
        try {
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = response.substring(jsonStart, jsonEnd);
                JsonNode root = objectMapper.readTree(jsonStr);
                
                if (root.has("assessment")) {
                    recommendation.setOverallAssessment(root.get("assessment").asText());
                }
                
                if (root.has("recommendations")) {
                    List<RecommendationCard> cards = new ArrayList<>();
                    for (JsonNode rec : root.get("recommendations")) {
                        RecommendationCard card = new RecommendationCard(
                            rec.path("title").asText("Recommendation"),
                            rec.path("description").asText(""),
                            rec.path("icon").asText("💡"),
                            rec.path("severity").asText("medium")
                        );
                        cards.add(card);
                    }
                    recommendation.setRecommendations(cards);
                    return;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse AI response: {}", e.getMessage());
        }
        
        // Fallback if parsing fails
        recommendation.setOverallAssessment(response);
        recommendation.setRecommendations(generateFallbackRecommendations(recommendation.getAqi()));
    }

    // Generates fallback assessment when AI is unavailable
    private String generateFallbackAssessment(AirQualityData data) {
        int aqi = data.getAqi() != null ? data.getAqi() : 0;
        
        if (aqi <= 50) {
            return String.format("Air quality in %s is good. Enjoy outdoor activities!", data.getCity());
        } else if (aqi <= 100) {
            return String.format("Air quality in %s is moderate. Generally acceptable for most people.", data.getCity());
        } else if (aqi <= 150) {
            return String.format("Air quality in %s is unhealthy for sensitive groups. Limit outdoor exposure.", data.getCity());
        } else {
            return String.format("Air quality in %s is unhealthy. Reduce prolonged outdoor activities.", data.getCity());
        }
    }

    // Generates fallback recommendations based on AQI
    private List<RecommendationCard> generateFallbackRecommendations(Integer aqi) {
        List<RecommendationCard> cards = new ArrayList<>();
        int aqiValue = aqi != null ? aqi : 0;
        
        if (aqiValue <= 50) {
            cards.add(new RecommendationCard("Outdoor Exercise", 
                "Great day for outdoor activities like jogging, cycling, or sports.", "🏃", "low"));
            cards.add(new RecommendationCard("Open Windows", 
                "Feel free to open windows for fresh air ventilation.", "🪟", "low"));
            cards.add(new RecommendationCard("Family Activities", 
                "Perfect conditions for outdoor family activities and picnics.", "👨‍👩‍👧‍👦", "low"));
            cards.add(new RecommendationCard("Garden Time", 
                "Ideal weather for gardening and outdoor work.", "🌱", "low"));
        } else if (aqiValue <= 100) {
            cards.add(new RecommendationCard("Moderate Caution", 
                "Generally safe for outdoor activities with normal precautions.", "⚠️", "low"));
            cards.add(new RecommendationCard("Sensitive Groups", 
                "Those with respiratory issues should consider reducing outdoor exercise.", "🫁", "medium"));
            cards.add(new RecommendationCard("Stay Hydrated", 
                "Drink plenty of water if exercising outdoors.", "💧", "low"));
            cards.add(new RecommendationCard("Monitor Conditions", 
                "Keep an eye on air quality updates throughout the day.", "📱", "low"));
        } else if (aqiValue <= 150) {
            cards.add(new RecommendationCard("Limit Outdoor Time", 
                "Reduce prolonged outdoor exertion, especially for sensitive groups.", "⏰", "medium"));
            cards.add(new RecommendationCard("Use Air Purifier", 
                "Consider running an indoor air purifier.", "🌬️", "medium"));
            cards.add(new RecommendationCard("Wear Mask", 
                "Consider wearing an N95 mask when outdoors.", "😷", "medium"));
            cards.add(new RecommendationCard("Indoor Exercise", 
                "Move workouts indoors when possible.", "🏠", "medium"));
        } else {
            cards.add(new RecommendationCard("Stay Indoors", 
                "Avoid outdoor activities. Keep windows and doors closed.", "🏠", "high"));
            cards.add(new RecommendationCard("Air Purification", 
                "Run air purifiers on high settings. Ensure HEPA filtration.", "🌬️", "high"));
            cards.add(new RecommendationCard("N95 Mask Required", 
                "Wear N95 or better mask if you must go outside.", "😷", "high"));
            cards.add(new RecommendationCard("Health Watch", 
                "Monitor for symptoms. Seek medical help if experiencing breathing difficulties.", "🏥", "high"));
        }
        
        return cards;
    }

    // Generates fallback health advisory
    private String generateFallbackHealthAdvisory(int aqi, String category) {
        if (aqi <= 50) {
            return "Air quality is satisfactory. Enjoy outdoor activities without concern.";
        } else if (aqi <= 100) {
            return "Air quality is acceptable. Unusually sensitive people should consider " +
                   "limiting prolonged outdoor exertion.";
        } else if (aqi <= 150) {
            return "Members of sensitive groups may experience health effects. " +
                   "The general public is less likely to be affected.";
        } else if (aqi <= 200) {
            return "Everyone may begin to experience health effects. Members of sensitive groups " +
                   "may experience more serious health effects.";
        } else if (aqi <= 300) {
            return "Health alert: everyone may experience more serious health effects. " +
                   "Avoid outdoor activities.";
        } else {
            return "Health emergency: the entire population is more likely to be affected. " +
                   "Stay indoors with air filtration.";
        }
    }
}
