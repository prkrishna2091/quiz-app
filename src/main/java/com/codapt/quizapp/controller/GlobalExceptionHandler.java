package com.codapt.quizapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";
    private static final String PATH = "path";

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");

        // favicon and unknown paths are expected browser/framework behavior; keep these logs less noisy.
        logger.warn("Resource not found for path {}: {}", requestPath, ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(MESSAGE, "Resource not found");
        errorDetails.put(DETAILS, ex.getMessage());
        errorDetails.put(PATH, requestPath);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        logger.warn("Malformed or missing request body for path {}", requestPath);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(MESSAGE, "Request body is missing or malformed");
        errorDetails.put(DETAILS, ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        errorDetails.put(PATH, requestPath);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(PATH, request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Invalid argument: {}", ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(PATH, request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, LocalDateTime.now());
        errorDetails.put(MESSAGE, "An unexpected error occurred");
        errorDetails.put(DETAILS, ex.getMessage());
        errorDetails.put(PATH, request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }
}
