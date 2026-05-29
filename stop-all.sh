#!/bin/bash
echo "🛑 Stopping Fleet Management System..."
docker-compose down -v
echo "✅ All services stopped and volumes removed"
