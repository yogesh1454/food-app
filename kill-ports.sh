#!/bin/bash

echo "🔍 Finding processes using ports 5432 and 6379..."
PIDS=$(lsof -ti:5432,6379 2>/dev/null | sort -u)

if [ -z "$PIDS" ]; then
    echo "✅ Ports are already free!"
else
    echo "📋 Processes found:"
    for PID in $PIDS; do
        ps -p $PID -o pid,command
    done
    echo ""
    echo "🔪 Killing processes..."
    for PID in $PIDS; do
        kill $PID
    done
    sleep 1
    echo "✅ Ports freed!"
fi
