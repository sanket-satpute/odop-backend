# API Test Script for Customer Dashboard
$baseUrl = "http://localhost:50982"

# First, create a fresh test customer
Write-Host "=== Creating Test Customer ==="
$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$testEmail = "testcustomer$timestamp@test.com"
$testPassword = "Test@1234"

$createBody = @{
    customerName = "Test Customer Dashboard"
    emailAddress = $testEmail
    password = $testPassword
    contactNumber = [int64](Get-Random -Minimum 9000000000 -Maximum 9999999999)
    address = "Test Address"
    city = "Test City"
    country = "India"
    gender = "Not Specified"
    pin = "123456"
    state = "Test State"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/odop/customer/create_account" -Method POST -Body $createBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "SUCCESS: Test customer created"
    Write-Host "Customer ID: $($createResponse.customerId)"
    Write-Host "Email: $testEmail"
} catch {
    Write-Host "ERROR creating customer: $($_.Exception.Message)"
    Write-Host "Continuing with existing customer..."
    $testEmail = "amit1@gmail.com"
    $testPassword = "TestPassword123"
}

# Now authenticate
Write-Host "`n=== Testing Authentication ==="
$body = @{
    username = $testEmail
    password = $testPassword
    role = "CUSTOMER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop
    Write-Host "SUCCESS: Login successful!"
    
    $token = $response.jwt
    $customerId = $response.user.customerId
    
    Write-Host "Token: $($token.Substring(0, 50))..."
    Write-Host "Customer ID: $customerId"
    
    # Test customer endpoints with token
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    Write-Host "`n=== Testing Customer Profile ==="
    try {
        $profile = Invoke-RestMethod -Uri "$baseUrl/odop/customer/get_customer_id/$customerId" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Profile loaded - $($profile.customerName)"
    } catch {
        Write-Host "ERROR: Profile - $($_.Exception.Message)"
    }
    
    Write-Host "`n=== Testing Customer Addresses ==="
    try {
        $addresses = Invoke-RestMethod -Uri "$baseUrl/odop/customer/$customerId/addresses" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Addresses loaded - Count: $($addresses.Count)"
    } catch {
        Write-Host "ERROR: Addresses - $($_.Exception.Message)"
    }
    
    Write-Host "`n=== Testing Support Tickets ==="
    try {
        $tickets = Invoke-RestMethod -Uri "$baseUrl/odop/customer/$customerId/support/tickets" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Tickets loaded - Count: $($tickets.Count)"
    } catch {
        Write-Host "ERROR: Tickets - $($_.Exception.Message)"
    }
    
    Write-Host "`n=== Testing Wallet ==="
    try {
        $wallet = Invoke-RestMethod -Uri "$baseUrl/odop/customer/$customerId/wallet" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Wallet loaded - Balance: $($wallet.balance)"
    } catch {
        Write-Host "ERROR: Wallet - $($_.Exception.Message)"
    }
    
    Write-Host "`n=== Testing Notifications ==="
    try {
        $notifs = Invoke-RestMethod -Uri "$baseUrl/odop/customer/$customerId/notifications" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Notifications loaded - Count: $($notifs.Count)"
    } catch {
        Write-Host "ERROR: Notifications - $($_.Exception.Message)"
    }
    
    Write-Host "`n=== Testing Preferences ==="
    try {
        $prefs = Invoke-RestMethod -Uri "$baseUrl/odop/customer/$customerId/preferences" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Preferences loaded"
    } catch {
        Write-Host "ERROR: Preferences - $($_.Exception.Message)"
    }
    
} catch {
    Write-Host "FAILED: Authentication error"
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "Message: $($_.Exception.Message)"
}
