package com.example.adapters.web.websocket;

import com.example.application.authentication.QRAuthenticationApplicationService.ChannelAuthenticationResult;
import com.example.application.ports.primary.QRAuthenticationUseCases;
import com.example.domain.authentication.AuthenticationCredentials;
import com.example.domain.authentication.AuthenticationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket adapter for real-time authentication channels
 * This is a primary adapter that handles WebSocket connections for the authentication use case
 * Following hexagonal architecture, this adapter translates WebSocket events into domain operations
 */
@ServerEndpoint("/{channel}")
@ApplicationScoped
public class AuthenticationWebSocketAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationWebSocketAdapter.class);
    
    private final QRAuthenticationUseCases authenticationUseCases;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Session> sessions;

    @Inject
    public AuthenticationWebSocketAdapter(QRAuthenticationUseCases authenticationUseCases) {
        this.authenticationUseCases = authenticationUseCases;
        this.objectMapper = new ObjectMapper();
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * Handle WebSocket connection opening
     * Primary port: Channel connection establishment use case
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("channel") String channel) {
        LOGGER.info("WebSocket connection opened for channel: {}", channel);
        
        try {
            // Validate channel immediately when connection opens
            var validationResult = authenticationUseCases.validateChannel(channel);
            
            if (!validationResult.isValid()) {
                LOGGER.warn("Invalid channel connection attempt: {}", channel);
                sendErrorAndClose(session, "invalid_channel", 
                    "This authentication channel does not exist or has expired");
                return;
            }
            
            // Store session for valid channel
            sessions.put(channel, session);
            LOGGER.debug("Session stored for valid channel: {}", channel);
            
        } catch (Exception e) {
            LOGGER.error("Error during WebSocket connection for channel: {}", channel, e);
            sendErrorAndClose(session, "server_error", "Error processing connection");
        }
    }

    /**
     * Handle WebSocket connection closing
     */
    @OnClose
    public void onClose(Session session, @PathParam("channel") String channel) {
        LOGGER.info("WebSocket connection closed for channel: {}", channel);
        sessions.remove(channel);
    }

    /**
     * Handle WebSocket errors
     */
    @OnError
    public void onError(Session session, @PathParam("channel") String channel, Throwable throwable) {
        LOGGER.error("WebSocket error for channel: {}", channel, throwable);
        sessions.remove(channel);
    }

    /**
     * Handle incoming WebSocket messages
     * Primary port: Authentication via channel use case
     */
    @OnMessage
    public void onMessage(String message, @PathParam("channel") String channel) {
        LOGGER.info("WebSocket message received for channel: {}", channel);
        
        try {
            // Parse and validate authentication message
            AuthenticationMessage authMessage = parseAuthenticationMessage(message);
            if (authMessage == null) {
                sendError(channel, "invalid_message", "Invalid authentication message format");
                return;
            }
            /* 
                * Authentication is done through REST
                // Create credentials from message
                AuthenticationCredentials credentials = AuthenticationCredentials.of(
                    authMessage.username(), authMessage.password()
                );
                
                // Process authentication via channel
                var result = authenticationUseCases.authenticateViaChannel(channel, credentials);
                // Send success response
                sendMessage(channel, result.toMap());
            */
            
            var result = new ChannelAuthenticationResult(
                authMessage.username(), 
                authMessage.password(), // Placeholder, actual token generation is done in REST
                "success"
            );
            sendMessage(channel, result.toMap());

            // Close the channel after successful authentication
            closeChannel(channel);
            
            LOGGER.info("Authentication successful for channel: {}, user: {}", 
                channel, result.getUsername());
            
/*         } catch (AuthenticationException e) {
            LOGGER.warn("Authentication failed for channel: {}, error: {}", channel, e.getMessage());
            
            if (e.getMessage().contains("channel")) {
                sendError(channel, "expired_channel", e.getMessage());
            } else {
                sendError(channel, "authentication_failed", e.getMessage());
            } */
        } catch (Exception e) {
            LOGGER.error("Unexpected error processing message for channel: {}", channel, e);
            sendError(channel, "server_error", "Error processing authentication");
        }
    }
    
    /**
     * Parse authentication message from WebSocket
     * Expected format: "auth:username:password"
     */
    private AuthenticationMessage parseAuthenticationMessage(String message) {
        if (message == null || !message.startsWith("auth:")) {
            return null;
        }
        
        String[] parts = message.split(":", 3);
        if (parts.length < 3) {
            return null;
        }
        
        return new AuthenticationMessage(parts[1], parts[2]);
    }
    
    /**
     * Send message to a specific channel
     */
    private void sendMessage(String channel, Object messageData) {
        Session session = sessions.get(channel);
        if (session == null || !session.isOpen()) {
            LOGGER.warn("Cannot send message, no active session for channel: {}", channel);
            return;
        }
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageData);
            session.getOpenSessions().forEach(s -> {
                if (s.getPathParameters().get("channel").equals(channel)) {
                    s.getAsyncRemote().sendText(jsonMessage);
                }
            });
            LOGGER.debug("Message sent to channel: {}", channel);
            
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing message for channel: {}", channel, e);
        }
    }
    
    /**
     * Send error message to a specific channel
     */
    private void sendError(String channel, String errorCode, String errorMessage) {
        ErrorMessage error = new ErrorMessage(errorCode, errorMessage);
        sendMessage(channel, error);
    }
    
    /**
     * Send error message and immediately close the session
     */
    private void sendErrorAndClose(Session session, String errorCode, String errorMessage) {
        try {
            ErrorMessage error = new ErrorMessage(errorCode, errorMessage);
            String jsonMessage = objectMapper.writeValueAsString(error);
            session.getBasicRemote().sendText(jsonMessage);
            
        } catch (Exception e) {
            LOGGER.error("Error sending error message", e);
        } finally {
            closeSession(session);
        }
    }
    
    /**
     * Close a channel and its associated session
     */
    private void closeChannel(String channel) {
        Session session = sessions.remove(channel);
        if (session != null) {
            closeSession(session);
        }
    }
    
    /**
     * Safely close a WebSocket session
     */
    private void closeSession(Session session) {
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing WebSocket session", e);
        }
    }
    
    /**
     * Record for authentication message parsing
     * Internal data structure for message handling
     */
    private record AuthenticationMessage(String username, String password) {}
    
    /**
     * Record for error messages sent to clients
     * External contract for error communication
     */
    public record ErrorMessage(String status, String message) {}
}
