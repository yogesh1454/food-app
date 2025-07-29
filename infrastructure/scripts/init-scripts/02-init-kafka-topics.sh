#!/bin/bash

# Kafka Topic Initialization Script for Tea & Snacks Delivery Aggregator
# This script creates all required Kafka topics for the application

echo "Waiting for Kafka to be ready..."
until docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 --list; do
  echo "Kafka is not ready yet. Waiting..."
  sleep 5
done

echo "Creating Kafka topics..."

# User Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic user-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

# Order Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic order-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

# Vendor Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic vendor-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

# Delivery Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic delivery-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

# Payment Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic payment-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

# Notification Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic notification-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

# Search Events Topic
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic search-events \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=604800000

echo "Listing all topics:"
docker exec tea-snacks-kafka kafka-topics --bootstrap-server localhost:9092 --list

echo "Kafka topics initialization completed!" 