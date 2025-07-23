package com.example.domain.authentication;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing an authentication channel
 * Immutable and contains business logic for channel validation
 */
public final class AuthenticationChannel {
    private final String id;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final boolean active;

    private AuthenticationChannel(String id, Instant createdAt, Instant expiresAt, boolean active) {
        this.id = Objects.requireNonNull(id, "Channel ID cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration timestamp cannot be null");
        this.active = active;
        
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Channel ID cannot be empty");
        }
        if (createdAt.isAfter(expiresAt)) {
            throw new IllegalArgumentException("Created timestamp cannot be after expiration");
        }
    }

    public static AuthenticationChannel create(String id, Instant createdAt, Instant expiresAt) {
        return new AuthenticationChannel(id, createdAt, expiresAt, true);
    }

    public static AuthenticationChannel createExpired(String id, Instant createdAt, Instant expiresAt) {
        return new AuthenticationChannel(id, createdAt, expiresAt, false);
    }

    public boolean isExpired() {
        return !active || Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return active && !isExpired();
    }

    public AuthenticationChannel markAsUsed() {
        return new AuthenticationChannel(id, createdAt, expiresAt, false);
    }

    // Getters
    public String getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationChannel that = (AuthenticationChannel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuthenticationChannel{" +
                "id='" + id + '\'' +
                ", active=" + active +
                ", expired=" + isExpired() +
                '}';
    }
}
