# Tea & Snacks Delivery Aggregator

A comprehensive food delivery platform built with Spring Boot microservices architecture, designed for local-first development with Docker containers.

## 🚀 Quick Start

### Prerequisites

- Docker Desktop (v20.10+)
- Docker Compose (v2.0+)
- Java 17 or higher
- Gradle 8.x

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd tea-snacks-delivery-aggregator
   ```

2. **Start the infrastructure**
   ```bash
   docker-compose up -d
   ```

3. **Verify all services are running**
   ```bash
   docker-compose ps
   ```

4. **Check service health**
   ```bash
   # PostgreSQL
   curl http://localhost:5432
   
   # Redis
   redis-cli -h localhost -p 6379 ping
   
   # Kafka
   curl http://localhost:9092
   
   # Elasticsearch
   curl http://localhost:9200
   
   # Prometheus
   curl http://localhost:9090/-/healthy
   
   # Grafana
   curl http://localhost:3000/api/health
   
   # Kafka UI
   curl http://localhost:8080/actuator/health
   ```

## 📊 Service Ports

| Service | Port | Description |
|---------|------|-------------|
| PostgreSQL | 5432 | Primary database |
| Redis | 6379 | Caching layer |
| Kafka | 9092 | Message broker |
| Zookeeper | 2181 | Kafka coordination |
| Elasticsearch | 9200 | Search engine |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Monitoring dashboard |
| Kafka UI | 8080 | Kafka management interface |

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
├── user-management-service/    # User registration, authentication
├── order-catalog-service/     # Order and catalog management
├── search-discovery-service/  # Search and discovery functionality
├── delivery-management-service/ # Delivery tracking and management
├── payment-management-service/  # Payment processing
└── notification-service/      # Notifications and messaging
```

## 🔧 Development

### Starting the Environment

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Database Management

```bash
# Connect to PostgreSQL
docker exec -it tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db

# Backup database
docker exec tea-snacks-postgres pg_dump -U tea_snacks_user tea_snacks_db > backup.sql

# Restore database
docker exec -i tea-snacks-postgres psql -U tea_snacks_user -d tea_snacks_db < backup.sql
```

### Kafka Management

```bash
# List topics
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Create topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic test-topic --partitions 3 --replication-factor 1

# Produce message
docker exec tea-snacks-kafka kafka-console-producer --bootstrap-server localhost:9092 \
  --topic test-topic

# Consume messages
docker exec tea-snacks-kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic test-topic --from-beginning
```

### Redis Management

```bash
# Connect to Redis CLI
docker exec -it tea-snacks-redis redis-cli

# Monitor Redis
docker exec tea-snacks-redis redis-cli monitor
```

### Elasticsearch Management

```bash
# Check cluster health
curl http://localhost:9200/_cluster/health

# List indices
curl http://localhost:9200/_cat/indices

# Create index
curl -X PUT "localhost:9200/test-index"
```

## 📈 Monitoring

### Grafana Dashboards

Access Grafana at http://localhost:3000
- Username: `admin`
- Password: `admin`

### Prometheus Metrics

Access Prometheus at http://localhost:9090

### Kafka UI

Access Kafka UI at http://localhost:8080

## 🧪 Testing

### Integration Tests

```bash
# Run all tests
./gradlew test

# Run specific service tests
./gradlew :user-management-service:test
```

### Environment Validation

```bash
# Validate all services
./scripts/validate-environment.sh
```

## 📚 Documentation

- [Architecture Overview](docs/architecture.md)
- [API Documentation](docs/api/)
- [Database Schema](docs/database-schema.md)
- [Deployment Guide](docs/deployment.md)

## 🔍 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 5432, 6379, 9092, 9200, 9090, 3000, 8080 are available
2. **Memory issues**: Increase Docker memory allocation to at least 4GB
3. **Service startup failures**: Check logs with `docker-compose logs <service-name>`

### Health Checks

```bash
# Check all service health
./scripts/health-check.sh
```

### Logs

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs postgres
docker-compose logs kafka
docker-compose logs redis
```

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
