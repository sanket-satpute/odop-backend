# Comprehensive Vendor Dashboard API Test Script
$baseUrl = "http://localhost:50982"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VENDOR DASHBOARD API TEST SUITE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# First, create a fresh test vendor
Write-Host "`n=== Creating Test Vendor ===" -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$testEmail = "testvendor$timestamp@test.com"
$testPassword = "Test@1234"

$createBody = @{
    shoppeeName = "Test Vendor Shop $timestamp"
    emailAddress = $testEmail
    password = $testPassword
    contactNumber = [int64](Get-Random -Minimum 9000000000 -Maximum 9999999999)
    address = "Test Vendor Address"
    city = "Test City"
    country = "India"
    gender = "Not Specified"
    pin = "123456"
    state = "Test State"
    district = "Test District"
    gstin = "22AAAAA0000A1Z5"
    description = "Test vendor for dashboard testing"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/odop/vendor/create_account" -Method POST -Body $createBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "SUCCESS: Test vendor created" -ForegroundColor Green
    Write-Host "Vendor ID: $($createResponse.vendorId)"
    Write-Host "Email: $testEmail"
    $vendorId = $createResponse.vendorId
} catch {
    Write-Host "ERROR creating vendor: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Trying to use existing vendor..."
    $testEmail = "testvendor@test.com"
    $testPassword = "Test@1234"
}

# Now authenticate as vendor
Write-Host "`n=== Testing Vendor Authentication ===" -ForegroundColor Yellow
$authBody = @{
    username = $testEmail
    password = $testPassword
    role = "VENDOR"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $authBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "SUCCESS: Vendor login successful!" -ForegroundColor Green
    
    $token = $authResponse.jwt
    $vendorId = $authResponse.user.vendorId
    
    Write-Host "Token: $($token.Substring(0, 50))..."
    Write-Host "Vendor ID: $vendorId"
    
    # Setup headers for authenticated requests
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    # Initialize test results
    $testResults = @()
    
    # ===========================================
    # TEST 1: Vendor Profile
    # ===========================================
    Write-Host "`n=== Testing Vendor Profile ===" -ForegroundColor Yellow
    try {
        $profile = Invoke-RestMethod -Uri "$baseUrl/odop/vendor/get_vendor_id/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Profile loaded - $($profile.shoppeeName)" -ForegroundColor Green
        $testResults += @{Test="Vendor Profile"; Status="PASS"; Details=$profile.shoppeeName}
    } catch {
        Write-Host "ERROR: Profile - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Profile"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 2: Vendor Products (Paginated)
    # ===========================================
    Write-Host "`n=== Testing Vendor Products ===" -ForegroundColor Yellow
    try {
        $products = Invoke-RestMethod -Uri "$baseUrl/odop/product/vendor/$vendorId/paginated?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
        $productCount = if ($products.content) { $products.content.Count } else { 0 }
        Write-Host "SUCCESS: Products loaded - Count: $productCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Products"; Status="PASS"; Details="Count: $productCount"}
    } catch {
        Write-Host "ERROR: Products - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Products"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 3: Vendor Orders
    # ===========================================
    Write-Host "`n=== Testing Vendor Orders ===" -ForegroundColor Yellow
    try {
        $orders = Invoke-RestMethod -Uri "$baseUrl/odop/order/vendor/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        $orderCount = if ($orders) { $orders.Count } else { 0 }
        Write-Host "SUCCESS: Orders loaded - Count: $orderCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Orders"; Status="PASS"; Details="Count: $orderCount"}
    } catch {
        Write-Host "ERROR: Orders - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Orders"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 4: Vendor Orders (Paginated)
    # ===========================================
    Write-Host "`n=== Testing Vendor Orders (Paginated) ===" -ForegroundColor Yellow
    try {
        $ordersPaged = Invoke-RestMethod -Uri "$baseUrl/odop/order/vendor/$vendorId/paginated?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
        $orderPagedCount = if ($ordersPaged.content) { $ordersPaged.content.Count } else { 0 }
        Write-Host "SUCCESS: Paginated Orders loaded - Count: $orderPagedCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Orders Paginated"; Status="PASS"; Details="Count: $orderPagedCount"}
    } catch {
        Write-Host "ERROR: Paginated Orders - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Orders Paginated"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 5: Vendor Analytics Summary
    # ===========================================
    Write-Host "`n=== Testing Vendor Analytics ===" -ForegroundColor Yellow
    try {
        $analytics = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/vendor/$vendorId/summary" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Analytics loaded" -ForegroundColor Green
        Write-Host "  Total Sales: $($analytics.totalSales)"
        Write-Host "  Total Orders: $($analytics.totalOrders)"
        $testResults += @{Test="Vendor Analytics"; Status="PASS"; Details="Sales: $($analytics.totalSales)"}
    } catch {
        Write-Host "ERROR: Analytics - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Analytics"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 6: Vendor Sales Data
    # ===========================================
    Write-Host "`n=== Testing Vendor Sales Data ===" -ForegroundColor Yellow
    try {
        $sales = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/vendor/$vendorId/sales?period=weekly" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Sales data loaded" -ForegroundColor Green
        $testResults += @{Test="Vendor Sales Data"; Status="PASS"; Details="Sales endpoint working"}
    } catch {
        Write-Host "ERROR: Sales - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Sales Data"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 7: Vendor Earnings Overview
    # ===========================================
    Write-Host "`n=== Testing Vendor Earnings ===" -ForegroundColor Yellow
    try {
        $earnings = Invoke-RestMethod -Uri "$baseUrl/odop/earnings/vendor/$vendorId/overview" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Earnings loaded" -ForegroundColor Green
        Write-Host "  Available Balance: $($earnings.availableBalance)"
        Write-Host "  Pending Earnings: $($earnings.pendingEarnings)"
        $testResults += @{Test="Vendor Earnings"; Status="PASS"; Details="Balance: $($earnings.availableBalance)"}
    } catch {
        Write-Host "ERROR: Earnings - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Earnings"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 8: Vendor Transactions
    # ===========================================
    Write-Host "`n=== Testing Vendor Transactions ===" -ForegroundColor Yellow
    try {
        $transactions = Invoke-RestMethod -Uri "$baseUrl/odop/earnings/vendor/$vendorId/transactions?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
        $txCount = if ($transactions.content) { $transactions.content.Count } else { 0 }
        Write-Host "SUCCESS: Transactions loaded - Count: $txCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Transactions"; Status="PASS"; Details="Count: $txCount"}
    } catch {
        Write-Host "ERROR: Transactions - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Transactions"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 9: Vendor Earnings Chart Data
    # ===========================================
    Write-Host "`n=== Testing Vendor Earnings Chart ===" -ForegroundColor Yellow
    try {
        $chartData = Invoke-RestMethod -Uri "$baseUrl/odop/earnings/vendor/$vendorId/chart?period=monthly" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Chart data loaded" -ForegroundColor Green
        $testResults += @{Test="Vendor Earnings Chart"; Status="PASS"; Details="Chart data available"}
    } catch {
        Write-Host "ERROR: Chart - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Earnings Chart"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 10: Vendor Reviews
    # ===========================================
    Write-Host "`n=== Testing Vendor Reviews ===" -ForegroundColor Yellow
    try {
        $reviews = Invoke-RestMethod -Uri "$baseUrl/odop/review/vendor/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        $reviewCount = if ($reviews) { $reviews.Count } else { 0 }
        Write-Host "SUCCESS: Reviews loaded - Count: $reviewCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Reviews"; Status="PASS"; Details="Count: $reviewCount"}
    } catch {
        Write-Host "ERROR: Reviews - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Reviews"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 11: Vendor Rating
    # ===========================================
    Write-Host "`n=== Testing Vendor Rating ===" -ForegroundColor Yellow
    try {
        $rating = Invoke-RestMethod -Uri "$baseUrl/odop/review/vendor/$vendorId/rating" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Rating loaded - $($rating.averageRating)" -ForegroundColor Green
        $testResults += @{Test="Vendor Rating"; Status="PASS"; Details="Rating: $($rating.averageRating)"}
    } catch {
        Write-Host "ERROR: Rating - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Rating"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 12: Vendor Certifications
    # ===========================================
    Write-Host "`n=== Testing Vendor Certifications ===" -ForegroundColor Yellow
    try {
        $certs = Invoke-RestMethod -Uri "$baseUrl/odop/certifications/vendor/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        $certCount = if ($certs) { $certs.Count } else { 0 }
        Write-Host "SUCCESS: Certifications loaded - Count: $certCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Certifications"; Status="PASS"; Details="Count: $certCount"}
    } catch {
        Write-Host "ERROR: Certifications - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Certifications"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 13: Certification Types (Public)
    # ===========================================
    Write-Host "`n=== Testing Certification Types ===" -ForegroundColor Yellow
    try {
        $certTypes = Invoke-RestMethod -Uri "$baseUrl/odop/certifications/types" -Method GET -ErrorAction Stop
        $typeCount = if ($certTypes) { $certTypes.Count } else { 0 }
        Write-Host "SUCCESS: Certification types loaded - Count: $typeCount" -ForegroundColor Green
        $testResults += @{Test="Certification Types"; Status="PASS"; Details="Count: $typeCount"}
    } catch {
        Write-Host "ERROR: Cert Types - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Certification Types"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 14: Vendor Returns
    # ===========================================
    Write-Host "`n=== Testing Vendor Returns ===" -ForegroundColor Yellow
    try {
        # Returns endpoint uses Authentication to get vendorId
        $returns = Invoke-RestMethod -Uri "$baseUrl/odop/returns/vendor" -Method GET -Headers $headers -ErrorAction Stop
        $returnCount = if ($returns.returns) { $returns.returns.Count } else { 0 }
        Write-Host "SUCCESS: Returns loaded - Count: $returnCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Returns"; Status="PASS"; Details="Count: $returnCount"}
    } catch {
        Write-Host "ERROR: Returns - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Returns"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 15: Vendor Shipping
    # ===========================================
    Write-Host "`n=== Testing Vendor Shipping ===" -ForegroundColor Yellow
    try {
        $shipping = Invoke-RestMethod -Uri "$baseUrl/odop/shipping/vendor/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        $shipCount = if ($shipping) { $shipping.Count } else { 0 }
        Write-Host "SUCCESS: Shipping records loaded - Count: $shipCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Shipping"; Status="PASS"; Details="Count: $shipCount"}
    } catch {
        Write-Host "ERROR: Shipping - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Shipping"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 16: Shipping Stats
    # ===========================================
    Write-Host "`n=== Testing Shipping Stats ===" -ForegroundColor Yellow
    try {
        $shipStats = Invoke-RestMethod -Uri "$baseUrl/odop/shipping/vendor/$vendorId/stats" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Shipping stats loaded" -ForegroundColor Green
        $testResults += @{Test="Shipping Stats"; Status="PASS"; Details="Stats available"}
    } catch {
        Write-Host "ERROR: Ship Stats - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Shipping Stats"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 17: Bulk Upload Health
    # ===========================================
    Write-Host "`n=== Testing Bulk Upload Health ===" -ForegroundColor Yellow
    try {
        $bulkHealth = Invoke-RestMethod -Uri "$baseUrl/odop/bulk-upload/health" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Bulk upload service is healthy - $bulkHealth" -ForegroundColor Green
        $testResults += @{Test="Bulk Upload Health"; Status="PASS"; Details=$bulkHealth}
    } catch {
        Write-Host "ERROR: Bulk Upload - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Bulk Upload Health"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 18: Chat Health
    # ===========================================
    Write-Host "`n=== Testing Chat Health ===" -ForegroundColor Yellow
    try {
        $chatHealth = Invoke-RestMethod -Uri "$baseUrl/odop/chat/health" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Chat service is healthy - $chatHealth" -ForegroundColor Green
        $testResults += @{Test="Chat Health"; Status="PASS"; Details=$chatHealth}
    } catch {
        Write-Host "ERROR: Chat - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Chat Health"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 19: Chat Rooms (for vendor)
    # ===========================================
    Write-Host "`n=== Testing Vendor Chat Rooms ===" -ForegroundColor Yellow
    try {
        # Chat rooms endpoint uses Authentication to get userId
        $chatRooms = Invoke-RestMethod -Uri "$baseUrl/odop/chat/rooms" -Method GET -Headers $headers -ErrorAction Stop
        $roomCount = if ($chatRooms.rooms) { $chatRooms.rooms.Count } else { 0 }
        Write-Host "SUCCESS: Chat rooms loaded - Count: $roomCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Chat Rooms"; Status="PASS"; Details="Count: $roomCount"}
    } catch {
        Write-Host "ERROR: Chat Rooms - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Chat Rooms"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 20: Vendor Invoices
    # ===========================================
    Write-Host "`n=== Testing Vendor Invoices ===" -ForegroundColor Yellow
    try {
        $invoices = Invoke-RestMethod -Uri "$baseUrl/odop/invoice/vendor/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        $invoiceCount = if ($invoices) { $invoices.Count } else { 0 }
        Write-Host "SUCCESS: Invoices loaded - Count: $invoiceCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Invoices"; Status="PASS"; Details="Count: $invoiceCount"}
    } catch {
        Write-Host "ERROR: Invoices - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Invoices"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 21: Vendor Payments
    # ===========================================
    Write-Host "`n=== Testing Vendor Payments ===" -ForegroundColor Yellow
    try {
        $payments = Invoke-RestMethod -Uri "$baseUrl/odop/payment/vendor/$vendorId" -Method GET -Headers $headers -ErrorAction Stop
        $paymentCount = if ($payments) { $payments.Count } else { 0 }
        Write-Host "SUCCESS: Payments loaded - Count: $paymentCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Payments"; Status="PASS"; Details="Count: $paymentCount"}
    } catch {
        Write-Host "ERROR: Payments - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Payments"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 22: Top Products (Earnings)
    # ===========================================
    Write-Host "`n=== Testing Top Products ===" -ForegroundColor Yellow
    try {
        $topProducts = Invoke-RestMethod -Uri "$baseUrl/odop/earnings/vendor/$vendorId/top-products?limit=5" -Method GET -Headers $headers -ErrorAction Stop
        $topCount = if ($topProducts) { $topProducts.Count } else { 0 }
        Write-Host "SUCCESS: Top products loaded - Count: $topCount" -ForegroundColor Green
        $testResults += @{Test="Top Products"; Status="PASS"; Details="Count: $topCount"}
    } catch {
        Write-Host "ERROR: Top Products - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Top Products"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 23: Vendor Payouts History
    # ===========================================
    Write-Host "`n=== Testing Vendor Payouts ===" -ForegroundColor Yellow
    try {
        $payouts = Invoke-RestMethod -Uri "$baseUrl/odop/earnings/vendor/$vendorId/payouts?page=0&size=10" -Method GET -Headers $headers -ErrorAction Stop
        $payoutCount = if ($payouts.content) { $payouts.content.Count } else { 0 }
        Write-Host "SUCCESS: Payouts loaded - Count: $payoutCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Payouts"; Status="PASS"; Details="Count: $payoutCount"}
    } catch {
        Write-Host "ERROR: Payouts - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Payouts"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # SUMMARY
    # ===========================================
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "           TEST SUMMARY" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
    $passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
    $failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
    $totalCount = $testResults.Count
    
    Write-Host "`nTotal Tests: $totalCount" -ForegroundColor White
    Write-Host "Passed: $passCount" -ForegroundColor Green
    Write-Host "Failed: $failCount" -ForegroundColor Red
    Write-Host "Success Rate: $([math]::Round(($passCount / $totalCount) * 100, 1))%" -ForegroundColor $(if ($passCount -eq $totalCount) { "Green" } elseif ($passCount -gt $failCount) { "Yellow" } else { "Red" })
    
    if ($failCount -gt 0) {
        Write-Host "`nFailed Tests:" -ForegroundColor Red
        foreach ($result in ($testResults | Where-Object { $_.Status -eq "FAIL" })) {
            Write-Host "  - $($result.Test): $($result.Details)" -ForegroundColor Red
        }
    }
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    
} catch {
    Write-Host "FAILED: Authentication error" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    Write-Host "Message: $($_.Exception.Message)"
}
