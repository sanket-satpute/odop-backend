# ODOP Authentication Test Script
$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:50982"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ODOP Authentication Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Test 1: Create Admin Account
Write-Host "`n[Test 1] Creating Admin Account..." -ForegroundColor Yellow
$adminBody = @{
    name = "Test Admin"
    username = "admin@test.com"
    password = "admin123"
    contactNo = "9876543210"
    address = "Test Admin Address"
    pinCode = "123456"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/odop/admin/create_account" -Method POST -Body $adminBody -ContentType "application/json"
    Write-Host "[SUCCESS] Admin account created" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 500) {
        Write-Host "[INFO] Admin may already exist - continuing..." -ForegroundColor Yellow
    } else {
        Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 1

# Test 2: Create Customer Account
Write-Host "`n[Test 2] Creating Customer Account..." -ForegroundColor Yellow
$customerBody = @{
    name = "Test Customer"
    username = "customer@test.com"
    password = "customer123"
    contactNo = "9123456789"
    address = "Test Customer Address"
    pinCode = "654321"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/odop/customer/create_account" -Method POST -Body $customerBody -ContentType "application/json"
    Write-Host "[SUCCESS] Customer account created" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 500) {
        Write-Host "[INFO] Customer may already exist - continuing..." -ForegroundColor Yellow
    } else {
        Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 1

# Test 3: Authenticate Admin
Write-Host "`n[Test 3] Authenticating Admin..." -ForegroundColor Yellow
$adminAuthBody = @{
    username = "admin@test.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $adminAuthBody -ContentType "application/json"
    $adminToken = $authResponse.token
    Write-Host "[SUCCESS] Admin authenticated" -ForegroundColor Green
    Write-Host "Token: $($adminToken.Substring(0, 20))..." -ForegroundColor Gray
} catch {
    Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 4: Authenticate Customer
Write-Host "`n[Test 4] Authenticating Customer..." -ForegroundColor Yellow
$customerAuthBody = @{
    username = "customer@test.com"
    password = "customer123"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $customerAuthBody -ContentType "application/json"
    $customerToken = $authResponse.token
    Write-Host "[SUCCESS] Customer authenticated" -ForegroundColor Green
    Write-Host "Token: $($customerToken.Substring(0, 20))..." -ForegroundColor Gray
} catch {
    Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 1

# Test 5: Test Invalid Credentials
Write-Host "`n[Test 5] Testing Invalid Credentials..." -ForegroundColor Yellow
$invalidAuthBody = @{
    username = "admin@test.com"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $invalidAuthBody -ContentType "application/json"
    Write-Host "[FAILED] Invalid credentials should have been rejected" -ForegroundColor Red
} catch {
    Write-Host "[SUCCESS] Invalid credentials rejected correctly" -ForegroundColor Green
}

Start-Sleep -Seconds 1

# Test 6: Access Protected Admin Endpoint with Valid Token
Write-Host "`n[Test 6] Accessing Admin Endpoint with Valid Token..." -ForegroundColor Yellow
if ($adminToken) {
    try {
        $headers = @{
            "Authorization" = "Bearer $adminToken"
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/odop/admin/all_admins" -Method GET -Headers $headers
        Write-Host "[SUCCESS] Admin endpoint accessible with valid token" -ForegroundColor Green
    } catch {
        Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "[SKIPPED] No admin token available" -ForegroundColor Yellow
}

Start-Sleep -Seconds 1

# Test 7: Access Customer Endpoint with Valid Token
Write-Host "`n[Test 7] Accessing Customer Endpoint with Valid Token..." -ForegroundColor Yellow
if ($customerToken) {
    try {
        $headers = @{
            "Authorization" = "Bearer $customerToken"
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/odop/customer/all_customers" -Method GET -Headers $headers
        Write-Host "[SUCCESS] Customer endpoint accessible with valid token" -ForegroundColor Green
    } catch {
        Write-Host "[FAILED] $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "[SKIPPED] No customer token available" -ForegroundColor Yellow
}

Start-Sleep -Seconds 1

# Test 8: Test Role-Based Access Control (Customer accessing Admin endpoint)
Write-Host "`n[Test 8] Testing RBAC - Customer accessing Admin endpoint..." -ForegroundColor Yellow
if ($customerToken) {
    try {
        $headers = @{
            "Authorization" = "Bearer $customerToken"
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/odop/admin/all_admins" -Method GET -Headers $headers
        Write-Host "[FAILED] Customer should not access Admin endpoint" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 403) {
            Write-Host "[SUCCESS] Customer correctly denied access to Admin endpoint (403)" -ForegroundColor Green
        } else {
            Write-Host "[INFO] Access denied (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "[SKIPPED] No customer token available" -ForegroundColor Yellow
}

Start-Sleep -Seconds 1

# Test 9: Access Protected Endpoint Without Token
Write-Host "`n[Test 9] Accessing Protected Endpoint Without Token..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/odop/admin/all_admins" -Method GET
    Write-Host "[FAILED] Protected endpoint should require authentication" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401 -or $_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "[SUCCESS] Endpoint correctly requires authentication" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Access denied (Status: $($_.Exception.Response.StatusCode.value__))" -ForegroundColor Yellow
    }
}

# Test Results Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Suite Completed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nNote: Some tests may show INFO messages if accounts already exist" -ForegroundColor Gray
Write-Host "This is expected behavior for subsequent test runs" -ForegroundColor Gray
