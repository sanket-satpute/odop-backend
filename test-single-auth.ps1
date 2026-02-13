# Single Authentication Test
$baseUrl = "http://localhost:50982"

Write-Host "Creating fresh admin account..." -ForegroundColor Cyan
$createBody = @{
    fullName = "Fresh Admin"
    emailAddress = "fresh@test.com"
    password = "fresh123"
    contactNumber = 8888877777
    positionAndRole = "Test Admin Role"
    authorizationKey = "test-auth-key-123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/odop/admin/create_account" -Method POST -Body $createBody -ContentType "application/json"
    Write-Host "Account created!" -ForegroundColor Green
} catch {
    Write-Host "Create error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Start-Sleep -Seconds 2

Write-Host "`nAttempting authentication..." -ForegroundColor Cyan
$authBody = @{
    username = "fresh@test.com"
    password = "fresh123"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $authBody -ContentType "application/json"
    Write-Host "SUCCESS! Token received" -ForegroundColor Green
    if ($authResponse.token) {
        $tokenPreview = $authResponse.token.Substring(0, [Math]::Min(30, $authResponse.token.Length))
        Write-Host "Token: $tokenPreview..."
    }
    Write-Host "User Info: $($authResponse.user | ConvertTo-Json -Compress)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
