# API Gateway

A Spring Cloud Gateway project for secure, scalable, and reactive API routing with JWT authentication, rate limiting, and service discovery via Eureka.

## Features
- **Spring Cloud Gateway** for reactive API routing
- **JWT Authentication** for secure endpoints
- **Eureka Service Discovery** for dynamic routing to microservices
- **Circuit Breaker & Resilience** with fallback responses
- **Role-Based Access Control (RBAC)** at gateway level
- **API Versioning** support (path-based and header-based)
- **Rate Limiting** (configurable)
- **Integration Tests** for authentication and protected routes

## Prerequisites
- Java 17+
- Maven 3.8+
- Running Eureka server (for service discovery)
- Running Auth Service (for /api/auth/* endpoints)

## Setup
1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd api-gateway
   ```
2. **Configure Eureka and Auth Service:**
   - Ensure your Eureka server is running (default: `http://localhost:8761/eureka`)
   - Ensure your Auth Service is registered with Eureka as `auth-service`

3. **Configuration:**
   - Edit `src/main/resources/application.properties` for gateway routes, JWT secret, and other settings.
   - Example route config for auth-service:
     ```properties
     spring.cloud.gateway.routes[0].id=auth-service
     spring.cloud.gateway.routes[0].uri=lb://auth-service
     spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
     ```
   - Set your JWT secret in `application.properties`:
     ```properties
     jwt.secret=your-base64-encoded-secret
     ```

## Running the API Gateway
```bash
./mvnw spring-boot:run
```
The gateway will start on the port specified in `application.properties` (default: 8080).

## Endpoints
- **/api/auth/login**: POST, expects `{ "email": "...", "password": "..." }` (password min 6 chars)
- **/api/auth/register**: POST, expects `{ "email": "...", "password": "..." }` (password min 6 chars)
- **/api/auth/protected**: Example protected endpoint (requires valid JWT)

### API Versioning
- **Path-based**: `/v1/api/auth/login`, `/v2/api/auth/login`
- **Header-based**: Add `X-API-Version: v1` header

### Role-Based Access
- **ADMIN**: Access to `/api/admin/**` endpoints
- **USER**: Access to `/api/user/**`, `/api/products/**`, `/api/orders/**` endpoints
- **Both**: Access to `/api/auth/protected` endpoint

## Testing
### Integration Tests
Integration tests are located in `src/test/java/com/example/api_gateway/Integration/ApiGatewayIntegrationTest.java` and cover:
- **Login/Register**: POST requests with email and password (min 6 chars)
- **Validation**: Short password, invalid email, invalid credentials
- **JWT-protected endpoints**: Valid/invalid/expired/malformed tokens
- **Routing**: Ensures requests are routed via Eureka to the auth-service

#### Example Test Case
```java
webTestClient.post()
    .uri("/api/auth/login")
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue("{"email":"test@example.com","password":"password123"}")
    .exchange()
    .expectStatus().isOk();
```

#### Run All Tests
```bash
./mvnw test
```

## Configuration
- **application.properties**: Main configuration for routes, JWT, Eureka, etc.
- **application-test.properties**: Test-specific configuration (enables Eureka, sets up test routes)
