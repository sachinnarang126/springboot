package com.learning.springboot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standardized API Response Wrapper
 * Ensures consistent response format across all endpoints
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in JSON
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private int statusCode;

    // Private constructor - use static factory methods instead
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Success response with data
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        response.statusCode = 200;
        return response;
    }

    // Success response without data
    public static <T> ApiResponse<T> success(String message) {
        return success(null, message);
    }

    // Success response without data but with custom status code
    public static <T> ApiResponse<T> success(String message, int statusCode) {
        ApiResponse<T> response = success(null, message);
        response.statusCode = statusCode;
        return response;
    }

    // Success response with data and custom status code
    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        ApiResponse<T> response = success(data, message);
        response.statusCode = statusCode;
        return response;
    }

    // Error response
    public static <T> ApiResponse<T> error(String error, int statusCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = error;
        response.statusCode = statusCode;
        return response;
    }

    // Error response with default 500 status
    public static <T> ApiResponse<T> error(String error) {
        return error(error, 500);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}

