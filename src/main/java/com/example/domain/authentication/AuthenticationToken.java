package com.example.domain.authentication;

import java.util.Objects;

/**
 * Value object representing an authentication token
 * Immutable and secure
 */
public final class AuthenticationToken {
    private final String value;

    private AuthenticationToken(String value) {
        this.value = Objects.requireNonNull(value, "Token value cannot be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Token value cannot be empty");
        }
    }

    public static AuthenticationToken of(String value) {
        return new AuthenticationToken(value);
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationToken that = (AuthenticationToken) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "AuthenticationToken{value='***'}"; // Don't expose token in logs
    }
}
