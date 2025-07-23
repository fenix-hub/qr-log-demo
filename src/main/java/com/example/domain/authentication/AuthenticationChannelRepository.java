package com.example.domain.authentication;

/**
 * Port (interface) for authentication channel repository
 * Following hexagonal architecture - this is a port that will be implemented by an adapter
 */
public interface AuthenticationChannelRepository {
    
    /**
     * Creates and stores a new authentication channel
     */
    AuthenticationChannel create();
    
    /**
     * Finds an authentication channel by its ID
     * @param channelId the channel ID to search for
     * @return the channel if found and valid, null otherwise
     */
    AuthenticationChannel findByIdIfValid(String channelId);
    
    /**
     * Marks a channel as used and removes it from active channels
     * @param channelId the channel to mark as used
     */
    void markAsUsed(String channelId);
    
    /**
     * Removes expired channels from the repository
     */
    void cleanupExpiredChannels();
    
    /**
     * Checks if a channel exists and is valid
     * @param channelId the channel ID to check
     * @return true if the channel exists and is valid
     */
    boolean existsAndValid(String channelId);
}
