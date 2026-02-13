$BASE = "http://localhost:50982/odop"
$ts = Get-Date -Format "yyyyMMddHHmmss"

Write-Host "=== VENDOR API TESTS ===" -ForegroundColor Cyan

# Test 1
$t = "Get All Vendors"
try { $r = Invoke-RestMethod -Uri "$BASE/vendor/get_all_vendors" -TimeoutSec 10; Write-Host "[PASS] $t (Count: $($r.Count))" -ForegroundColor Green } catch { Write-Host "[FAIL] $t" -ForegroundColor Red }

# Test 2
$t = "Register Vendor"
$body = @{ shoppeeName="TestShop$ts"; shopkeeperName="TestVendor"; emailAddress="v$ts@test.com"; password="Test@123"; contactNumber="1234567890"; shoppeeAddress="Test"; locationDistrict="Test"; locationState="UP"; pinCode="123456" } | ConvertTo-Json
try { $reg = Invoke-RestMethod -Uri "$BASE/vendor/create_account" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 15; $vid = $reg.vendorId; $email = "v$ts@test.com"; Write-Host "[PASS] $t (ID: $vid)" -ForegroundColor Green } catch { Write-Host "[FAIL] $t : $($_.Exception.Message)" -ForegroundColor Red }

# Test 3
$t = "Vendor Login"
if ($vid) {
    $loginBody = @{ username=$email; password="Test@123"; role="vendor" } | ConvertTo-Json
    try { $login = Invoke-RestMethod -Uri "http://localhost:50982/authenticate" -Method POST -Body $loginBody -ContentType "application/json" -TimeoutSec 10; $jwt = $login.jwt; Write-Host "[PASS] $t" -ForegroundColor Green } catch { Write-Host "[FAIL] $t : $($_.Exception.Message)" -ForegroundColor Red }
} else { Write-Host "[SKIP] $t - No vendor" -ForegroundColor Yellow }

# Test 4
$t = "Get Vendor by ID"
if ($vid) { try { $v = Invoke-RestMethod -Uri "$BASE/vendor/get_vendor_id/$vid" -TimeoutSec 10; Write-Host "[PASS] $t ($($v.shoppeeName))" -ForegroundColor Green } catch { Write-Host "[FAIL] $t" -ForegroundColor Red } } else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 5
$t = "Get Vendor Products"
if ($vid) { try { $prods = Invoke-RestMethod -Uri "$BASE/product/get_product_by_vendor_id/$vid" -TimeoutSec 10; Write-Host "[PASS] $t" -ForegroundColor Green } catch { Write-Host "[FAIL] $t" -ForegroundColor Red } } else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 6
$t = "Get Vendor Orders"
if ($vid -and $jwt) { try { $orders = Invoke-RestMethod -Uri "$BASE/order/vendor/$vid" -TimeoutSec 10 -Headers @{Authorization="Bearer $jwt"}; Write-Host "[PASS] $t" -ForegroundColor Green } catch { Write-Host "[FAIL] $t" -ForegroundColor Red } } else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 7
$t = "Get Categories"
try { $cats = Invoke-RestMethod -Uri "$BASE/category/get_all_categories" -TimeoutSec 10; Write-Host "[PASS] $t (Count: $($cats.Count))" -ForegroundColor Green } catch { Write-Host "[FAIL] $t : $($_.Exception.Message)" -ForegroundColor Red }

# Test 8
$t = "Update Vendor Profile"
if ($vid -and $jwt) { 
    $upBody = @{ vendorId=$vid; shoppeeName="Updated$ts"; shopkeeperName="TestVendor"; emailAddress=$email; locationState="UP" } | ConvertTo-Json
    try { $upd = Invoke-RestMethod -Uri "$BASE/vendor/update_vendor_by_id/$vid" -Method PUT -Body $upBody -ContentType "application/json" -Headers @{Authorization="Bearer $jwt"} -TimeoutSec 10; Write-Host "[PASS] $t" -ForegroundColor Green } catch { Write-Host "[FAIL] $t" -ForegroundColor Red } 
} else { Write-Host "[SKIP] $t" -ForegroundColor Yellow }

# Test 9
$t = "Search by State"
try { $sv = Invoke-RestMethod -Uri "$BASE/vendor/search_by_state?state=UP" -TimeoutSec 10; Write-Host "[PASS] $t (Count: $(@($sv).Count))" -ForegroundColor Green } catch { Write-Host "[FAIL] $t" -ForegroundColor Red }

# Test 10
$t = "Get Vendor Orders Paginated"
if ($vid -and $jwt) { try { $ao = Invoke-RestMethod -Uri "$BASE/order/vendor/$vid/paginated?page=0&size=10" -TimeoutSec 10 -Headers @{Authorization="Bearer $jwt"}; Write-Host "[PASS] $t (Total: $($ao.totalElements))" -ForegroundColor Green } catch { Write-Host "[FAIL] $t : $($_.Exception.Message)" -ForegroundColor Red } } else { Write-Host "[SKIP] $t - No JWT" -ForegroundColor Yellow }

Write-Host "=== TESTS COMPLETE ===" -ForegroundColor Cyan
