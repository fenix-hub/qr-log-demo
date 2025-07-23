package com.example.domain.authentication;

/**
 * Port (interface) for authentication service
 * Domain service that handles authentication business logic
 */
public interface AuthenticationService {
    
    /**
     * Authenticates user credentials and returns a token
     * @param credentials the user credentials
     * @return authentication token if successful
     * @throws AuthenticationException if authentication fails
     */
    AuthenticationToken authenticate(AuthenticationCredentials credentials) throws AuthenticationException;
    
    /**
     * Validates if a token is still valid
     * @param token the token to validate
     * @return true if token is valid
     */
    boolean isTokenValid(AuthenticationToken token);
}
