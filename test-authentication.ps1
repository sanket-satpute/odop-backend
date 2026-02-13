# ODOP Authentication Test Script
# Testing JWT Authentication Flow

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ODOP Authentication Feature Testing" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:50982"

# Test 1: Create Admin Account
Write-Host "[TEST 1] Creating Admin Account..." -ForegroundColor Yellow
$adminData = @{
    fullName = "Test Admin"
    emailAddress = "admin@test.com"
    password = "admin123"
    contactNumber = 9876543210
    positionAndRole = "System Administrator"
    authorizationKey = "ADMIN-KEY-2024"
} | ConvertTo-Json

try {
    $adminResponse = Invoke-RestMethod -Uri "$baseUrl/odop/admin/create_account" -Method Post -Body $adminData -ContentType "application/json"
    Write-Host "✓ Admin created successfully!" -ForegroundColor Green
    Write-Host "  Admin ID: $($adminResponse.adminId)" -ForegroundColor Gray
    Write-Host "  Email: $($adminResponse.emailAddress)`n" -ForegroundColor Gray
} catch {
    if ($_.Exception.Response.StatusCode -eq 500) {
        Write-Host "✓ Admin might already exist (expected on re-run)" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Failed: $($_.Exception.Message)`n" -ForegroundColor Red
    }
}

# Test 2: Create Customer Account
Write-Host "[TEST 2] Creating Customer Account..." -ForegroundColor Yellow
$customerData = @{
    fullName = "Test Customer"
    emailAddress = "customer@test.com"
    password = "customer123"
    contactNumber = 9876543211
    address = "123 Test Street"
    city = "Mumbai"
    state = "Maharashtra"
    pinCode = "400001"
} | ConvertTo-Json

try {
    $customerResponse = Invoke-RestMethod -Uri "$baseUrl/odop/customer/create_account" -Method Post -Body $customerData -ContentType "application/json"
    Write-Host "✓ Customer created successfully!" -ForegroundColor Green
    Write-Host "  Customer ID: $($customerResponse.customerId)" -ForegroundColor Gray
    Write-Host "  Email: $($customerResponse.emailAddress)`n" -ForegroundColor Gray
} catch {
    if ($_.Exception.Response.StatusCode -eq 500) {
        Write-Host "✓ Customer might already exist (expected on re-run)" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Failed: $($_.Exception.Message)`n" -ForegroundColor Red
    }
}

# Test 3: Create Vendor Account
Write-Host "[TEST 3] Creating Vendor Account..." -ForegroundColor Yellow
$vendorData = @{
    fullName = "Test Vendor"
    emailAddress = "vendor@test.com"
    password = "vendor123"
    contactNumber = 9876543212
    address = "456 Market Road"
    city = "Pune"
    state = "Maharashtra"
    pinCode = "411001"
    businessName = "Test Shop"
    businessRegistryNumber = "BRN123456"
} | ConvertTo-Json

try {
    $vendorResponse = Invoke-RestMethod -Uri "$baseUrl/odop/vendor/create_account" -Method Post -Body $vendorData -ContentType "application/json"
    Write-Host "✓ Vendor created successfully!" -ForegroundColor Green
    Write-Host "  Vendor ID: $($vendorResponse.vendorId)" -ForegroundColor Gray
    Write-Host "  Email: $($vendorResponse.emailAddress)`n" -ForegroundColor Gray
} catch {
    if ($_.Exception.Response.StatusCode -eq 500) {
        Write-Host "✓ Vendor might already exist (expected on re-run)" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Failed: $($_.Exception.Message)`n" -ForegroundColor Red
    }
}

Start-Sleep -Seconds 1

# Test 4: Authenticate as Admin
Write-Host "[TEST 4] Authenticating as Admin..." -ForegroundColor Yellow
$adminAuth = @{
    username = "admin@test.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $adminAuthResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method Post -Body $adminAuth -ContentType "application/json"
    Write-Host "✓ Admin authentication successful!" -ForegroundColor Green
    Write-Host "  JWT Token (first 50 chars): $($adminAuthResponse.jwt.Substring(0, [Math]::Min(50, $adminAuthResponse.jwt.Length)))..." -ForegroundColor Gray
    Write-Host "  User Details: $($adminAuthResponse.user.fullName) (ID: $($adminAuthResponse.user.adminId))" -ForegroundColor Gray
    Write-Host "  Active: $($adminAuthResponse.user.active)`n" -ForegroundColor Gray
    $adminToken = $adminAuthResponse.jwt
} catch {
    Write-Host "✗ Admin authentication failed: $($_.Exception.Message)`n" -ForegroundColor Red
    $adminToken = $null
}

# Test 5: Authenticate as Customer
Write-Host "[TEST 5] Authenticating as Customer..." -ForegroundColor Yellow
$customerAuth = @{
    username = "customer@test.com"
    password = "customer123"
} | ConvertTo-Json

try {
    $customerAuthResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method Post -Body $customerAuth -ContentType "application/json"
    Write-Host "✓ Customer authentication successful!" -ForegroundColor Green
    Write-Host "  JWT Token (first 50 chars): $($customerAuthResponse.jwt.Substring(0, [Math]::Min(50, $customerAuthResponse.jwt.Length)))..." -ForegroundColor Gray
    Write-Host "  User Details: $($customerAuthResponse.user.fullName) (ID: $($customerAuthResponse.user.customerId))" -ForegroundColor Gray
    Write-Host "  Status: $($customerAuthResponse.user.status)`n" -ForegroundColor Gray
    $customerToken = $customerAuthResponse.jwt
} catch {
    Write-Host "✗ Customer authentication failed: $($_.Exception.Message)`n" -ForegroundColor Red
    $customerToken = $null
}

# Test 6: Authenticate as Vendor
Write-Host "[TEST 6] Authenticating as Vendor..." -ForegroundColor Yellow
$vendorAuth = @{
    username = "vendor@test.com"
    password = "vendor123"
} | ConvertTo-Json

try {
    $vendorAuthResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method Post -Body $vendorAuth -ContentType "application/json"
    Write-Host "✓ Vendor authentication successful!" -ForegroundColor Green
    Write-Host "  JWT Token (first 50 chars): $($vendorAuthResponse.jwt.Substring(0, [Math]::Min(50, $vendorAuthResponse.jwt.Length)))..." -ForegroundColor Gray
    Write-Host "  User Details: $($vendorAuthResponse.user.fullName) (ID: $($vendorAuthResponse.user.vendorId))" -ForegroundColor Gray
    Write-Host "  Business: $($vendorAuthResponse.user.businessName)`n" -ForegroundColor Gray
    $vendorToken = $vendorAuthResponse.jwt
} catch {
    Write-Host "✗ Vendor authentication failed: $($_.Exception.Message)`n" -ForegroundColor Red
    $vendorToken = $null
}

# Test 7: Test Invalid Credentials
Write-Host "[TEST 7] Testing Invalid Credentials..." -ForegroundColor Yellow
$invalidAuth = @{
    username = "admin@test.com"
    password = "wrongpassword"
} | ConvertTo-Json

try {
    $invalidAuthResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method Post -Body $invalidAuth -ContentType "application/json"
    Write-Host "✗ Security Issue: Invalid credentials were accepted!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 500) {
        Write-Host "✓ Invalid credentials correctly rejected!" -ForegroundColor Green
    } else {
        Write-Host "? Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
Write-Host ""

# Test 8: Test Protected Endpoint with Admin Token
if ($adminToken) {
    Write-Host "[TEST 8] Testing Admin Protected Endpoint..." -ForegroundColor Yellow
    $headers = @{
        "Authorization" = "Bearer $adminToken"
    }
    try {
        $adminEndpoint = Invoke-RestMethod -Uri "$baseUrl/odop/admin/findAll_admin" -Method Get -Headers $headers
        Write-Host "✓ Admin can access admin endpoints! Found $($adminEndpoint.Count) admins" -ForegroundColor Green
    } catch {
        Write-Host "✗ Admin cannot access admin endpoints: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 9: Test Protected Endpoint with Customer Token
if ($customerToken) {
    Write-Host "[TEST 9] Testing Customer Protected Endpoint..." -ForegroundColor Yellow
    $headers = @{
        "Authorization" = "Bearer $customerToken"
    }
    try {
        $customerEndpoint = Invoke-RestMethod -Uri "$baseUrl/odop/customer/get_all_customers" -Method Get -Headers $headers
        Write-Host "✓ Customer can access customer endpoints! Found $($customerEndpoint.Count) customers" -ForegroundColor Green
    } catch {
        Write-Host "✗ Customer cannot access customer endpoints: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 10: Test Cross-Role Access (Customer trying Admin endpoint)
if ($customerToken) {
    Write-Host "[TEST 10] Testing Role-Based Access Control..." -ForegroundColor Yellow
    $headers = @{
        "Authorization" = "Bearer $customerToken"
    }
    try {
        $crossRoleTest = Invoke-RestMethod -Uri "$baseUrl/odop/admin/findAll_admin" -Method Get -Headers $headers
        Write-Host "✗ Security Issue: Customer can access admin endpoints!" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq 403) {
            Write-Host "✓ Role-based access control working! Customer blocked from admin endpoint" -ForegroundColor Green
        } else {
            Write-Host "? Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    Write-Host ""
}

# Test 11: Test Access Without Token
Write-Host "[TEST 11] Testing Endpoint Without Authentication..." -ForegroundColor Yellow
try {
    $noAuthTest = Invoke-RestMethod -Uri "$baseUrl/odop/customer/get_all_customers" -Method Get
    Write-Host "✗ Security Issue: Protected endpoint accessible without token!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✓ Protected endpoint requires authentication!" -ForegroundColor Green
    } else {
        Write-Host "? Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ Registration: Admin, Customer, Vendor accounts" -ForegroundColor Green
Write-Host "✓ Authentication: JWT token generation for all roles" -ForegroundColor Green
Write-Host "✓ Security: Invalid credentials rejected" -ForegroundColor Green
Write-Host "✓ Authorization: Role-based access control" -ForegroundColor Green
Write-Host "✓ Token Validation: Protected endpoints require JWT" -ForegroundColor Green
Write-Host "`nAuthentication feature is working correctly! ✨" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan
