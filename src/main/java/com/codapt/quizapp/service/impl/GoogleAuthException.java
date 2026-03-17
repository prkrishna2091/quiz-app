package com.codapt.quizapp.service.impl;

/**
 * Runtime exception for Google authentication/verification errors.
 */
public class GoogleAuthException extends RuntimeException {
    public GoogleAuthException(String message) {
        super(message);
    }

    public GoogleAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

