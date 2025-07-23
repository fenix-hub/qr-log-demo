package com.example.adapters.web.rest;

import com.example.application.ports.primary.QRAuthenticationUseCases;
import com.example.domain.authentication.AuthenticationCredentials;
import com.example.domain.authentication.AuthenticationException;
import com.example.domain.authentication.AuthenticationToken;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * REST adapter for authentication operations
 * This is a primary adapter that exposes HTTP endpoints for the authentication use cases
 * Following hexagonal architecture, this adapter translates HTTP requests into domain operations
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationRestAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationRestAdapter.class);
    
    private final QRAuthenticationUseCases authenticationUseCases;

    @Inject
    public AuthenticationRestAdapter(QRAuthenticationUseCases authenticationUseCases) {
        this.authenticationUseCases = authenticationUseCases;
    }

    /**
     * Generates QR code for authentication
     * Primary port: QR code generation use case
     */
    @GET
    @Path("/qr")
    public Response generateQRCode(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo) {
        try {
            String baseUri = buildBaseUri(httpHeaders, uriInfo);
            LOGGER.info("QR code requested for base URI: {}", baseUri);
            
            var response = authenticationUseCases.createQRAuthentication(baseUri);
            return Response.ok(response.toMap()).build();
            
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid request for QR code generation: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid request: " + e.getMessage()))
                    .build();
                    
        } catch (Exception e) {
            LOGGER.error("Failed to generate QR code", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "QR code generation failed"))
                    .build();
        }
    }

    /**
     * Authenticates user with username and password
     * Primary port: User authentication use case
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response authenticateUser(
            @FormParam("username") String username, 
            @FormParam("password") String password) {
        
        try {
            // Domain object creation handles validation
            AuthenticationCredentials credentials = AuthenticationCredentials.of(username, password);
            AuthenticationToken token = authenticationUseCases.authenticateUser(credentials);
            
            LOGGER.info("User authenticated successfully: {}", credentials.getUsername());
            return Response.ok(Map.of("token", token.getValue())).build();
            
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid authentication request: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
                    
        } catch (AuthenticationException e) {
            LOGGER.warn("Authentication failed: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
                    
        } catch (Exception e) {
            LOGGER.error("Unexpected error during authentication", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Authentication service temporarily unavailable"))
                    .build();
        }
    }

    /**
     * Validates if a channel exists and is valid
     * Primary port: Channel validation use case
     */
    @GET
    @Path("/channel/{channelId}/validate")
    public Response validateChannel(@PathParam("channelId") String channelId) {
        try {
            if (channelId == null || channelId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Channel ID is required"))
                        .build();
            }
            
            var result = authenticationUseCases.validateChannel(channelId);
            return Response.ok(result.toMap()).build();
            
        } catch (Exception e) {
            LOGGER.error("Error validating channel: {}", channelId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Channel validation failed"))
                    .build();
        }
    }
    
    /**
     * Builds the base URI from headers and request info
     * Helper method for URI construction in proxy/load balancer scenarios
     */
    private String buildBaseUri(HttpHeaders httpHeaders, UriInfo uriInfo) {
        // Check for forwarded headers (for proxy/load balancer scenarios)
        String forwardedHost = httpHeaders.getHeaderString("X-Forwarded-Host");
        String forwardedScheme = httpHeaders.getHeaderString("X-Forwarded-Proto");
        String forwardedPort = httpHeaders.getHeaderString("X-Forwarded-Port");
        
        if (forwardedHost != null && forwardedScheme != null) {
            UriBuilder uriBuilder = UriBuilder.fromUri("")
                    .scheme(forwardedScheme)
                    .host(forwardedHost);
            
            if (forwardedPort != null) {
                try {
                    int port = Integer.parseInt(forwardedPort);
                    // Only add port if it's not the default port for the scheme
                    if (!isDefaultPort(forwardedScheme, port)) {
                        uriBuilder.port(port);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid forwarded port: {}", forwardedPort);
                }
            }
            
            return uriBuilder.build().toString();
        }
        
        // Fallback to request URI
        return uriInfo.getBaseUri().toString().replaceAll("/$", "");
    }
    
    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equals(scheme) && port == 80) || 
               ("https".equals(scheme) && port == 443);
    }
}
