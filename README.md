# Tea & Snacks Delivery Aggregator

A comprehensive food delivery platform built with Spring Boot microservices architecture, designed for local-first development with Docker containers.

## 🚀 Quick Start

### Prerequisites


- Docker Desktop (v20.10+)
- Docker Compose (v2.0+)
- Java 21 or higher
- Gradle 8.x

### Build and Run

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd food-app
   ```

2. **Build all services**
   ```bash
   cd tea-snacks-delivery-aggregator
   ./gradlew clean build
   cd ..
   ```

3. **Start all services**
   ```bash
   docker-compose -f infrastructure/docker/docker-compose.yml up -d --build
   ```

4. **Check service health**
   ```bash
   ./scripts/health-check.sh
   ```

## 🗄️ Database & Infrastructure Details

### PostgreSQL Database

**Connection Details:**
- **Host**: `localhost` (or `127.0.0.1`)
- **Port**: `5432`
- **Database**: `tea_snacks_db`
- **Username**: `tea_snacks_user`
- **Password**: `tea_snacks_password`

**Connection String:**
```
jdbc:postgresql://localhost:5432/tea_snacks_db
```

**Connect via Command Line:**
```bash
# Using psql directly
psql -h localhost -p 5432 -U tea_snacks_user -d tea_snacks_db

# Using Docker
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db
```

**Available Tables:**
- `users` - User registration and authentication data
- `user_profiles` - Extended user profile information
- `refresh_tokens` - JWT refresh token storage
- `otp_verifications` - Phone OTP verification data
- `flyway_schema_history` - Database migration history

### Redis Cache

**Connection Details:**
- **Host**: `localhost`
- **Port**: `6379`
- **Database**: `0` (default)
- **Password**: None (default)

**Connect via Command Line:**
```bash
# Using redis-cli directly
redis-cli -h localhost -p 6379

# Using Docker
docker exec -it tea-snacks-redis redis-cli
```

### Apache Kafka

**Connection Details:**
- **Host**: `localhost`
- **Port**: `9092` (external), `29092` (internal)
- **Zookeeper**: `localhost:2181`

**Topics:**
- `user.events` - User registration and profile events
- `system.audit` - System audit logs

### Elasticsearch

**Connection Details:**
- **Host**: `localhost`
- **Port**: `9200` (HTTP), `9300` (Transport)
- **Cluster**: `tea-snacks-elasticsearch`

### Service Ports

#### Infrastructure Services
| Service | Port | Health Check URL | Connection Details |
|---------|------|-----------------|-------------------|
| PostgreSQL | 5432 | - | `localhost:5432/tea_snacks_db` |
| Redis | 6379 | - | `localhost:6379` |
| Kafka | 9092, 9101 | - | `localhost:9092` |
| Elasticsearch | 9200, 9300 | http://localhost:9200/_cluster/health | `localhost:9200` |
| Prometheus | 9090 | http://localhost:9090/-/healthy | `localhost:9090` |
| Grafana | 3000 | http://localhost:3000/api/health | `localhost:3000` |
| Kafka UI | 8080 | http://localhost:8080 | `localhost:8080` |

#### Application Services
| Service | Port | Health Check URL | API Documentation |
|---------|------|-----------------|-------------------|
| User Management Service | 8081 | http://localhost:8081/actuator/health | http://localhost:8081/swagger-ui/index.html |
| Order Catalog Service | 8082 | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui/index.html |
| Payment Management Service | 8083 | http://localhost:8083/actuator/health | http://localhost:8083/swagger-ui/index.html |
| Delivery Management Service | 8084 | http://localhost:8084/actuator/health | http://localhost:8084/swagger-ui/index.html |
| Notification Service | 8085 | http://localhost:8085/actuator/health | http://localhost:8085/swagger-ui/index.html |
| Search Discovery Service | 8086 | http://localhost:8086/actuator/health | http://localhost:8086/swagger-ui/index.html |

### Common Operations

#### Build Commands
```bash
# Build all services
cd tea-snacks-delivery-aggregator
./gradlew clean build

# Build a specific service
./gradlew :user-management-service:build
```

#### Docker Commands
```bash
# Start all services
docker-compose -f infrastructure/docker/docker-compose.yml up -d --build

# View logs for all services
docker-compose -f infrastructure/docker/docker-compose.yml logs -f

# View logs for a specific service
docker-compose -f infrastructure/docker/docker-compose.yml logs -f user-management-service

# Stop all services
docker-compose -f infrastructure/docker/docker-compose.yml down

# Stop and remove volumes
docker-compose -f infrastructure/docker/docker-compose.yml down -v
```

#### Health Check Commands
```bash
# Check health of all services
./scripts/health-check.sh

# Check health of a specific service
curl http://localhost:8081/actuator/health  # User Management Service
curl http://localhost:8082/actuator/health  # Order Catalog Service
curl http://localhost:8083/actuator/health  # Payment Management Service
curl http://localhost:8084/actuator/health  # Delivery Management Service
curl http://localhost:8085/actuator/health  # Notification Service
curl http://localhost:8086/actuator/health  # Search Discovery Service
```

#### Database Commands
```bash
# Connect to PostgreSQL
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db

# List all tables
\dt

# View table structure
\d users

# View data
SELECT * FROM users LIMIT 5;

# Connect to Redis
docker exec -it tea-snacks-redis redis-cli

# List Redis keys
KEYS *

# View Redis data
GET key_name
```

## 🏗️ Architecture

### Infrastructure Components

- **PostgreSQL 15**: Primary database with comprehensive schema
- **Redis 7**: Caching and session management
- **Apache Kafka 3**: Event streaming and message queuing
- **Elasticsearch 8**: Search and analytics engine
- **Prometheus**: Metrics collection and monitoring
- **Grafana**: Visualization and alerting

### Microservices Structure

```
tea-snacks-delivery-aggregator/
├── shared/                    # Shared libraries and configurations
├── user-management-service/   # User registration, authentication
├── order-catalog-service/     # Order and catalog management
├── search-discovery-service/  # Search and discovery functionality
├── delivery-management-service/ # Delivery tracking and management
├── payment-management-service/  # Payment processing
└── notification-service/      # Notifications and messaging
```

## 🔍 Troubleshooting

### Common Issues

1. **Port Conflicts**: 
   - Ensure all required ports (5432, 6379, 8080-8086, 9092, 9200, 9090, 3000) are available
   - Use `lsof -i :PORT` to check if a port is in use
   - Stop any conflicting services or change the port mapping in `docker-compose.yml`

2. **Memory Issues**: 
   - Increase Docker memory allocation to at least 4GB
   - Check Docker Desktop settings → Resources → Memory

3. **Service Startup Failures**:
   - Check service logs: `docker-compose -f infrastructure/docker/docker-compose.yml logs -f SERVICE_NAME`
   - Ensure all required environment variables are set
   - Verify infrastructure services (PostgreSQL, Redis, Kafka) are healthy

4. **Health Check Failures**:
   - Run `./scripts/health-check.sh` to identify failing services
   - Check service logs for detailed error messages
   - Verify service dependencies are running and accessible

5. **Database Connection Issues**:
   - Verify PostgreSQL container is running: `docker ps | grep postgres`
   - Check database logs: `docker-compose logs postgres`
   - Test connection: `docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db`

### Logs

```bash
# View all logs
docker-compose -f infrastructure/docker/docker-compose.yml logs -f

# View specific service logs
docker-compose -f infrastructure/docker/docker-compose.yml logs -f postgres
docker-compose -f infrastructure/docker/docker-compose.yml logs -f kafka
docker-compose -f infrastructure/docker/docker-compose.yml logs -f redis
```

## 📚 Documentation

- [Architecture Overview](docs/architecture.md)
- [API Documentation](docs/api/)
- [Database Schema](docs/database-schema.md)
- [Deployment Guide](docs/deployment.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the documentation

---

**Happy coding! 🚀** 
