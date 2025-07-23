package com.example.infrastructure.authentication;

import com.example.domain.authentication.AuthenticationCredentials;
import com.example.domain.authentication.AuthenticationException;
import com.example.domain.authentication.AuthenticationService;
import com.example.domain.authentication.AuthenticationToken;
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
 * Simple implementation of AuthenticationService
 * In a real application, this would integrate with a proper user store and authentication system
 */
@ApplicationScoped
public class SimpleAuthenticationService implements AuthenticationService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAuthenticationService.class);
    private static final int TOKEN_SIZE_BYTES = 32;
    private static final int TOKEN_EXPIRY_HOURS = 24; // Tokens expire after 24 hours
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();
    
    // Simple user store for demo purposes - in real app this would be a database
    private final ConcurrentMap<String, String> userStore = new ConcurrentHashMap<>();
    
    public SimpleAuthenticationService() {
        // Initialize with some demo users
        userStore.put("admin", "admin123");
        userStore.put("user", "password");
        userStore.put("demo", "demo");
    }

    @Override
    public AuthenticationToken authenticate(AuthenticationCredentials credentials) throws AuthenticationException {
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        
        LOGGER.info("Authentication attempt for user: {}", username);
        
        // Validate credentials
        String storedPassword = userStore.get(username);
        if (storedPassword == null || !storedPassword.equals(password)) {
            LOGGER.warn("Authentication failed for user: {}", username);
            throw AuthenticationException.invalidCredentials();
        }
        
        // Generate token
        String tokenValue = generateSecureToken();
        Instant expiresAt = Instant.now().plus(TOKEN_EXPIRY_HOURS, ChronoUnit.HOURS);
        
        TokenInfo tokenInfo = new TokenInfo(username, expiresAt);
        activeTokens.put(tokenValue, tokenInfo);
        
        LOGGER.info("Authentication successful for user: {}", username);
        return AuthenticationToken.of(tokenValue);
    }

    @Override
    public boolean isTokenValid(AuthenticationToken token) {
        if (token == null) {
            return false;
        }
        
        TokenInfo tokenInfo = activeTokens.get(token.getValue());
        if (tokenInfo == null) {
            return false;
        }
        
        if (tokenInfo.isExpired()) {
            activeTokens.remove(token.getValue()); // Cleanup expired token
            return false;
        }
        
        return true;
    }
    
    /**
     * Get username associated with a valid token
     * @param token the token to lookup
     * @return username if token is valid, null otherwise
     */
    public String getUsernameForToken(AuthenticationToken token) {
        if (!isTokenValid(token)) {
            return null;
        }
        
        TokenInfo tokenInfo = activeTokens.get(token.getValue());
        return tokenInfo != null ? tokenInfo.username : null;
    }
    
    /**
     * Invalidate a token (logout)
     * @param token the token to invalidate
     */
    public void invalidateToken(AuthenticationToken token) {
        if (token != null) {
            activeTokens.remove(token.getValue());
            LOGGER.info("Token invalidated");
        }
    }
    
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_SIZE_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Cleanup expired tokens
     */
    public void cleanupExpiredTokens() {
        int removedCount = 0;
        var iterator = activeTokens.entrySet().iterator();
        
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            LOGGER.info("Cleaned up {} expired tokens", removedCount);
        }
    }
    
    // Helper class to store token information
    private static class TokenInfo {
        final String username;
        final Instant expiresAt;
        
        TokenInfo(String username, Instant expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
        
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
