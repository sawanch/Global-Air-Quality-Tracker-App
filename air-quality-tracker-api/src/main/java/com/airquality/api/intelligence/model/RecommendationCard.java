package com.airquality.api.intelligence.model;

/**
 * Model representing a single recommendation card
 * Used to provide specific, actionable recommendations based on air quality conditions
 */
public class RecommendationCard {
    
    private String title;
    private String description;
    private String icon;
    private String severity; // low, medium, high

    public RecommendationCard() {
    }

    public RecommendationCard(String title, String description, String icon, String severity) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.severity = severity;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
