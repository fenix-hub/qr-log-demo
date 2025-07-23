# QR Authentication Demo

A modern, secure authentication system demonstrating QR code-based login with real-time WebSocket communication. Built with Quarkus, following hexagonal architecture, domain-driven design (DDD), and SOLID principles.

## 🚀 Features

- **QR Code Authentication**: Generate QR codes for secure, contactless login
- **Real-time Communication**: WebSocket-based authentication channels
- **Traditional Login**: Fallback username/password authentication
- **Security First**: Input validation, secure token generation, and XSS prevention
- **Modern Architecture**: Hexagonal architecture with clear separation of concerns
- **Production Ready**: Multiple deployment options including native compilation

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports and Adapters) with **Domain-Driven Design**:

```
┌──────────────────────────────────────────────────────────────┐
│                    Adapters (Web Layer)                      │
│  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ REST Controllers │  │ WebSocket       │  │ Static Pages │ │
│  │                  │  │ Handlers        │  │              │ │
│  └──────────────────┘  └─────────────────┘  └──────────────┘ │
└──────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│                 Application Layer                            │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │         QRAuthenticationApplicationService              │ │
│  │    (Orchestrates domain logic and use cases)            │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│                    Domain Layer                              │
│  ┌───────────────────┐           ┌─────────────────────────┐ │
│  │ Authentication    │           │ QR Code Generation      │ │
│  │ Bounded Context   │           │ Bounded Context         │ │
│  │                   │           │                         │ │
│  │ • AuthChannel     │           │ • QRCodeData            │ │
│  │ • Credentials     │           │ • QRCodeGenerator       │ │
│  │ • AuthToken       │           │ • QRCodeException       │ │
│  │ • AuthService     │           │                         │ │
│  └───────────────────┘           └─────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
┌──────────────────────────────────────────────────────────────┐
│                Infrastructure Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐  │
│  │ In-Memory       │  │ Simple Auth     │  │ Nayuki QR    │  │
│  │ Channel Repo    │  │ Service         │  │ Generator    │  │
│  └─────────────────┘  └─────────────────┘  └──────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### Key Components

**Domain Layer** (Business Logic):
- `AuthenticationChannel`: Represents authentication sessions
- `AuthenticationCredentials`: User credentials with validation
- `AuthenticationToken`: Secure authentication tokens
- `QRCodeData`: QR code generation parameters

**Application Layer** (Use Cases):
- `QRAuthenticationApplicationService`: Main orchestrator for authentication flows

**Adapters** (External Interface):
- `AuthenticationRestAdapter`: REST API endpoints
- `AuthenticationWebSocketAdapter`: Real-time WebSocket communication
- `SystemRestAdapter`: Health checks and system information

**Infrastructure** (Technical Implementation):
- `InMemoryAuthenticationChannelRepository`: Channel storage
- `SimpleAuthenticationService`: Authentication logic
- `NayukiQRCodeGenerator`: QR code generation

## 🛠️ Technology Stack

- **Framework**: [Quarkus 3.5.2](https://quarkus.io/) (Supersonic Subatomic Java)
- **Language**: Java 17
- **QR Generation**: [Nayuki QR Code Generator](https://www.nayuki.io/page/qr-code-generator-library)
- **WebSockets**: Quarkus WebSockets extension
- **REST**: Quarkus RESTEasy Reactive
- **JSON**: Jackson
- **Testing**: JUnit 5, REST Assured
- **Build**: Maven
- **Containerization**: Docker (multiple variants)

## 🚦 Quick Start

### Prerequisites

- Java 17 or later
- Maven 3.8.1 or later

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd qr-log-demo
   ```

2. **Run in development mode**
   ```bash
   ./mvnw quarkus:dev
   ```

3. **Access the application**
   - Main page: http://localhost:8080
   - QR Login: http://localhost:8080/qr.html
   - Traditional Login: http://localhost:8080/login.html
   - Health Check: http://localhost:8080/q/health

### Building for Production

1. **Build JAR**
   ```bash
   ./mvnw clean package
   java -jar target/quarkus-app/quarkus-run.jar
   ```

2. **Build native executable** (requires GraalVM)
   ```bash
   ./mvnw clean package -Pnative
   ./target/qr-log-1.0-SNAPSHOT-runner
   ```

3. **Docker deployment**
   ```bash
   # JVM-based container
   docker build -f src/main/docker/Dockerfile.jvm -t qr-log-demo:jvm .
   
   # Native container (smaller, faster startup)
   docker build -f src/main/docker/Dockerfile.native -t qr-log-demo:native .
   
   # Run container
   docker run -p 8080:8080 qr-log-demo:jvm
   ```

## 📱 How It Works

### QR Code Authentication Flow

1. **QR Code Generation**
   - User visits `/qr.html`
   - System generates unique authentication channel
   - QR code contains URL to login page with channel parameter
   - WebSocket connection established for real-time updates

2. **Mobile Authentication**
   - User scans QR code with mobile device
   - Mobile browser opens login page with channel parameter
   - User enters credentials and submits form
   - Server authenticates and sends success message via WebSocket

3. **Desktop Notification**
   - Desktop browser receives WebSocket message
   - Displays authentication success with user details
   - QR code replaced with success message

### Traditional Login Flow

1. User visits `/login.html`
2. Enters username and password
3. Server validates credentials
4. Returns authentication token
5. Token stored in localStorage for future use

## 🔒 Security Features

- **Input Validation**: All user inputs validated at domain level
- **XSS Prevention**: Username sanitization and output encoding
- **Secure Token Generation**: Cryptographically secure random tokens
- **Channel Expiration**: Authentication channels expire after 5 minutes
- **Token Expiration**: Authentication tokens expire after 24 hours
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Audit Logging**: Comprehensive logging of authentication events

## 🛡️ API Reference

### REST Endpoints

#### Generate QR Code
```http
GET /auth/qr
```
**Response:**
```json
{
  "qr": "base64-encoded-qr-image",
  "channel": "unique-channel-id"
}
```

#### User Authentication
```http
POST /auth/login
Content-Type: application/x-www-form-urlencoded

username=user&password=password
```
**Response:**
```json
{
  "token": "authentication-token"
}
```

#### Channel Validation
```http
GET /auth/channel/{channelId}/validate
```
**Response:**
```json
{
  "status": "valid|invalid"
}
```

#### Health Check
```http
GET /q/health
```

### WebSocket Endpoints

#### Authentication Channel
```
ws://localhost:8080/{channelId}
```

**Message Format:**
```
auth:username:password
```

**Response Format:**
```json
{
  "status": "connected|error",
  "username": "user",
  "token": "auth-token",
  "message": "success-or-error-message"
}
```

## 🧪 Testing

Run the complete test suite:
```bash
./mvnw test
```

Run integration tests:
```bash
./mvnw verify
```

The test suite includes:
- Unit tests for domain logic
- Input validation tests
- Security validation tests
- Integration tests with REST Assured

## 📊 Monitoring and Health

### Health Endpoints
- `/q/health` - Application health status
- `/q/health/live` - Liveness probe
- `/q/health/ready` - Readiness probe

### Logging
Structured logging with SLF4J:
- Application logs: `DEBUG` level for `com.example`
- System logs: `INFO` level
- Console format includes timestamp, level, class, and thread

## 🚀 Deployment Options

### 1. Traditional JAR Deployment
```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

### 2. Native Executable (GraalVM)
```bash
./mvnw clean package -Pnative
./target/qr-log-1.0-SNAPSHOT-runner
```
- **Pros**: ~0.03s startup time, lower memory usage
- **Cons**: Longer build time, requires GraalVM

### 3. Docker Containers

**JVM Container:**
```bash
docker build -f src/main/docker/Dockerfile.jvm -t qr-log:jvm .
docker run -p 8080:8080 qr-log:jvm
```

**Native Container:**
```bash
docker build -f src/main/docker/Dockerfile.native -t qr-log:native .
docker run -p 8080:8080 qr-log:native
```

**Multi-stage Container:**
```bash
docker build -f src/main/docker/Dockerfile.multi -t qr-log:multi .
```

### 4. Kubernetes Deployment
See `src/main/docker/` for various Dockerfile options suitable for Kubernetes deployments.

## ⚙️ Configuration

### Application Properties
```properties
# CORS Configuration
quarkus.http.cors=true
quarkus.http.cors.origins=/.*/

# Server Configuration  
quarkus.http.host=0.0.0.0
quarkus.http.port=8080

# Logging Configuration
quarkus.log.level=INFO
quarkus.log.category."com.example".level=DEBUG

# Security
quarkus.http.auth.basic=false
```

### Environment Variables
- `QUARKUS_HTTP_PORT`: Server port (default: 8080)
- `QUARKUS_LOG_LEVEL`: Global log level
- `QUARKUS_HTTP_CORS_ORIGINS`: CORS allowed origins

## 🛣️ Roadmap

### Current Features ✅
- QR code generation and scanning
- Real-time WebSocket authentication
- Traditional username/password login
- Security validations and token management
- Multiple deployment options

### Planned Enhancements 🚧
- [ ] Persistent storage (database integration)
- [ ] JWT token implementation
- [ ] OAuth2/OpenID Connect support
- [ ] Rate limiting and DDoS protection
- [ ] Multi-factor authentication (MFA)
- [ ] Password complexity requirements
- [ ] Account lockout policies
- [ ] Audit logging and compliance
- [ ] Mobile app SDK

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow hexagonal architecture principles
- Write unit tests for domain logic
- Update documentation for API changes
- Use conventional commit messages
- Ensure security best practices

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Quarkus](https://quarkus.io/) - Supersonic Subatomic Java framework
- [Nayuki QR Code Generator](https://www.nayuki.io/page/qr-code-generator-library) - High-quality QR code generation
- [Jackson](https://github.com/FasterXML/jackson) - JSON processing
- Domain-Driven Design and Hexagonal Architecture patterns

## 📞 Support

For questions, issues, or contributions:
- 🐛 Report bugs via [GitHub Issues](../../issues)
- 💡 Request features via [GitHub Issues](../../issues)
- 💬 Discuss in [GitHub Discussions](../../discussions)

---

**Made with ❤️ and ☕ - Happy Coding!** 🚀
