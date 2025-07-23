package com.example.domain.authentication;

import java.util.Objects;

/**
 * Value object representing authentication credentials
 * Immutable and validates input data
 */
public final class AuthenticationCredentials {
    private final String username;
    private final String password;

    private AuthenticationCredentials(String username, String password) {
        this.username = validateUsername(username);
        this.password = validatePassword(password);
    }

    public static AuthenticationCredentials of(String username, String password) {
        return new AuthenticationCredentials(username, password);
    }

    private String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (username.length() > 255) {
            throw new IllegalArgumentException("Username cannot exceed 255 characters");
        }
        // Sanitize input - remove potentially dangerous characters
        return username.trim().replaceAll("[<>\"'&]", "");
    }

    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 3) { // Very basic validation for demo
            throw new IllegalArgumentException("Password must be at least 3 characters long");
        }
        return password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationCredentials that = (AuthenticationCredentials) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "AuthenticationCredentials{username='" + username + "'}";
    }
}
