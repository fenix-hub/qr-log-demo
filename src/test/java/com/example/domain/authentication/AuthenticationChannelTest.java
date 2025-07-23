package com.example.domain.authentication;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationChannelTest {

    @Test
    void testCreateValidChannel() {
        String id = "test-channel-123";
        Instant now = Instant.now();
        Instant expiresAt = now.plus(5, ChronoUnit.MINUTES);
        
        AuthenticationChannel channel = AuthenticationChannel.create(id, now, expiresAt);
        
        assertEquals(id, channel.getId());
        assertEquals(now, channel.getCreatedAt());
        assertEquals(expiresAt, channel.getExpiresAt());
        assertTrue(channel.isActive());
        assertTrue(channel.isValid());
        assertFalse(channel.isExpired());
    }

    @Test
    void testChannelExpiration() {
        String id = "expired-channel";
        Instant now = Instant.now();
        Instant pastTime = now.minus(1, ChronoUnit.HOURS);
        
        AuthenticationChannel channel = AuthenticationChannel.create(id, pastTime, now.minus(30, ChronoUnit.MINUTES));
        
        assertTrue(channel.isExpired());
        assertFalse(channel.isValid());
    }

    @Test
    void testMarkChannelAsUsed() {
        String id = "used-channel";
        Instant now = Instant.now();
        Instant expiresAt = now.plus(5, ChronoUnit.MINUTES);
        
        AuthenticationChannel channel = AuthenticationChannel.create(id, now, expiresAt);
        AuthenticationChannel usedChannel = channel.markAsUsed();
        
        assertFalse(usedChannel.isActive());
        assertFalse(usedChannel.isValid());
        assertEquals(id, usedChannel.getId());
    }

    @Test
    void testInvalidChannelCreation() {
        Instant now = Instant.now();
        Instant past = now.minus(1, ChronoUnit.HOURS);
        
        // Test null ID
        assertThrows(NullPointerException.class, () -> 
            AuthenticationChannel.create(null, now, now.plus(1, ChronoUnit.HOURS)));
        
        // Test empty ID
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationChannel.create("", now, now.plus(1, ChronoUnit.HOURS)));
        
        // Test created after expiration
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationChannel.create("test", now, past));
    }
}
