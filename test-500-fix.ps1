# Test 500 Error Fix
$baseUrl = "http://localhost:50982"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Customer Authentication Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Create a fresh customer account
Write-Host "`n[Step 1] Creating new customer account..." -ForegroundColor Yellow
$createBody = @{
    fullName = "Fix Test Customer"
    emailAddress = "fixtest@customer.com"
    password = "test123456"
    contactNumber = 9999999999
    address = "123 Test Street"
    city = "Mumbai"
    state = "Maharashtra"
    pinCode = "400001"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/odop/customer/create_account" -Method POST -Body $createBody -ContentType "application/json"
    Write-Host "[SUCCESS] Customer account created!" -ForegroundColor Green
    Write-Host "  Customer ID: $($response.customerId)" -ForegroundColor Gray
    Write-Host "  Name: $($response.fullName)" -ForegroundColor Gray
    Write-Host "  Email: $($response.emailAddress)" -ForegroundColor Gray
} catch {
    Write-Host "[ERROR] $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# Step 2: Authenticate with the same credentials
Write-Host "`n[Step 2] Authenticating customer..." -ForegroundColor Yellow
$authBody = @{
    username = "fixtest@customer.com"
    password = "test123456"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $authBody -ContentType "application/json"
    Write-Host "[SUCCESS] Authentication successful!" -ForegroundColor Green
    $tokenPreview = $authResponse.jwt.Substring(0, [Math]::Min(30, $authResponse.jwt.Length))
    Write-Host "  Token: $tokenPreview..." -ForegroundColor Gray
    Write-Host "  User ID: $($authResponse.user.customerId)" -ForegroundColor Gray
    Write-Host "  Name: $($authResponse.user.fullName)" -ForegroundColor Gray
    Write-Host "  Email: $($authResponse.user.emailAddress)" -ForegroundColor Gray
    Write-Host "  City: $($authResponse.user.city)" -ForegroundColor Gray
    Write-Host "  State: $($authResponse.user.state)" -ForegroundColor Gray
    
    # Step 3: Test protected endpoint access
    Write-Host "`n[Step 3] Testing protected endpoint with token..." -ForegroundColor Yellow
    $headers = @{
        "Authorization" = "Bearer $($authResponse.jwt)"
    }
    $customerData = Invoke-RestMethod -Uri "$baseUrl/odop/customer/all_customers" -Method GET -Headers $headers
    Write-Host "[SUCCESS] Protected endpoint accessible!" -ForegroundColor Green
    Write-Host "  Retrieved $($customerData.Count) customer(s)" -ForegroundColor Gray
    
} catch {
    Write-Host "[ERROR] Authentication failed!" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Server Response: $responseBody" -ForegroundColor Yellow
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
