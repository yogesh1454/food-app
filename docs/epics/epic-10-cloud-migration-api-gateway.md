# Epic 10: Cloud Migration & API Gateway

**Epic ID:** BE-010  
**Priority:** Medium (P2) - Later in Development Cycle  
**Business Value:** Migrates local development setup to production-ready cloud infrastructure  
**Estimated Effort:** 3-4 sprints  
**Dependencies:** All core services completed and tested locally (Epics 1-9)  

## Description
Migrate the locally developed Tea & Snacks Delivery Aggregator platform to AWS cloud infrastructure and implement API Gateway for production-ready deployment. This epic focuses on cost-effective scaling and production readiness.

## Business Justification
Cloud migration becomes essential for production deployment:
- **Scalability**: Handle production traffic loads beyond local capabilities
- **Reliability**: High availability and disaster recovery
- **Security**: Enterprise-grade security features
- **Performance**: Global CDN and optimized infrastructure
- **Operational Efficiency**: Managed services reduce maintenance overhead
- **Cost Optimization**: Pay-as-you-scale model for growing business

## Key Components
- **AWS Infrastructure Migration**: Move from local Docker to AWS managed services
- **API Gateway Implementation**: Unified entry point for all client applications
- **Database Migration**: PostgreSQL local → AWS RDS with minimal downtime
- **Kafka Migration**: Local Kafka → AWS MSK with data preservation
- **Redis Migration**: Local Redis → AWS ElastiCache with cache warming
- **Elasticsearch Migration**: Local Elasticsearch → AWS OpenSearch
- **Monitoring Migration**: Local Prometheus/Grafana → AWS CloudWatch + custom dashboards
- **CI/CD Enhancement**: Update pipelines for cloud deployment
- **Security Hardening**: Production-grade security configurations

## Migration Strategy
### Phase 1: Infrastructure Setup
- Provision AWS resources via Terraform
- Set up VPC, security groups, and networking
- Configure managed services (RDS, MSK, ElastiCache, OpenSearch)

### Phase 2: Data Migration
- Database migration with minimal downtime
- Kafka topic and message migration
- Cache warming strategies
- Search index migration

### Phase 3: Application Deployment
- Deploy services to AWS EKS/ECS
- Configure API Gateway routing
- Update service configurations for cloud

### Phase 4: Cutover & Validation
- DNS cutover to cloud infrastructure
- Performance and load testing
- Monitoring and alerting validation

## Acceptance Criteria
- [ ] All AWS infrastructure is provisioned via Terraform
- [ ] API Gateway routes all requests correctly to backend services
- [ ] Database migration completes with zero data loss
- [ ] Kafka migration preserves all message history
- [ ] Redis cache is warmed and performing optimally
- [ ] Elasticsearch indices are migrated successfully
- [ ] All services are deployed and operational in cloud
- [ ] Monitoring and alerting work correctly in cloud environment
- [ ] Performance meets or exceeds local development benchmarks
- [ ] Security configurations pass production readiness review
- [ ] Cost optimization measures are implemented
- [ ] Rollback procedures are tested and documented
- [ ] Auto-scaling is configured and tested

## Technical Requirements
- **Infrastructure**: AWS (RDS, MSK, ElastiCache, OpenSearch, EKS, API Gateway)
- **Migration Tools**: AWS DMS for database, custom scripts for Kafka
- **Monitoring**: AWS CloudWatch, custom Grafana dashboards
- **Security**: AWS IAM, VPC, security groups, WAF
- **Automation**: Terraform for infrastructure, updated CI/CD pipelines

## API Gateway Configuration
- **Base URL**: `https://api.teasansnacks.com/v1`
- **Authentication**: Bearer token validation for protected routes
- **Rate Limits**: 
  - Authenticated users: 1000 requests/hour
  - Anonymous users: 100 requests/hour
  - Burst limit: 50 requests/second
- **CORS**: Allow origins from approved frontend domains
- **Timeout**: 30 seconds for backend service calls

## Cost Optimization Strategies
- **Right-sizing**: Start with smaller instances, scale as needed
- **Reserved Instances**: For predictable workloads
- **Spot Instances**: For non-critical batch processing
- **Auto-scaling**: Scale down during low traffic periods
- **Monitoring**: Track costs and optimize continuously

## Infrastructure Requirements

### Infrastructure Migration Strategy (Local to Cloud)
- **MIGRATE**: PostgreSQL → AWS RDS (PostgreSQL)
- **MIGRATE**: Redis → AWS ElastiCache (Redis)
- **MIGRATE**: Kafka → AWS MSK (Managed Streaming for Kafka)
- **MIGRATE**: Elasticsearch → AWS OpenSearch
- **MIGRATE**: All Microservices → AWS EKS (Kubernetes)
- **ADD**: AWS API Gateway (unified API endpoint)
- **ADD**: AWS Application Load Balancer
- **ADD**: AWS CloudWatch (monitoring replacement for Prometheus/Grafana)

### Pre-Migration Infrastructure Commands
```bash
# Ensure all services are production-ready locally
docker-compose up -d

# Run full integration tests
./scripts/run-integration-tests.sh

# Backup all data before migration
./scripts/backup-all-data.sh
```

### Cloud Infrastructure Provisioning
- **Terraform Scripts**: Infrastructure as Code for all AWS resources
- **Kubernetes Manifests**: Service deployments and configurations
- **Helm Charts**: Package management for microservices
- **CI/CD Pipelines**: Automated deployment to cloud

### Migration Phases
1. **Phase 1**: Infrastructure provisioning (RDS, ElastiCache, MSK, OpenSearch)
2. **Phase 2**: Data migration (PostgreSQL, Redis, Kafka, Elasticsearch)
3. **Phase 3**: Service deployment (all microservices to EKS)
4. **Phase 4**: API Gateway configuration and DNS cutover
5. **Phase 5**: Monitoring setup and performance validation

### Dependencies on Previous Epic Infrastructure
- **Epic 1-9**: All local infrastructure must be stable and tested
- **Epic 9**: Monitoring and performance baselines established
- **Production Data**: All services must have production-ready data schemas
- **Security**: All security configurations must be cloud-ready

### Cloud Resource Scaling
- **Auto-scaling Groups**: Dynamic scaling based on load
- **Database Read Replicas**: For improved read performance
- **Multi-AZ Deployment**: For high availability
- **CDN Integration**: For static content delivery

### Cost Optimization
- **Reserved Instances**: For predictable workloads
- **Spot Instances**: For non-critical batch processing
- **Auto-scaling Policies**: Scale down during low traffic
- **Resource Tagging**: For cost tracking and optimization

## Success Metrics
- Migration completed with < 1 hour downtime
- Cloud performance matches or exceeds local benchmarks
- Monthly cloud costs within approved budget
- 99.9% uptime post-migration
- Zero data loss during migration
