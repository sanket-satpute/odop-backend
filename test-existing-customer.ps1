# Test existing customer
$baseUrl = "http://localhost:50982"

Write-Host "Testing authentication with existing customer..." -ForegroundColor Cyan

$authBody = @{
    username = "testcustomer@odop.com"
    password = "customer123456"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $authBody -ContentType "application/json"
    Write-Host "[SUCCESS] Existing customer authentication works!" -ForegroundColor Green
    Write-Host "Token: $($authResponse.jwt.Substring(0, 25))..." -ForegroundColor Gray
    Write-Host "User: $($authResponse.user.fullName)" -ForegroundColor Gray
} catch {
    Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
}
