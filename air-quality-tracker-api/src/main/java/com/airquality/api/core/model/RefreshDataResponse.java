package com.airquality.api.core.model;

/**
 * Response model for data refresh operation
 */
public class RefreshDataResponse {
    
    private String status;
    private String message;
    private int rowsAffected;

    public RefreshDataResponse() {
    }

    public RefreshDataResponse(String status, String message, int rowsAffected) {
        this.status = status;
        this.message = message;
        this.rowsAffected = rowsAffected;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRowsAffected() {
        return rowsAffected;
    }

    public void setRowsAffected(int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }
}
