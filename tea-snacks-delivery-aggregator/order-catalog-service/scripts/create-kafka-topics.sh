#!/bin/bash

# Script to create Kafka topics for Order & Delivery FSM
# Usage: ./create-kafka-topics.sh [bootstrap-server] [replication-factor]
# Example: ./create-kafka-topics.sh localhost:9092 1

BOOTSTRAP_SERVER=${1:-localhost:9092}
REPLICATION_FACTOR=${2:-1}

echo "Creating Kafka topics on $BOOTSTRAP_SERVER with replication factor $REPLICATION_FACTOR"

# Order events topic
kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic order-events \
  --partitions 6 \
  --replication-factor $REPLICATION_FACTOR \
  --config retention.ms=604800000 \
  --config compression.type=snappy \
  --config min.insync.replicas=1 \
  --if-not-exists

echo "✓ Created order-events topic"

# Delivery events topic
kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic delivery-events \
  --partitions 6 \
  --replication-factor $REPLICATION_FACTOR \
  --config retention.ms=604800000 \
  --config compression.type=snappy \
  --config min.insync.replicas=1 \
  --if-not-exists

echo "✓ Created delivery-events topic"

# Assignment requests topic
kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic assignment-requests \
  --partitions 3 \
  --replication-factor $REPLICATION_FACTOR \
  --config retention.ms=86400000 \
  --config compression.type=snappy \
  --config min.insync.replicas=1 \
  --if-not-exists

echo "✓ Created assignment-requests topic"

# Assignment responses topic
kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic assignment-responses \
  --partitions 3 \
  --replication-factor $REPLICATION_FACTOR \
  --config retention.ms=86400000 \
  --config compression.type=snappy \
  --config min.insync.replicas=1 \
  --if-not-exists

echo "✓ Created assignment-responses topic"

# Dead letter queue topic
kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP_SERVER \
  --topic order-events-dlq \
  --partitions 3 \
  --replication-factor $REPLICATION_FACTOR \
  --config retention.ms=2592000000 \
  --config compression.type=snappy \
  --config min.insync.replicas=1 \
  --if-not-exists

echo "✓ Created order-events-dlq topic"

echo ""
echo "All Kafka topics created successfully!"
echo ""
echo "List topics:"
kafka-topics.sh --list --bootstrap-server $BOOTSTRAP_SERVER
