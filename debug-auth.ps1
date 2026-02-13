# Debug Authentication Response
$baseUrl = "http://localhost:50982"

$authBody = @{
    username = "testadmin@odop.com"
    password = "admin123456"
} | ConvertTo-Json

Write-Host "Sending authentication request..." -ForegroundColor Cyan
$authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $authBody -ContentType "application/json"

Write-Host "`nFull Response:" -ForegroundColor Yellow
$authResponse | ConvertTo-Json -Depth 5

Write-Host "`nResponse Type:" -ForegroundColor Yellow
$authResponse.GetType().FullName

Write-Host "`nToken property:" -ForegroundColor Yellow
$authResponse.token

Write-Host "`nUser property:" -ForegroundColor Yellow
$authResponse.user | ConvertTo-Json -Depth 2
