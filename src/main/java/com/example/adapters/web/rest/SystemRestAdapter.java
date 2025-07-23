package com.example.adapters.web.rest;

import com.example.application.ports.primary.SystemUseCases;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * REST adapter for system health and monitoring operations
 * This adapter provides system health endpoints and maintenance operations
 */
@Path("/api/system")
@Produces(MediaType.APPLICATION_JSON)
public class SystemRestAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemRestAdapter.class);
    
    private final SystemUseCases systemUseCases;

    @Inject
    public SystemRestAdapter(SystemUseCases systemUseCases) {
        this.systemUseCases = systemUseCases;
    }

    /**
     * Health check endpoint
     * Primary port: System health monitoring use case
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        try {
            String healthStatus = systemUseCases.checkHealth();
            
            return Response.ok(Map.of(
                "status", "healthy",
                "message", healthStatus,
                "timestamp", Instant.now().toString(),
                "version", "2.0"
            )).build();
            
        } catch (Exception e) {
            LOGGER.error("Health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of(
                        "status", "unhealthy", 
                        "error", e.getMessage(),
                        "timestamp", Instant.now().toString()
                    ))
                    .build();
        }
    }

    /**
     * Basic application info endpoint
     */
    @GET
    @Path("/info")
    public Response getApplicationInfo() {
        return Response.ok(Map.of(
            "application", "QR Authentication System",
            "version", "2.0",
            "architecture", "Hexagonal Architecture with DDD",
            "timestamp", Instant.now().toString()
        )).build();
    }

    /**
     * Manual cleanup trigger for maintenance
     * Primary port: System maintenance use case
     */
    @POST
    @Path("/cleanup")
    public Response triggerCleanup() {
        try {
            LOGGER.info("Manual cleanup triggered");
            String healthStatus = systemUseCases.checkHealth();
            
            return Response.ok(Map.of(
                "status", "cleanup_completed",  
                "healthStatus", healthStatus,
                "timestamp", Instant.now().toString()
            )).build();
            
        } catch (Exception e) {
            LOGGER.error("Manual cleanup failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "status", "cleanup_failed",
                        "error", e.getMessage(),
                        "timestamp", Instant.now().toString()
                    ))
                    .build();
        }
    }
}
