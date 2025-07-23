package com.example.domain.authentication;

/**
 * Domain exception for authentication failures
 * Specific exception for authentication-related errors
 */
public class AuthenticationException extends Exception {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid username or password");
    }
    
    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid or expired token");
    }
    
    public static AuthenticationException channelNotFound() {
        return new AuthenticationException("Authentication channel not found or expired");
    }
}
