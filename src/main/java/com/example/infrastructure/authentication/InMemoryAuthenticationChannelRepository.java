package com.example.infrastructure.authentication;

import com.example.domain.authentication.AuthenticationChannel;
import com.example.domain.authentication.AuthenticationChannelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation of AuthenticationChannelRepository
 * This is an adapter implementing the port defined in the domain
 */
@ApplicationScoped
public class InMemoryAuthenticationChannelRepository implements AuthenticationChannelRepository {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryAuthenticationChannelRepository.class);
    private static final int CHANNEL_EXPIRY_MINUTES = 5; // Channels expire after 5 minutes
    private static final int TOKEN_SIZE_BYTES = 32;
    
    private final ConcurrentMap<String, AuthenticationChannel> channels = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AuthenticationChannel create() {
        String channelId = generateSecureChannelId();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(CHANNEL_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        
        AuthenticationChannel channel = AuthenticationChannel.create(channelId, now, expiresAt);
        channels.put(channelId, channel);
        
        LOGGER.info("Created authentication channel: {}", channelId);
        return channel;
    }

    @Override
    public AuthenticationChannel findByIdIfValid(String channelId) {
        if (channelId == null || channelId.trim().isEmpty()) {
            return null;
        }
        
        AuthenticationChannel channel = channels.get(channelId);
        if (channel == null) {
            LOGGER.debug("Channel not found: {}", channelId);
            return null;
        }
        
        if (!channel.isValid()) {
            LOGGER.debug("Channel is invalid or expired: {}", channelId);
            channels.remove(channelId); // Cleanup expired channel
            return null;
        }
        
        return channel;
    }

    @Override
    public void markAsUsed(String channelId) {
        AuthenticationChannel channel = channels.get(channelId);
        if (channel != null) {
            channels.remove(channelId);
            LOGGER.info("Marked channel as used and removed: {}", channelId);
        }
    }

    @Override
    public void cleanupExpiredChannels() {
        int removedCount = 0;
        var iterator = channels.entrySet().iterator();
        
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!entry.getValue().isValid()) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            LOGGER.info("Cleaned up {} expired channels", removedCount);
        }
    }

    @Override
    public boolean existsAndValid(String channelId) {
        return findByIdIfValid(channelId) != null;
    }
    
    private String generateSecureChannelId() {
        byte[] bytes = new byte[TOKEN_SIZE_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    // For testing/monitoring purposes
    public int getActiveChannelCount() {
        cleanupExpiredChannels(); // Clean up before counting
        return channels.size();
    }
}
