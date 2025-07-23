package com.example.application.ports.primary;

/**
 * Primary port for system-level operations
 * Following hexagonal architecture, this interface exposes system use cases
 */
public interface SystemUseCases {
    
    /**
     * Health check use case
     * @return health status message
     */
    String checkHealth();
    
    /**
     * Welcome message use case
     * @return system welcome message
     */
    String getWelcomeMessage();
}
