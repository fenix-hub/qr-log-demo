package com.example.application.authentication;

import com.example.domain.authentication.*;
import com.example.domain.qr.QRCodeData;
import com.example.domain.qr.QRCodeGenerationException;
import com.example.domain.qr.QRCodeGenerator;
import com.example.application.ports.primary.QRAuthenticationUseCases;
import com.example.application.ports.primary.SystemUseCases;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Application service for QR-based authentication
 * This orchestrates the domain logic and coordinates between different bounded contexts
 */
@ApplicationScoped
public class QRAuthenticationApplicationService implements QRAuthenticationUseCases, SystemUseCases {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QRAuthenticationApplicationService.class);
    
    private final AuthenticationChannelRepository channelRepository;
    private final AuthenticationService authenticationService;
    private final QRCodeGenerator qrCodeGenerator;
    
    @Inject
    public QRAuthenticationApplicationService(
            AuthenticationChannelRepository channelRepository,
            AuthenticationService authenticationService,
            QRCodeGenerator qrCodeGenerator) {
        this.channelRepository = channelRepository;
        this.authenticationService = authenticationService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    /**
     * Creates a new QR code for authentication
     * @param baseUri the base URI for the login page
     * @return map containing QR code data and channel information
     */
    public QRAuthenticationResponse createQRAuthentication(String baseUri) {
        try {
            LOGGER.info("Creating QR authentication for base URI: {}", baseUri);
            
            // Create authentication channel
            AuthenticationChannel channel = channelRepository.create();
            
            // Build login URI with channel parameter
            String loginUri = baseUri + "/login.html?channel=" + channel.getId();
            URI targetUri = new URI(loginUri);
            
            // Generate QR code
            QRCodeData qrCodeData = QRCodeData.of(targetUri);
            String qrCodeBase64 = qrCodeGenerator.generateBase64(qrCodeData);
            
            LOGGER.info("Created QR authentication with channel: {}", channel.getId());
            
            return new QRAuthenticationResponse(qrCodeBase64, channel.getId());
            
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URI for QR authentication: {}", baseUri, e);
            throw new IllegalArgumentException("Invalid base URI", e);
        } catch (QRCodeGenerationException e) {
            LOGGER.error("Failed to generate QR code", e);
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    /**
     * Authenticates user credentials
     * @param credentials the user credentials
     * @return authentication token if successful
     * @throws AuthenticationException if authentication fails
     */
    public AuthenticationToken authenticateUser(AuthenticationCredentials credentials) throws AuthenticationException {
        LOGGER.info("Authenticating user: {}", credentials.getUsername());
        
        return authenticationService.authenticate(credentials);
    }

    /**
     * Processes authentication through a channel
     * @param channelId the authentication channel ID
     * @param credentials the user credentials
     * @return authentication result
     * @throws AuthenticationException if authentication fails
     */
    public ChannelAuthenticationResult authenticateViaChannel(String channelId, AuthenticationCredentials credentials) 
            throws AuthenticationException {
        
        LOGGER.info("Processing channel authentication for channel: {}, user: {}", channelId, credentials.getUsername());
        
        // Verify channel exists and is valid
        AuthenticationChannel channel = channelRepository.findByIdIfValid(channelId);
        if (channel == null) {
            LOGGER.warn("Channel not found or expired: {}", channelId);
            throw AuthenticationException.channelNotFound();
        }
        
        // Authenticate user
        AuthenticationToken token = authenticationService.authenticate(credentials);
        
        // Mark channel as used
        channelRepository.markAsUsed(channelId);
        
        LOGGER.info("Channel authentication successful for channel: {}, user: {}", channelId, credentials.getUsername());
        
        return new ChannelAuthenticationResult(
            credentials.getUsername(),
            token.getValue(),
            "connected"
        );
    }

    /**
     * Checks if a channel is valid
     * @param channelId the channel ID to check
     * @return channel validation result
     */
    public ChannelValidationResult validateChannel(String channelId) {
        if (channelId == null || channelId.trim().isEmpty()) {
            return new ChannelValidationResult(false, "invalid");
        }
        
        boolean isValid = channelRepository.existsAndValid(channelId);
        String status = isValid ? "valid" : "invalid";
        
        LOGGER.debug("Channel validation - ID: {}, Valid: {}", channelId, isValid);
        
        return new ChannelValidationResult(isValid, status);
    }

    /**
     * Cleanup expired channels and tokens
     */
    public void performCleanup() {
        LOGGER.info("Performing cleanup of expired channels and tokens");
        channelRepository.cleanupExpiredChannels();
        
        // If authentication service supports cleanup, call it
        if (authenticationService instanceof com.example.infrastructure.authentication.SimpleAuthenticationService) {
            ((com.example.infrastructure.authentication.SimpleAuthenticationService) authenticationService)
                .cleanupExpiredTokens();
        }
    }
    
    // Response DTOs
    public static class QRAuthenticationResponse {
        private final String qrCode;
        private final String channel;
        
        public QRAuthenticationResponse(String qrCode, String channel) {
            this.qrCode = qrCode;
            this.channel = channel;
        }
        
        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            map.put("qr", qrCode);
            map.put("channel", channel);
            return map;
        }
        
        public String getQrCode() { return qrCode; }
        public String getChannel() { return channel; }
    }
    
    public static class ChannelAuthenticationResult {
        private final String username;
        private final String token;
        private final String status;
        
        public ChannelAuthenticationResult(String username, String token, String status) {
            this.username = username;
            this.token = token;
            this.status = status;
        }
        
        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            map.put("status", status);
            map.put("username", username);
            map.put("token", token);
            return map;
        }
        
        public String getUsername() { return username; }
        public String getToken() { return token; }
        public String getStatus() { return status; }
    }
    
    public static class ChannelValidationResult {
        private final boolean valid;
        private final String status;
        
        public ChannelValidationResult(boolean valid, String status) {
            this.valid = valid;
            this.status = status;
        }
        
        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            map.put("status", status);
            return map;
        }
        
        public boolean isValid() { return valid; }
        public String getStatus() { return status; }
    }
    
    // Implementation of SystemUseCases
    @Override
    public String checkHealth() {
        return "QR Authentication Service is healthy";
    }
    
    @Override
    public String getWelcomeMessage() {
        return "Hello, QR Authentication!";
    }
}
