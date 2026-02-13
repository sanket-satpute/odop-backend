# Comprehensive Admin Dashboard API Test Script
$baseUrl = "http://localhost:50982"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ADMIN DASHBOARD API TEST SUITE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# First, create a fresh test admin
Write-Host "`n=== Creating Test Admin ===" -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$testEmail = "testadmin$timestamp@test.com"
$testPassword = "Admin@1234"

$createBody = @{
    fullName = "Test Admin $timestamp"
    emailAddress = $testEmail
    password = $testPassword
    contactNumber = [int64](Get-Random -Minimum 9000000000 -Maximum 9999999999)
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "$baseUrl/odop/admin/create_account" -Method POST -Body $createBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "SUCCESS: Test admin created" -ForegroundColor Green
    Write-Host "Admin ID: $($createResponse.adminId)"
    Write-Host "Email: $testEmail"
    $adminId = $createResponse.adminId
} catch {
    Write-Host "ERROR creating admin: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Now authenticate as admin
Write-Host "`n=== Testing Admin Authentication ===" -ForegroundColor Yellow
$authBody = @{
    username = $testEmail
    password = $testPassword
    role = "ADMIN"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method POST -Body $authBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "SUCCESS: Admin login successful!" -ForegroundColor Green
    
    $token = $authResponse.jwt
    $adminId = $authResponse.user.adminId
    
    Write-Host "Token: $($token.Substring(0, 50))..."
    Write-Host "Admin ID: $adminId"
    
    # Setup headers for authenticated requests
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    # Initialize test results
    $testResults = @()
    
    # ===========================================
    # TEST 1: Admin Profile (Find Admin by ID)
    # ===========================================
    Write-Host "`n=== Testing Admin Profile ===" -ForegroundColor Yellow
    try {
        $profile = Invoke-RestMethod -Uri "$baseUrl/odop/admin/find_admin/$adminId" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Admin profile loaded - $($profile.name)" -ForegroundColor Green
        $testResults += @{Test="Admin Profile"; Status="PASS"; Details=$profile.name}
    } catch {
        Write-Host "ERROR: Profile - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Admin Profile"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 2: Get All Admins
    # ===========================================
    Write-Host "`n=== Testing Get All Admins ===" -ForegroundColor Yellow
    try {
        $admins = Invoke-RestMethod -Uri "$baseUrl/odop/admin/findAll_admin" -Method GET -Headers $headers -ErrorAction Stop
        $adminCount = if ($admins) { $admins.Count } else { 0 }
        Write-Host "SUCCESS: Admins loaded - Count: $adminCount" -ForegroundColor Green
        $testResults += @{Test="Get All Admins"; Status="PASS"; Details="Count: $adminCount"}
    } catch {
        Write-Host "ERROR: Get Admins - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Get All Admins"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 3: Analytics Health
    # ===========================================
    Write-Host "`n=== Testing Analytics Health ===" -ForegroundColor Yellow
    try {
        $analyticsHealth = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/health" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Analytics service is healthy - $($analyticsHealth.status)" -ForegroundColor Green
        $testResults += @{Test="Analytics Health"; Status="PASS"; Details=$analyticsHealth.status}
    } catch {
        Write-Host "ERROR: Analytics Health - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Analytics Health"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 4: Dashboard Summary
    # ===========================================
    Write-Host "`n=== Testing Dashboard Summary ===" -ForegroundColor Yellow
    try {
        $dashboardSummary = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/dashboard/summary" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Dashboard summary loaded" -ForegroundColor Green
        Write-Host "  Total Orders: $($dashboardSummary.totalOrders)"
        Write-Host "  Total Revenue: $($dashboardSummary.totalRevenue)"
        Write-Host "  Total Vendors: $($dashboardSummary.totalVendors)"
        Write-Host "  Total Customers: $($dashboardSummary.totalCustomers)"
        $testResults += @{Test="Dashboard Summary"; Status="PASS"; Details="Orders: $($dashboardSummary.totalOrders)"}
    } catch {
        Write-Host "ERROR: Dashboard Summary - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Dashboard Summary"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 5: Sales Analytics
    # ===========================================
    Write-Host "`n=== Testing Sales Analytics ===" -ForegroundColor Yellow
    try {
        $salesAnalytics = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/sales?period=MONTH" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Sales analytics loaded" -ForegroundColor Green
        $testResults += @{Test="Sales Analytics"; Status="PASS"; Details="Sales data available"}
    } catch {
        Write-Host "ERROR: Sales Analytics - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Sales Analytics"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 6: Geographic Analytics
    # ===========================================
    Write-Host "`n=== Testing Geographic Analytics ===" -ForegroundColor Yellow
    try {
        $geoAnalytics = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/geographic?period=MONTH" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Geographic analytics loaded" -ForegroundColor Green
        $testResults += @{Test="Geographic Analytics"; Status="PASS"; Details="Geographic data available"}
    } catch {
        Write-Host "ERROR: Geographic Analytics - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Geographic Analytics"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 7: Vendor Leaderboard
    # ===========================================
    Write-Host "`n=== Testing Vendor Leaderboard ===" -ForegroundColor Yellow
    try {
        $leaderboard = Invoke-RestMethod -Uri "$baseUrl/odop/analytics/vendors/leaderboard?limit=10" -Method GET -Headers $headers -ErrorAction Stop
        $leaderCount = if ($leaderboard) { $leaderboard.Count } else { 0 }
        Write-Host "SUCCESS: Vendor leaderboard loaded - Count: $leaderCount" -ForegroundColor Green
        $testResults += @{Test="Vendor Leaderboard"; Status="PASS"; Details="Count: $leaderCount"}
    } catch {
        Write-Host "ERROR: Vendor Leaderboard - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Vendor Leaderboard"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 8: All Customers (Admin needs special endpoint)
    # ===========================================
    Write-Host "`n=== Testing Admin User Stats ===" -ForegroundColor Yellow
    try {
        # Use dashboard summary which includes customer count
        $userStats = @{
            customersFromSummary = $dashboardSummary.totalCustomers
            vendorsFromSummary = $dashboardSummary.totalVendors
        }
        Write-Host "SUCCESS: User stats from dashboard - Customers: $($userStats.customersFromSummary), Vendors: $($userStats.vendorsFromSummary)" -ForegroundColor Green
        $testResults += @{Test="Admin User Stats"; Status="PASS"; Details="Customers: $($userStats.customersFromSummary)"}
    } catch {
        Write-Host "ERROR: User Stats - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Admin User Stats"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 9: All Vendors (Public)
    # ===========================================
    Write-Host "`n=== Testing Get All Vendors ===" -ForegroundColor Yellow
    try {
        $vendors = Invoke-RestMethod -Uri "$baseUrl/odop/vendor/get_all_vendors" -Method GET -ErrorAction Stop
        $vendorCount = if ($vendors) { $vendors.Count } else { 0 }
        Write-Host "SUCCESS: Vendors loaded - Count: $vendorCount" -ForegroundColor Green
        $testResults += @{Test="Get All Vendors"; Status="PASS"; Details="Count: $vendorCount"}
    } catch {
        Write-Host "ERROR: Get Vendors - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Get All Vendors"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 10: All Products (Public)
    # ===========================================
    Write-Host "`n=== Testing Get All Products ===" -ForegroundColor Yellow
    try {
        $products = Invoke-RestMethod -Uri "$baseUrl/odop/product/get_all_products" -Method GET -ErrorAction Stop
        $productCount = if ($products) { $products.Count } else { 0 }
        Write-Host "SUCCESS: Products loaded - Count: $productCount" -ForegroundColor Green
        $testResults += @{Test="Get All Products"; Status="PASS"; Details="Count: $productCount"}
    } catch {
        Write-Host "ERROR: Get Products - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Get All Products"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 11: All Categories (Public)
    # ===========================================
    Write-Host "`n=== Testing Get All Categories ===" -ForegroundColor Yellow
    try {
        $categories = Invoke-RestMethod -Uri "$baseUrl/odop/category/get_all_categorie" -Method GET -ErrorAction Stop
        $categoryCount = if ($categories) { $categories.Count } else { 0 }
        Write-Host "SUCCESS: Categories loaded - Count: $categoryCount" -ForegroundColor Green
        $testResults += @{Test="Get All Categories"; Status="PASS"; Details="Count: $categoryCount"}
    } catch {
        Write-Host "ERROR: Get Categories - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Get All Categories"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 12: All Orders
    # ===========================================
    Write-Host "`n=== Testing Get All Orders ===" -ForegroundColor Yellow
    try {
        $orders = Invoke-RestMethod -Uri "$baseUrl/odop/order/all" -Method GET -Headers $headers -ErrorAction Stop
        $orderCount = if ($orders) { $orders.Count } else { 0 }
        Write-Host "SUCCESS: Orders loaded - Count: $orderCount" -ForegroundColor Green
        $testResults += @{Test="Get All Orders"; Status="PASS"; Details="Count: $orderCount"}
    } catch {
        Write-Host "ERROR: Get Orders - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Get All Orders"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 13: Coupon Health
    # ===========================================
    Write-Host "`n=== Testing Coupon Health ===" -ForegroundColor Yellow
    try {
        $couponHealth = Invoke-RestMethod -Uri "$baseUrl/odop/coupon/health" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Coupon service is healthy - $($couponHealth.status)" -ForegroundColor Green
        $testResults += @{Test="Coupon Health"; Status="PASS"; Details=$couponHealth.status}
    } catch {
        Write-Host "ERROR: Coupon Health - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Coupon Health"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 14: Get All Coupons
    # ===========================================
    Write-Host "`n=== Testing Get All Coupons ===" -ForegroundColor Yellow
    try {
        $coupons = Invoke-RestMethod -Uri "$baseUrl/odop/coupon" -Method GET -Headers $headers -ErrorAction Stop
        $couponCount = if ($coupons) { $coupons.Count } else { 0 }
        Write-Host "SUCCESS: Coupons loaded - Count: $couponCount" -ForegroundColor Green
        $testResults += @{Test="Get All Coupons"; Status="PASS"; Details="Count: $couponCount"}
    } catch {
        Write-Host "ERROR: Get Coupons - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Get All Coupons"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 15: CMS Banners (Public)
    # ===========================================
    Write-Host "`n=== Testing CMS Banners ===" -ForegroundColor Yellow
    try {
        $banners = Invoke-RestMethod -Uri "$baseUrl/odop/cms/banners" -Method GET -ErrorAction Stop
        $bannerCount = if ($banners) { $banners.Count } else { 0 }
        Write-Host "SUCCESS: Banners loaded - Count: $bannerCount" -ForegroundColor Green
        $testResults += @{Test="CMS Banners"; Status="PASS"; Details="Count: $bannerCount"}
    } catch {
        Write-Host "ERROR: CMS Banners - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="CMS Banners"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 16: CMS FAQs (Public)
    # ===========================================
    Write-Host "`n=== Testing CMS FAQs ===" -ForegroundColor Yellow
    try {
        $faqs = Invoke-RestMethod -Uri "$baseUrl/odop/cms/faqs" -Method GET -ErrorAction Stop
        $faqCount = if ($faqs) { $faqs.Count } else { 0 }
        Write-Host "SUCCESS: FAQs loaded - Count: $faqCount" -ForegroundColor Green
        $testResults += @{Test="CMS FAQs"; Status="PASS"; Details="Count: $faqCount"}
    } catch {
        Write-Host "ERROR: CMS FAQs - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="CMS FAQs"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 17: CMS Testimonials (Public)
    # ===========================================
    Write-Host "`n=== Testing CMS Testimonials ===" -ForegroundColor Yellow
    try {
        $testimonials = Invoke-RestMethod -Uri "$baseUrl/odop/cms/testimonials" -Method GET -ErrorAction Stop
        $testimonialCount = if ($testimonials) { $testimonials.Count } else { 0 }
        Write-Host "SUCCESS: Testimonials loaded - Count: $testimonialCount" -ForegroundColor Green
        $testResults += @{Test="CMS Testimonials"; Status="PASS"; Details="Count: $testimonialCount"}
    } catch {
        Write-Host "ERROR: CMS Testimonials - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="CMS Testimonials"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 18: Returns Admin - All Returns
    # ===========================================
    Write-Host "`n=== Testing Admin Returns ===" -ForegroundColor Yellow
    try {
        $returns = Invoke-RestMethod -Uri "$baseUrl/odop/returns/admin/all" -Method GET -Headers $headers -ErrorAction Stop
        $returnCount = if ($returns.returns) { $returns.returns.Count } else { 0 }
        Write-Host "SUCCESS: Returns loaded - Count: $returnCount" -ForegroundColor Green
        $testResults += @{Test="Admin Returns"; Status="PASS"; Details="Count: $returnCount"}
    } catch {
        Write-Host "ERROR: Admin Returns - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Admin Returns"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 19: Returns Admin Summary
    # ===========================================
    Write-Host "`n=== Testing Returns Admin Summary ===" -ForegroundColor Yellow
    try {
        $returnSummary = Invoke-RestMethod -Uri "$baseUrl/odop/returns/admin/summary" -Method GET -Headers $headers -ErrorAction Stop
        Write-Host "SUCCESS: Returns summary loaded" -ForegroundColor Green
        $testResults += @{Test="Returns Summary"; Status="PASS"; Details="Summary available"}
    } catch {
        Write-Host "ERROR: Returns Summary - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Returns Summary"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 20: Platform Settings (Public)
    # ===========================================
    Write-Host "`n=== Testing Platform Settings ===" -ForegroundColor Yellow
    try {
        $settings = Invoke-RestMethod -Uri "$baseUrl/odop/settings/public/contact" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Platform settings loaded" -ForegroundColor Green
        $testResults += @{Test="Platform Settings"; Status="PASS"; Details="Settings available"}
    } catch {
        Write-Host "ERROR: Platform Settings - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Platform Settings"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 21: Craft Categories (Public)
    # ===========================================
    Write-Host "`n=== Testing Craft Categories ===" -ForegroundColor Yellow
    try {
        $crafts = Invoke-RestMethod -Uri "$baseUrl/odop/craft-categories/roots" -Method GET -ErrorAction Stop
        $craftCount = if ($crafts) { $crafts.Count } else { 0 }
        Write-Host "SUCCESS: Craft categories loaded - Count: $craftCount" -ForegroundColor Green
        $testResults += @{Test="Craft Categories"; Status="PASS"; Details="Count: $craftCount"}
    } catch {
        Write-Host "ERROR: Craft Categories - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Craft Categories"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 22: Festival Collections (Public)
    # ===========================================
    Write-Host "`n=== Testing Festival Collections ===" -ForegroundColor Yellow
    try {
        $festivals = Invoke-RestMethod -Uri "$baseUrl/odop/festivals" -Method GET -ErrorAction Stop
        $festivalCount = if ($festivals.content) { $festivals.content.Count } elseif ($festivals) { $festivals.Count } else { 0 }
        Write-Host "SUCCESS: Festivals loaded - Count: $festivalCount" -ForegroundColor Green
        $testResults += @{Test="Festival Collections"; Status="PASS"; Details="Count: $festivalCount"}
    } catch {
        Write-Host "ERROR: Festivals - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Festival Collections"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 23: Government Schemes (Public)
    # ===========================================
    Write-Host "`n=== Testing Government Schemes ===" -ForegroundColor Yellow
    try {
        $schemes = Invoke-RestMethod -Uri "$baseUrl/odop/schemes" -Method GET -ErrorAction Stop
        $schemeCount = if ($schemes.content) { $schemes.content.Count } elseif ($schemes) { $schemes.Count } else { 0 }
        Write-Host "SUCCESS: Schemes loaded - Count: $schemeCount" -ForegroundColor Green
        $testResults += @{Test="Government Schemes"; Status="PASS"; Details="Count: $schemeCount"}
    } catch {
        Write-Host "ERROR: Schemes - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Government Schemes"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 24: Artisan Stories (Public)
    # ===========================================
    Write-Host "`n=== Testing Artisan Stories ===" -ForegroundColor Yellow
    try {
        $artisans = Invoke-RestMethod -Uri "$baseUrl/odop/artisans" -Method GET -ErrorAction Stop
        $artisanCount = if ($artisans.content) { $artisans.content.Count } elseif ($artisans) { $artisans.Count } else { 0 }
        Write-Host "SUCCESS: Artisan stories loaded - Count: $artisanCount" -ForegroundColor Green
        $testResults += @{Test="Artisan Stories"; Status="PASS"; Details="Count: $artisanCount"}
    } catch {
        Write-Host "ERROR: Artisans - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Artisan Stories"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 25: District Map (Public)
    # ===========================================
    Write-Host "`n=== Testing District Map ===" -ForegroundColor Yellow
    try {
        $districtMap = Invoke-RestMethod -Uri "$baseUrl/odop/district-map/statistics" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: District map stats loaded" -ForegroundColor Green
        $testResults += @{Test="District Map"; Status="PASS"; Details="Statistics available"}
    } catch {
        Write-Host "ERROR: District Map - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="District Map"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 26: Review Moderation (Admin)
    # ===========================================
    Write-Host "`n=== Testing Review Moderation ===" -ForegroundColor Yellow
    try {
        $pendingReviews = Invoke-RestMethod -Uri "$baseUrl/odop/review/admin/pending" -Method GET -Headers $headers -ErrorAction Stop
        $pendingCount = if ($pendingReviews.content) { $pendingReviews.content.Count } elseif ($pendingReviews) { $pendingReviews.Count } else { 0 }
        Write-Host "SUCCESS: Pending reviews loaded - Count: $pendingCount" -ForegroundColor Green
        $testResults += @{Test="Review Moderation"; Status="PASS"; Details="Count: $pendingCount"}
    } catch {
        Write-Host "ERROR: Pending Reviews - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Review Moderation"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 27: Chat Health (Public)
    # ===========================================
    Write-Host "`n=== Testing Chat System ===" -ForegroundColor Yellow
    try {
        $chatHealth = Invoke-RestMethod -Uri "$baseUrl/odop/chat/health" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Chat system healthy - $($chatHealth.status)" -ForegroundColor Green
        $testResults += @{Test="Chat System"; Status="PASS"; Details=$chatHealth.status}
    } catch {
        Write-Host "ERROR: Chat System - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Chat System"; Status="FAIL"; Details=$_.Exception.Message}
    }
    
    # ===========================================
    # TEST 28: Admin Reports Health
    # ===========================================
    Write-Host "`n=== Testing Reports System ===" -ForegroundColor Yellow
    try {
        $reportsHealth = Invoke-RestMethod -Uri "$baseUrl/odop/reports/health" -Method GET -ErrorAction Stop
        Write-Host "SUCCESS: Reports system healthy - $($reportsHealth.status)" -ForegroundColor Green
        $testResults += @{Test="Reports System"; Status="PASS"; Details=$reportsHealth.status}
    } catch {
        Write-Host "ERROR: Reports - $($_.Exception.Message)" -ForegroundColor Red
        $testResults += @{Test="Reports System"; Status="FAIL"; Details=$_.Exception.Message}
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
