$BASE = "http://localhost:50982/odop"
$AUTH = "http://localhost:50982/authenticate"
$ts = Get-Date -Format "yyyyMMddHHmmss"
$results = @()

function Log-Test {
    param([string]$name, [bool]$pass, [string]$details = "")
    $status = if ($pass) { "[PASS]" } else { "[FAIL]" }
    $color = if ($pass) { "Green" } else { "Red" }
    Write-Host "$status $name $details" -ForegroundColor $color
    $script:results += @{ Name = $name; Pass = $pass; Details = $details }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "      ADMIN E2E API TEST SUITE" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ========== SECTION 1: Admin Account Management ==========
Write-Host "--- Section 1: Admin Account Management ---" -ForegroundColor Yellow

# Test 1.1: Check Admin Exists (prereq check)
$t = "1.1 Check Admin Exists Endpoint"
try {
    $r = Invoke-RestMethod -Uri "$BASE/admin/check_admin_exists?emailAddress=test@t.com&authorizationKey=test" -TimeoutSec 10
    Log-Test $t $true "(Response: $r)"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 1.2: Create Admin Account
$t = "1.2 Create Admin Account"
$authKey = "ADMIN_KEY_$ts"
$adminEmail = "admin$ts@test.com"
$body = @{
    fullName = "TestAdmin$ts"
    emailAddress = $adminEmail
    password = "Admin@123"
    contactNumber = 9999999999
    positionAndRole = "Super Admin"
    authorizationKey = $authKey
} | ConvertTo-Json
try {
    $admin = Invoke-RestMethod -Uri "$BASE/admin/create_account" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 15
    $adminId = $admin.adminId
    Log-Test $t ($adminId -ne $null) "(ID: $adminId)"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 1.3: Admin Login via Authenticate Endpoint
$t = "1.3 Admin Login (JWT)"
if ($adminId) {
    $loginBody = @{ username = $adminEmail; password = "Admin@123"; role = "admin" } | ConvertTo-Json
    try {
        $login = Invoke-RestMethod -Uri $AUTH -Method POST -Body $loginBody -ContentType "application/json" -TimeoutSec 10
        $jwt = $login.jwt
        Log-Test $t ($jwt -ne $null) "(JWT: $($jwt.Substring(0,30))...)"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t - No Admin ID" -ForegroundColor Yellow }

# Test 1.4: Get Admin by ID
$t = "1.4 Get Admin by ID"
if ($adminId -and $jwt) {
    try {
        $a = Invoke-RestMethod -Uri "$BASE/admin/find_admin/$adminId" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($a.adminId -eq $adminId) "(Name: $($a.fullName))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 1.5: Get All Admins
$t = "1.5 Get All Admins"
if ($jwt) {
    try {
        $admins = Invoke-RestMethod -Uri "$BASE/admin/findAll_admin" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($admins.Count -ge 1) "(Count: $($admins.Count))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 1.6: Update Admin Profile
$t = "1.6 Update Admin Profile"
if ($adminId -and $jwt) {
    $upBody = @{
        adminId = $adminId
        fullName = "UpdatedAdmin$ts"
        emailAddress = $adminEmail
        contactNumber = 8888888888
        positionAndRole = "System Admin"
        active = $true
    } | ConvertTo-Json
    try {
        $updated = Invoke-RestMethod -Uri "$BASE/admin/update_admin/$adminId" -Method PUT -Body $upBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($updated.fullName -eq "UpdatedAdmin$ts") "(Name: $($updated.fullName))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 1.7: Update Admin Status
$t = "1.7 Update Admin Status"
if ($adminId -and $jwt) {
    $statusBody = @{ status = $false } | ConvertTo-Json
    try {
        $s = Invoke-RestMethod -Uri "$BASE/admin/update_status/$adminId" -Method PATCH -Body $statusBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t $true "(Active: $($s.active))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SECTION 2: Analytics Dashboard (Admin Only) ==========
Write-Host "`n--- Section 2: Analytics Dashboard ---" -ForegroundColor Yellow

# Test 2.1: Analytics Health
$t = "2.1 Analytics Health"
try {
    $h = Invoke-RestMethod -Uri "$BASE/analytics/health" -TimeoutSec 10
    Log-Test $t ($h.status -eq "UP") "(Status: $($h.status))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 2.2: Dashboard Summary (Admin Only)
$t = "2.2 Dashboard Summary"
if ($jwt) {
    try {
        $dash = Invoke-RestMethod -Uri "$BASE/analytics/dashboard/summary" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 15
        Log-Test $t ($dash -ne $null) "(Revenue: $($dash.totalRevenue))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 2.3: Sales Analytics (Admin Only)
$t = "2.3 Sales Analytics"
if ($jwt) {
    try {
        $sales = Invoke-RestMethod -Uri "$BASE/analytics/sales?period=MONTH" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 15
        Log-Test $t ($sales -ne $null) "(Period: $($sales.period))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 2.4: Geographic Analytics (Admin Only)
$t = "2.4 Geographic Analytics"
if ($jwt) {
    try {
        $geo = Invoke-RestMethod -Uri "$BASE/analytics/geographic?period=MONTH" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 15
        Log-Test $t ($geo -ne $null) "(States: $($geo.stateData.Count))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 2.5: Vendor Leaderboard (Admin Only)
$t = "2.5 Vendor Leaderboard"
if ($jwt) {
    try {
        $lb = Invoke-RestMethod -Uri "$BASE/analytics/vendors/leaderboard?limit=10" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 15
        Log-Test $t $true "(Vendors: $(@($lb).Count))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SECTION 3: CMS Management (Admin Only) ==========
Write-Host "`n--- Section 3: CMS Management ---" -ForegroundColor Yellow

# Test 3.1: Get FAQs (Public)
$t = "3.1 Get FAQs"
try {
    $faqs = Invoke-RestMethod -Uri "$BASE/cms/faqs" -TimeoutSec 10
    Log-Test $t $true "(FAQs: $($faqs.total))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 3.2: Get Banners (Public)
$t = "3.2 Get Banners"
try {
    $banners = Invoke-RestMethod -Uri "$BASE/cms/banners" -TimeoutSec 10
    Log-Test $t $true "(Banners: $($banners.total))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 3.3: Get Pages (Admin)
$t = "3.3 Get CMS Pages"
if ($jwt) {
    try {
        $pages = Invoke-RestMethod -Uri "$BASE/cms/pages" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t $true "(Pages: $($pages.total))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 3.4: Create CMS Page (Admin)
$t = "3.4 Create CMS Page"
if ($jwt) {
    $pageBody = @{
        title = "Test Page $ts"
        slug = "test-page-$ts"
        content = "Test content for the page"
        status = "DRAFT"
        metaTitle = "Test Page"
        metaDescription = "Test description"
    } | ConvertTo-Json
    try {
        $page = Invoke-RestMethod -Uri "$BASE/cms/pages" -Method POST -Body $pageBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        $pageId = $page.id
        Log-Test $t ($pageId -ne $null) "(ID: $pageId)"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 3.5: Create FAQ (Admin)
$t = "3.5 Create FAQ"
if ($jwt) {
    $faqBody = @{
        question = "Test FAQ Question $ts?"
        answer = "Test FAQ Answer"
        category = "General"
        active = $true
        order = 99
    } | ConvertTo-Json
    try {
        $faq = Invoke-RestMethod -Uri "$BASE/cms/faqs" -Method POST -Body $faqBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($faq.id -ne $null) "(ID: $($faq.id))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SECTION 4: Coupon Management (Admin Only) ==========
Write-Host "`n--- Section 4: Coupon Management ---" -ForegroundColor Yellow

# Test 4.1: Coupon Health
$t = "4.1 Coupon Health"
try {
    $ch = Invoke-RestMethod -Uri "$BASE/coupon/health" -TimeoutSec 10
    Log-Test $t ($ch.status -eq "UP") "(Status: $($ch.status))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 4.2: Get Available Coupons (Public)
$t = "4.2 Get Available Coupons"
try {
    $coupons = Invoke-RestMethod -Uri "$BASE/coupon/available" -TimeoutSec 10
    Log-Test $t $true "(Coupons: $(@($coupons).Count))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 4.3: Get All Coupons (Admin)
$t = "4.3 Get All Coupons (Admin)"
if ($jwt) {
    try {
        $allCoupons = Invoke-RestMethod -Uri "$BASE/coupon" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t $true "(Total: $(@($allCoupons).Count))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 4.4: Create Coupon (Admin)
$t = "4.4 Create Coupon"
$couponCode = "TEST$ts"
if ($jwt) {
    $couponBody = @{
        code = $couponCode
        description = "Test coupon $ts"
        discountType = "PERCENTAGE"
        discountValue = 10
        minOrderAmount = 100
        maxDiscountAmount = 500
        usageLimit = 100
        startDate = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        endDate = (Get-Date).AddDays(30).ToString("yyyy-MM-ddTHH:mm:ss")
        active = $true
    } | ConvertTo-Json
    try {
        $coupon = Invoke-RestMethod -Uri "$BASE/coupon" -Method POST -Body $couponBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($coupon.code -eq $couponCode) "(Code: $($coupon.code))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 4.5: Get Coupon by Code (Admin)
$t = "4.5 Get Coupon by Code"
if ($jwt -and $couponCode) {
    try {
        $c = Invoke-RestMethod -Uri "$BASE/coupon/$couponCode" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($c.code -eq $couponCode) "(Discount: $($c.discountValue)%)"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 4.6: Update Coupon (Admin)
$t = "4.6 Update Coupon"
if ($jwt -and $couponCode) {
    $updateBody = @{
        code = $couponCode
        description = "Updated coupon $ts"
        discountType = "PERCENTAGE"
        discountValue = 15
        minOrderAmount = 200
        active = $true
    } | ConvertTo-Json
    try {
        $updated = Invoke-RestMethod -Uri "$BASE/coupon/$couponCode" -Method PUT -Body $updateBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($updated.discountValue -eq 15) "(New Discount: $($updated.discountValue)%)"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SECTION 5: Order Management (Admin) ==========
Write-Host "`n--- Section 5: Order Management ---" -ForegroundColor Yellow

# Test 5.1: Get All Orders (Admin)
$t = "5.1 Get All Orders"
if ($jwt) {
    try {
        $orders = Invoke-RestMethod -Uri "$BASE/order/all" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 15
        Log-Test $t $true "(Orders: $(@($orders).Count))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 5.2: Get Orders Paginated (Admin)
$t = "5.2 Get Orders Paginated"
if ($jwt) {
    try {
        $ordersPaged = Invoke-RestMethod -Uri "$BASE/order/paginated?page=0&size=10" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 15
        Log-Test $t ($ordersPaged -ne $null) "(Total: $($ordersPaged.totalElements))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SECTION 6: Product Management (Admin) ==========
Write-Host "`n--- Section 6: Product Management ---" -ForegroundColor Yellow

# Test 6.1: Get All Products (Public)
$t = "6.1 Get All Products"
try {
    $products = Invoke-RestMethod -Uri "$BASE/product/get_all_products" -TimeoutSec 10
    Log-Test $t $true "(Products: $(@($products).Count))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 6.2: Get Featured Products (Public)
$t = "6.2 Get Featured Products"
try {
    $featured = Invoke-RestMethod -Uri "$BASE/product/featured" -TimeoutSec 10
    Log-Test $t $true "(Featured: $(@($featured).Count))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 6.3: Get All Categories (Public)
$t = "6.3 Get All Categories"
try {
    $cats = Invoke-RestMethod -Uri "$BASE/category/get_all_categories" -TimeoutSec 10
    Log-Test $t $true "(Categories: $(@($cats).Count))"
} catch { Log-Test $t $false $_.Exception.Message }

# ========== SECTION 7: Vendor Management (Admin) ==========
Write-Host "`n--- Section 7: Vendor Management ---" -ForegroundColor Yellow

# Test 7.1: Get All Vendors
$t = "7.1 Get All Vendors"
try {
    $vendors = Invoke-RestMethod -Uri "$BASE/vendor/get_all_vendors" -TimeoutSec 10
    $vendorCount = @($vendors).Count
    Log-Test $t ($vendorCount -ge 0) "(Vendors: $vendorCount)"
    if ($vendorCount -gt 0) { $testVendorId = $vendors[0].vendorId }
} catch { Log-Test $t $false $_.Exception.Message }

# Test 7.2: Update Vendor Status (Admin)
$t = "7.2 Update Vendor Status"
if ($jwt -and $testVendorId) {
    $statusBody = @{ status = "approved" } | ConvertTo-Json
    try {
        $v = Invoke-RestMethod -Uri "$BASE/vendor/update_status/$testVendorId" -Method PATCH -Body $statusBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t ($v.status -eq "approved") "(Status: $($v.status))"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SECTION 8: Returns Management (Admin) ==========
Write-Host "`n--- Section 8: Returns Management ---" -ForegroundColor Yellow

# Test 8.1: Returns Health
$t = "8.1 Returns Health"
try {
    $rh = Invoke-RestMethod -Uri "$BASE/returns/health" -TimeoutSec 10
    Log-Test $t ($rh.status -eq "UP") "(Status: $($rh.status))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 8.2: Return Policy (Public)
$t = "8.2 Return Policy"
try {
    $policy = Invoke-RestMethod -Uri "$BASE/returns/policy" -TimeoutSec 10
    Log-Test $t ($policy -ne $null) "(Policy loaded)"
} catch { Log-Test $t $false $_.Exception.Message }

# ========== SECTION 9: Search Features (Admin Sync) ==========
Write-Host "`n--- Section 9: Search Features ---" -ForegroundColor Yellow

# Test 9.1: Search Health
$t = "9.1 Search Health"
try {
    $sh = Invoke-RestMethod -Uri "$BASE/search/health" -TimeoutSec 10
    Log-Test $t ($sh.status -eq "UP") "(Status: $($sh.status))"
} catch { Log-Test $t $false $_.Exception.Message }

# Test 9.2: Search Products (Public)
$t = "9.2 Search Products"
try {
    $sr = Invoke-RestMethod -Uri "$BASE/search?q=handicraft&page=0&size=10" -TimeoutSec 15
    Log-Test $t $true "(Results: $($sr.totalResults))"
} catch { Log-Test $t $false $_.Exception.Message }

# ========== SECTION 10: Platform Settings (Admin) ==========
Write-Host "`n--- Section 10: Platform Settings ---" -ForegroundColor Yellow

# Test 10.1: Get Public Settings
$t = "10.1 Get Public Settings"
try {
    $settings = Invoke-RestMethod -Uri "$BASE/settings/public/general" -TimeoutSec 10
    Log-Test $t $true "(Settings loaded)"
} catch { Log-Test $t $false $_.Exception.Message }

# ========== CLEANUP ==========
Write-Host "`n--- Cleanup ---" -ForegroundColor Yellow

# Test C.1: Delete Coupon
$t = "C.1 Delete Coupon"
if ($jwt -and $couponCode) {
    try {
        $del = Invoke-RestMethod -Uri "$BASE/coupon/$couponCode" -Method DELETE -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t $true "(Deleted: $couponCode)"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test C.2: Delete Admin
$t = "C.2 Delete Test Admin"
if ($jwt -and $adminId) {
    try {
        $delAdmin = Invoke-RestMethod -Uri "$BASE/admin/delete_by_id/$adminId" -Method DELETE -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10
        Log-Test $t $delAdmin "(Deleted: $adminId)"
    } catch { Log-Test $t $false $_.Exception.Message }
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# ========== SUMMARY ==========
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "           TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
$passed = ($results | Where-Object { $_.Pass }).Count
$failed = ($results | Where-Object { -not $_.Pass }).Count
$total = $results.Count
Write-Host "Total:  $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
Write-Host "Pass Rate: $([math]::Round($passed/$total*100, 1))%" -ForegroundColor $(if ($passed -eq $total) { "Green" } else { "Yellow" })

if ($failed -gt 0) {
    Write-Host "`nFailed Tests:" -ForegroundColor Red
    $results | Where-Object { -not $_.Pass } | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Details)" -ForegroundColor Red
    }
}
Write-Host "`n========================================`n" -ForegroundColor Cyan
