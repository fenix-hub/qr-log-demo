package com.example.domain.authentication;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationCredentialsTest {

    @Test
    void testValidCredentials() {
        String username = "testuser";
        String password = "password123";
        
        AuthenticationCredentials credentials = AuthenticationCredentials.of(username, password);
        
        assertEquals(username, credentials.getUsername());
        assertEquals(password, credentials.getPassword());
    }

    @Test
    void testUsernameSanitization() {
        String dirtyUsername = "user<script>alert('xss')</script>";
        String cleanUsername = "userscriptalert('xss')/script";
        
        AuthenticationCredentials credentials = AuthenticationCredentials.of(dirtyUsername, "password");
        
        assertEquals(cleanUsername, credentials.getUsername());
    }

    @Test
    void testInvalidCredentials() {
        // Test null username
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationCredentials.of(null, "password"));
        
        // Test empty username
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationCredentials.of("", "password"));
        
        // Test null password
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationCredentials.of("username", null));
        
        // Test short password
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationCredentials.of("username", "12"));
        
        // Test long username
        String longUsername = "a".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> 
            AuthenticationCredentials.of(longUsername, "password"));
    }

    @Test
    void testCredentialsEquality() {
        AuthenticationCredentials creds1 = AuthenticationCredentials.of("user", "pass");
        AuthenticationCredentials creds2 = AuthenticationCredentials.of("user", "different");
        AuthenticationCredentials creds3 = AuthenticationCredentials.of("user", "pass");
        
        assertEquals(creds1, creds3);
        assertEquals(creds1, creds2); // Equality is based on username only
        assertEquals(creds1.hashCode(), creds3.hashCode());
    }
}
