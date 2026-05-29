# Fleet Management - Windows Stop Script
Write-Host "🛑 Stopping Fleet Management System..." -ForegroundColor Yellow
docker-compose down
Write-Host "✅ All services stopped" -ForegroundColor Green
