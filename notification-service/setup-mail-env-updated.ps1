# Setup Mail Environment Variables for Notification Service
# This script loads environment variables from .env file and sets them for the current session

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $scriptPath ".env"

if (Test-Path $envFile) {
	Write-Host "Loading environment variables from .env file..." -ForegroundColor Cyan

	Get-Content $envFile | Where-Object { $_ -match '^\s*[^#]' } | ForEach-Object {
		$line = $_.Trim()
		if ($line -and -not $line.StartsWith('#')) {
			$parts = $line -split '=', 2
			if ($parts.Length -eq 2) {
				$key = $parts[0].Trim()
				$value = $parts[1].Trim()

				if ($key -and $value) {
					# Use Set-Item to set environment variables in the current session
					Set-Item -Path "env:$key" -Value $value
					if ($key -like '*PASSWORD*') {
						Write-Host "  ✓ $key = ****" -ForegroundColor Green
					} else {
						Write-Host "  ✓ $key = $value" -ForegroundColor Green
					}
				}
			}
		}
	}
} else {
	Write-Host "❌ .env file not found at: $envFile" -ForegroundColor Red
	Write-Host "Please create .env file with mail configuration (copy .env.example -> .env)" -ForegroundColor Yellow
	exit 1
}

Write-Host ""
Write-Host "Environment variables loaded successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Next, run: mvn clean spring-boot:run" -ForegroundColor Yellow

