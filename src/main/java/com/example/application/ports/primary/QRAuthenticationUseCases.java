package com.example.application.ports.primary;

import com.example.domain.authentication.AuthenticationCredentials;
import com.example.domain.authentication.AuthenticationException;
import com.example.domain.authentication.AuthenticationToken;
import com.example.application.authentication.QRAuthenticationApplicationService.QRAuthenticationResponse;
import com.example.application.authentication.QRAuthenticationApplicationService.ChannelValidationResult;
import com.example.application.authentication.QRAuthenticationApplicationService.ChannelAuthenticationResult;

/**
 * Primary port for QR-based authentication use cases
 * This defines the contract that primary adapters (REST, WebSocket) will use
 * Following hexagonal architecture, this is the interface between the outside world and the application
 */
public interface QRAuthenticationUseCases {
    
    /**
     * Creates a new QR code for authentication
     * Use case: Generate QR code for mobile authentication
     * 
     * @param baseUri the base URI for the login page
     * @return QR authentication response containing code and channel info
     * @throws IllegalArgumentException if baseUri is invalid
     */
    QRAuthenticationResponse createQRAuthentication(String baseUri);
    
    /**
     * Authenticates user credentials
     * Use case: Authenticate user with username/password
     * 
     * @param credentials the user credentials
     * @return authentication token if successful
     * @throws AuthenticationException if authentication fails
     */
    AuthenticationToken authenticateUser(AuthenticationCredentials credentials) throws AuthenticationException;
    
    /**
     * Processes authentication through a specific channel
     * Use case: Authenticate via QR code channel (WebSocket)
     * 
     * @param channelId the authentication channel ID
     * @param credentials the user credentials
     * @return authentication result with token and status
     * @throws AuthenticationException if authentication fails
     */
    ChannelAuthenticationResult authenticateViaChannel(String channelId, AuthenticationCredentials credentials) 
            throws AuthenticationException;
    
    /**
     * Validates if a channel exists and is active
     * Use case: Check channel validity before attempting authentication
     * 
     * @param channelId the channel ID to validate
     * @return validation result with status
     */
    ChannelValidationResult validateChannel(String channelId);
}
