# Windows PowerShell End-to-End API Test Script for URL Shortener

$baseUrl = "http://localhost:8081"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "URL Shortener API Verification Suite" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Health Check
Write-Host "`n1. Testing Actuator Health Endpoint..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    Write-Host "   Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "   [!] Backend is not running at $baseUrl. Start the app first using 'mvn spring-boot:run'." -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Cyan
    exit
}

# 2. Register User
Write-Host "`n2. Testing User Registration..." -ForegroundColor Yellow
$regBody = @{
    email = "engineer@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/register" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "   Registered User: $($regResponse.email) (Role: $($regResponse.role))" -ForegroundColor Green
    $token = $regResponse.token
} catch {
    Write-Host "   User registration check completed." -ForegroundColor Yellow
}

# 3. Login User
Write-Host "`n3. Testing User Login..." -ForegroundColor Yellow
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/auth/login" -Method Post -Body $regBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "   JWT Token Obtained: $($token.Substring(0, 25))..." -ForegroundColor Green
} catch {
    Write-Host "   Login failed." -ForegroundColor Red
}

# 4. Shorten a URL
Write-Host "`n4. Testing URL Shortening (POST /api/v1/urls)..." -ForegroundColor Yellow
$shortenBody = @{
    longUrl = "https://donnemartin.com/system-design-primer"
    customAlias = "sys-design-$((Get-Random -Minimum 100 -Maximum 999))"
} | ConvertTo-Json

$headers = @{
    Authorization = "Bearer $token"
}

try {
    $shortResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1/urls" -Method Post -Body $shortenBody -ContentType "application/json" -Headers $headers
    Write-Host "   Short Code Created: $($shortResponse.shortCode)" -ForegroundColor Green
    Write-Host "   Short URL: $($shortResponse.shortUrl)" -ForegroundColor Green
    $code = $shortResponse.shortCode
} catch {
    Write-Host "   URL Shortening failed." -ForegroundColor Red
}

# 5. Test Redirection (HTTP 302)
Write-Host "`n5. Testing Redirect Endpoint (GET /$code)..." -ForegroundColor Yellow
try {
    $req = [System.Net.WebRequest]::Create("$baseUrl/$code")
    $req.AllowAutoRedirect = $false
    $resp = $req.GetResponse()
    Write-Host "   HTTP Status Code: $([int]$resp.StatusCode) ($($resp.StatusCode))" -ForegroundColor Green
    Write-Host "   Redirect Location: $($resp.Headers['Location'])" -ForegroundColor Green
} catch [System.Net.WebException] {
    $resp = $_.Exception.Response
    if ($resp.StatusCode -eq [System.Net.HttpStatusCode]::Found) {
        Write-Host "   HTTP Status Code: 302 Found" -ForegroundColor Green
        Write-Host "   Redirect Location: $($resp.Headers['Location'])" -ForegroundColor Green
    } else {
        Write-Host "   Redirect failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 6. Test Analytics Endpoint
Write-Host "`n6. Testing Link Analytics (GET /api/v1/urls/$code/analytics)..." -ForegroundColor Yellow
try {
    $analytics = Invoke-RestMethod -Uri "$baseUrl/api/v1/urls/$code/analytics" -Method Get -Headers $headers
    Write-Host "   Total Clicks Registered: $($analytics.totalClicks)" -ForegroundColor Green
    Write-Host "   Top Devices: $($analytics.deviceBreakdown | ConvertTo-Json)" -ForegroundColor Green
} catch {
    Write-Host "   Analytics lookup failed." -ForegroundColor Red
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "API Verification Complete!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
