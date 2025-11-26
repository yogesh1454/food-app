#!/bin/bash

echo "🔒 Creating SSH tunnels to AWS RDS and Redis..."
echo ""
echo "RDS PostgreSQL: localhost:5432"
echo "Redis Cache: localhost:6379"
echo ""
echo "Keep this terminal open while working."
echo "Press Ctrl+C to close tunnels."
echo ""

ssh -i infrastructure/cloudformation/nastto-key.pem \
    -L 5432:nashtto-postgres.c2z440siod1m.us-east-1.rds.amazonaws.com:5432 \
    -L 6379:nas-re-1oknf2h1dludd.qoapqv.0001.use1.cache.amazonaws.com:6379 \
    ec2-user@54.87.117.181 -N
