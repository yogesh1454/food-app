# User Management Service (UMS)

This service handles user-related operations including:
- User registration and authentication
- Profile management
- Address and contact information
- User preferences and settings
- Account security and verification
- Role and permission management

## 📚 Documentation

- **[Domain Relationships](./DOMAIN_RELATIONSHIPS.md)** - Complete DDD structure and domain interactions
- **[User Flows](../USER_FLOWS.md)** - Complete user flows documentation and testing guide
- **[Architecture Overview](../docs/architecture/)** - System architecture and design decisions
- **[API Documentation](http://localhost:8080/swagger-ui/index.html)** - Interactive API documentation (when running)

## 🏗️ Domain Structure

The service follows Domain-Driven Design (DDD) principles with the following domains:

```
user/
├── config/                    # Global configurations
├── auth/                      # Authentication & Authorization
├── profile/                   # User Management (including registration)
├── guest/                     # Guest User Management
└── password/                  # Password Management
```

## 🚀 Getting Started

### Prerequisites
- Java 21
- PostgreSQL 15+
- Gradle 8.14+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd tea-snacks-delivery-aggregator
   ```

2. **Start PostgreSQL**
   ```bash
   # Using Docker
   docker run --name tea-snacks-postgres \
     -e POSTGRES_DB=tea_snacks_db \
     -e POSTGRES_USER=tea_user \
     -e POSTGRES_PASSWORD=tea_password \
     -p 5432:5432 \
     -d postgres:15
   ```

3. **Build the service**
   ```bash
   ./gradlew :user-management-service:build
   ```

4. **Run the service**
   ```bash
   ./gradlew :user-management-service:bootRun
   ```

5. **Verify the service**
   ```bash
   curl http://localhost:8080/api/auth/health
   ```

## 🔗 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Token refresh
- `GET /api/auth/health` - Auth service health

### User Management
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/phone/{phoneNumber}` - Get user by phone
- `GET /api/users/active` - Get all active users
- `PUT /api/users/{userId}/status` - Update user status
- `GET /api/users/health` - User service health

### Registration (OTP)
- `POST /api/v1/auth/phone/send-otp` - Send OTP
- `POST /api/v1/auth/phone/verify-otp` - Verify OTP
- `POST /api/v1/auth/phone/resend-otp` - Resend OTP
- `GET /api/v1/auth/phone/health` - OTP service health

### Guest User Management
- `POST /api/v1/auth/guest/create` - Create guest user
- `GET /api/v1/auth/guest/session` - Get guest session
- `POST /api/v1/auth/guest/action` - Record guest action
- `POST /api/v1/auth/guest/conversion-prompt-shown` - Record conversion prompt
- `GET /api/v1/auth/guest/health` - Guest service health

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew :user-management-service:test
```

### Run Integration Tests
```bash
./gradlew :user-management-service:integrationTest
```

## 🔧 Configuration

### Environment Variables
- `SPRING_PROFILES_ACTIVE` - Active profile (default: local)
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: tea_snacks_db)
- `DB_USER` - Database user (default: tea_user)
- `DB_PASSWORD` - Database password (default: tea_password)

### Application Properties
Configuration files are located in `src/main/resources/`:
- `application.yml` - Main configuration
- `application-local.yml` - Local development settings
- `application-docker.yml` - Docker deployment settings

## 📊 Monitoring

### Health Checks
- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics

### Logging
- Log level: INFO (default)
- Log format: JSON (structured logging)
- Log output: Console and file

## 🚨 Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   ```bash
   # Check what's using the port
   lsof -i :8080
   # Kill the process or change port in application.yml
   ```

2. **Database connection failed**
   ```bash
   # Verify PostgreSQL is running
   pg_isready -h localhost -p 5432
   # Check database credentials
   psql -h localhost -U tea_user -d tea_snacks_db
   ```

3. **Build failures**
   ```bash
   # Clean and rebuild
   ./gradlew clean :user-management-service:build
   ```

## 📈 Development

### Adding New Features
1. Follow DDD principles
2. Add tests for new functionality
3. Update documentation
4. Follow coding standards

### Code Standards
- Use Lombok for boilerplate reduction
- Follow Spring Boot best practices
- Implement proper error handling
- Add comprehensive logging

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Add tests
4. Update documentation
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
