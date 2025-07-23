# QR Authentication System - Architecture 

## Overview

This project has been refactored to follow **SOLID principles**, **Domain-Driven Design (DDD)**, and **Hexagonal Architecture** patterns. The new architecture provides better security, maintainability, and extensibility.

## Architecture

### Domain Layer (`com.example.domain`)
Contains the core business logic and rules:

- **`authentication/`**: Authentication bounded context
  - `AuthenticationChannel`: Value object representing authentication channels
  - `AuthenticationCredentials`: Value object with input validation
  - `AuthenticationToken`: Secure token representation
  - `AuthenticationService`: Domain service interface
  - `AuthenticationChannelRepository`: Domain repository interface
  - `AuthenticationException`: Domain-specific exception

- **`qr/`**: QR code generation bounded context
  - `QRCodeData`: Value object for QR code parameters
  - `QRCodeGenerator`: Interface for QR code generation
  - `QRCodeGenerationException`: Domain-specific exception

### Application Layer (`com.example.application`)
Orchestrates domain logic and coordinates between bounded contexts:

- `QRAuthenticationApplicationService`: Main application service that:
  - Creates QR codes for authentication
  - Processes user authentication
  - Manages authentication channels
  - Coordinates between domain services

### Infrastructure Layer (`com.example.infrastructure`)
Implements domain interfaces with concrete technology choices:

- **`authentication/`**:
  - `InMemoryAuthenticationChannelRepository`: In-memory channel storage
  - `SimpleAuthenticationService`: Basic authentication implementation

- **`qr/`**:
  - `NayukiQRCodeGenerator`: QR code generation using Nayuki library

### Adapters (Controllers/WebSockets)
Handle external communication:

- `ExampleResource`: REST API controller
- `ChannelSocket`: WebSocket handler for real-time authentication

## Key Improvements

### 1. Security Enhancements
- **Input Validation**: All inputs are validated at domain level
- **Secure Token Generation**: Uses `SecureRandom` for cryptographically secure tokens
- **XSS Prevention**: Username sanitization removes dangerous characters
- **Channel Expiration**: Authentication channels expire after 5 minutes
- **Token Expiration**: Authentication tokens expire after 24 hours

### 2. SOLID Principles Applied
- **Single Responsibility**: Each class has one clear responsibility
- **Open/Closed**: Easy to extend with new authentication methods
- **Liskov Substitution**: Interfaces can be substituted with different implementations
- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: High-level modules don't depend on low-level modules

### 3. Domain-Driven Design
- **Bounded Contexts**: Authentication and QR generation are separate contexts
- **Value Objects**: Immutable objects with business logic
- **Domain Services**: Business logic that doesn't belong to entities
- **Repository Pattern**: Abstract data access

### 4. Hexagonal Architecture
- **Ports**: Interfaces defining what the application does
- **Adapters**: Implementations of how the application does it
- **Domain Independence**: Core logic doesn't depend on external frameworks

### 5. Better Error Handling
- **Typed Exceptions**: Domain-specific exceptions
- **Proper HTTP Status Codes**: REST API returns appropriate status codes
- **Comprehensive Logging**: Structured logging with SLF4J
- **Graceful Degradation**: Handles errors without crashing

### 6. Code Quality
- **Immutability**: Value objects are immutable
- **Null Safety**: Proper null checks and validation
- **Documentation**: Comprehensive JavaDoc
- **Testing**: Unit tests for domain logic
- **Clean Code**: Self-documenting code with clear naming

## Usage

### Creating QR Code for Authentication
```bash
GET /qr
```
Returns a QR code that links to the login page with a unique channel.

### User Authentication
```bash
POST /login
Content-Type: application/x-www-form-urlencoded

username=user&password=password
```

### Channel Validation
```bash
GET /channel-check?channel=CHANNEL_ID
```

### Health Check
```bash
GET /health
```

## Configuration

Key configuration properties in `application.properties`:

```properties
# Logging
quarkus.log.level = INFO
quarkus.log.category."com.example".level = DEBUG

# CORS
quarkus.http.cors=true
quarkus.http.cors.origins = /.*/
```

## Migration from Legacy Code

The old `Service` class is marked as `@Deprecated` and will be removed in a future version. Migration steps:

1. Replace direct `Service` usage with `QRAuthenticationApplicationService`
2. Use domain objects (`AuthenticationCredentials`, `AuthenticationToken`) instead of raw strings
3. Handle typed exceptions (`AuthenticationException`) instead of generic exceptions
4. Update WebSocket handlers to use the new architecture

## Testing

Run tests with:
```bash
./mvnw test
```

The test suite includes:
- Domain logic validation tests
- Input validation tests
- Security tests
- Integration tests

## Security Considerations

- **Authentication**: Simple demo authentication (replace with proper system in production)
- **Token Storage**: Tokens stored in memory (use persistent storage in production)
- **HTTPS**: Use HTTPS in production
- **Rate Limiting**: Consider adding rate limiting for authentication endpoints
- **Audit Logging**: Add audit logging for security events

## Future Enhancements

- Persistent storage for channels and tokens
- JWT token implementation
- OAuth2/OpenID Connect integration
- Rate limiting and DDoS protection
- Audit logging
- Multi-factor authentication
- Password complexity requirements
- Account lockout policies
