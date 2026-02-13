# ============================================================================
# VENDOR END-TO-END TEST SUITE
# Comprehensive testing of all vendor functionality in ODOP
# ============================================================================

$BASE_URL = "http://localhost:50982/odop"
$AUTH_URL = "http://localhost:50982/authenticate"
$timestamp = Get-Date -Format "yyyyMMddHHmmss"

# Test counters
$script:totalTests = 0
$script:passedTests = 0
$script:failedTests = 0
$script:testResults = @()

# Colors for output
function Write-TestHeader($text) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host $text -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
}

function Write-Pass($testName) {
    $script:totalTests++
    $script:passedTests++
    Write-Host "[PASS] $testName" -ForegroundColor Green
    $script:testResults += @{ Name = $testName; Status = "PASS"; Details = "" }
}

function Write-Fail($testName, $details = "") {
    $script:totalTests++
    $script:failedTests++
    Write-Host "[FAIL] $testName" -ForegroundColor Red
    if ($details) { Write-Host "       Details: $details" -ForegroundColor Yellow }
    $script:testResults += @{ Name = $testName; Status = "FAIL"; Details = $details }
}

function Write-Skip($testName, $reason = "") {
    Write-Host "[SKIP] $testName - $reason" -ForegroundColor Yellow
    $script:testResults += @{ Name = $testName; Status = "SKIP"; Details = $reason }
}

function Invoke-SafeRequest {
    param([string]$Method, [string]$Uri, $Body = $null, $Headers = @{})
    try {
        $params = @{
            Method = $Method
            Uri = $Uri
            ContentType = "application/json"
            Headers = $Headers
            ErrorAction = "Stop"
        }
        if ($Body) { $params.Body = ($Body | ConvertTo-Json -Depth 10) }
        return Invoke-RestMethod @params
    } catch {
        return @{ error = $true; message = $_.Exception.Message; statusCode = $_.Exception.Response.StatusCode.value__ }
    }
}

function Invoke-SafeRequestForm {
    param([string]$Uri, $FormData, $Headers = @{})
    try {
        return Invoke-RestMethod -Method POST -Uri $Uri -Form $FormData -Headers $Headers -ErrorAction Stop
    } catch {
        return @{ error = $true; message = $_.Exception.Message; statusCode = $_.Exception.Response.StatusCode.value__ }
    }
}

# ============================================================================
# TEST DATA
# ============================================================================
$testVendor = @{
    shoppeeName = "Test Artisan Shop $timestamp"
    shopkeeperName = "Test Vendor $timestamp"
    emailAddress = "test.vendor.$timestamp@example.com"
    password = "Test@Vendor123"
    contactNumber = "+91-9876543210"
    shoppeeAddress = "123 Test Street, Market Area"
    locationDistrict = "Varanasi"
    locationState = "Uttar Pradesh"
    pinCode = "221001"
    businessRegistryNumber = "BRN$timestamp"
    taxIdentificationNumber = "TIN$timestamp"
    businessLicenseNumber = "BLN$timestamp"
    completeAddress = "123 Test Street, Market Area, Varanasi, UP 221001"
    returnPolicy = "30 day return policy"
    termsAndServiceAgreement = $true
    businessDescription = "Traditional handloom products from Varanasi"
    profilePictureUrl = "https://res.cloudinary.com/lazerous/image/upload/v1634195883/pmfme-logo-website_y2qqqy.png"
    websiteUrl = "https://testshop.example.com"
    operatingHours = "9 AM - 8 PM"
    deliveryAvailable = $true
    deliveryRadiusInKm = 50
    deliveryCharges = 50.00
}

$script:vendorId = ""
$script:jwtToken = ""
$script:existingVendorEmail = ""

# ============================================================================
# SECTION 1: VENDOR REGISTRATION TESTS
# ============================================================================
Write-TestHeader "SECTION 1: VENDOR REGISTRATION TESTS"

# Test 1.1: Register new vendor with complete data
Write-Host "`n[Test 1.1] Register New Vendor with Complete Data" -ForegroundColor White
$response = Invoke-SafeRequest -Method POST -Uri "$BASE_URL/vendor/create_account" -Body $testVendor
if ($response.vendorId) {
    $script:vendorId = $response.vendorId
    Write-Pass "Register new vendor with complete data"
    Write-Host "       Vendor ID: $($script:vendorId)" -ForegroundColor Gray
} elseif ($response.error) {
    Write-Fail "Register new vendor with complete data" $response.message
} else {
    Write-Fail "Register new vendor with complete data" "No vendor ID returned"
}

# Test 1.2: Vendor data persisted correctly
Write-Host "`n[Test 1.2] Verify Vendor Data Persistence" -ForegroundColor White
if ($script:vendorId) {
    $vendorData = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/get_vendor_id/$($script:vendorId)"
    if ($vendorData.shoppeeName -eq $testVendor.shoppeeName) {
        Write-Pass "Vendor shop name persisted correctly"
    } else {
        Write-Fail "Vendor shop name persisted correctly" "Expected: $($testVendor.shoppeeName), Got: $($vendorData.shoppeeName)"
    }
    
    if ($vendorData.emailAddress -eq $testVendor.emailAddress) {
        Write-Pass "Vendor email persisted correctly"
    } else {
        Write-Fail "Vendor email persisted correctly" "Expected: $($testVendor.emailAddress), Got: $($vendorData.emailAddress)"
    }
    
    if ($vendorData.locationState -eq $testVendor.locationState) {
        Write-Pass "Vendor location state persisted correctly"
    } else {
        Write-Fail "Vendor location state persisted correctly" "Expected: $($testVendor.locationState), Got: $($vendorData.locationState)"
    }
    
    if ($vendorData.locationDistrict -eq $testVendor.locationDistrict) {
        Write-Pass "Vendor location district persisted correctly"
    } else {
        Write-Fail "Vendor location district persisted correctly" "Expected: $($testVendor.locationDistrict), Got: $($vendorData.locationDistrict)"
    }
} else {
    Write-Skip "Vendor data persistence tests" "No vendor ID available"
}

# Test 1.3: Duplicate email registration should fail
Write-Host "`n[Test 1.3] Duplicate Email Registration Prevention" -ForegroundColor White
$duplicateVendor = $testVendor.Clone()
$duplicateVendor.shoppeeName = "Duplicate Shop"
$response = Invoke-SafeRequest -Method POST -Uri "$BASE_URL/vendor/create_account" -Body $duplicateVendor
if ($response.error -or !$response.vendorId -or ($response.vendorId -eq $script:vendorId)) {
    # Either error or same vendor returned - depends on implementation
    Write-Pass "Duplicate email handling (Error or returns existing)"
    Write-Host "       Response: $($response.message ?? 'Same vendor or handled gracefully')" -ForegroundColor Gray
} else {
    Write-Fail "Duplicate email handling" "Allowed creating duplicate vendor"
}

# ============================================================================
# SECTION 2: VENDOR LOGIN TESTS
# ============================================================================
Write-TestHeader "SECTION 2: VENDOR LOGIN TESTS"

# Test 2.1: Login with correct credentials
Write-Host "`n[Test 2.1] Login with Correct Credentials" -ForegroundColor White
$loginBody = @{
    username = $testVendor.emailAddress
    password = $testVendor.password
    role = "vendor"
}
$loginResponse = Invoke-SafeRequest -Method POST -Uri $AUTH_URL -Body $loginBody
if ($loginResponse.jwt) {
    $script:jwtToken = $loginResponse.jwt
    Write-Pass "Vendor login returns JWT token"
    Write-Host "       Token received (length): $($script:jwtToken.Length)" -ForegroundColor Gray
} elseif ($loginResponse.error) {
    Write-Fail "Vendor login returns JWT token" $loginResponse.message
} else {
    Write-Fail "Vendor login returns JWT token" "No token returned"
}

# Test 2.2: Login returns vendor data
Write-Host "`n[Test 2.2] Login Returns Vendor Data" -ForegroundColor White
if ($loginResponse.user) {
    $user = $loginResponse.user
    if ($user.vendorId -or $user.shoppeeName) {
        Write-Pass "Login returns vendor data object"
        $script:vendorId = $user.vendorId ?? $script:vendorId
        Write-Host "       Shop Name: $($user.shoppeeName)" -ForegroundColor Gray
    } else {
        Write-Fail "Login returns vendor data object" "Vendor data incomplete"
    }
} elseif ($loginResponse.vendorId) {
    # Alternate response structure
    Write-Pass "Login returns vendor data (alt structure)"
    $script:vendorId = $loginResponse.vendorId
} else {
    Write-Fail "Login returns vendor data object" "No vendor data in response"
}

# Test 2.3: Login with wrong credentials should fail
Write-Host "`n[Test 2.3] Login with Wrong Password Fails" -ForegroundColor White
$wrongLogin = @{
    username = $testVendor.emailAddress
    password = "WrongPassword123"
    role = "vendor"
}
$wrongResponse = Invoke-SafeRequest -Method POST -Uri $AUTH_URL -Body $wrongLogin
if ($wrongResponse.error -or !$wrongResponse.jwt) {
    Write-Pass "Invalid credentials rejected"
} else {
    Write-Fail "Invalid credentials rejected" "Login succeeded with wrong password"
}

# Test 2.4: Try existing vendor from test data
Write-Host "`n[Test 2.4] Login with Existing Test Vendor" -ForegroundColor White
$existingVendors = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/get_all_vendors"
if ($existingVendors -and $existingVendors.Count -gt 0) {
    $testExistingVendor = $existingVendors | Where-Object { $_.emailAddress -match "vendor" } | Select-Object -First 1
    if ($testExistingVendor) {
        $script:existingVendorEmail = $testExistingVendor.emailAddress
        Write-Pass "Found existing vendor for testing"
        Write-Host "       Email: $($script:existingVendorEmail)" -ForegroundColor Gray
        Write-Host "       Vendor ID: $($testExistingVendor.vendorId)" -ForegroundColor Gray
    } else {
        Write-Skip "Login with existing vendor" "No vendor with 'vendor' in email"
    }
} else {
    Write-Skip "Login with existing vendor" "No vendors in system"
}

# ============================================================================
# SECTION 3: VENDOR DASHBOARD & DATA TESTS
# ============================================================================
Write-TestHeader "SECTION 3: VENDOR DASHBOARD & DATA TESTS"

# Ensure we have a valid JWT for authenticated requests
$authHeaders = @{}
if ($script:jwtToken) {
    $authHeaders["Authorization"] = "Bearer $($script:jwtToken)"
}

# Test 3.1: Get vendor by ID
Write-Host "`n[Test 3.1] Get Vendor by ID" -ForegroundColor White
if ($script:vendorId) {
    $vendorDetails = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/get_vendor_id/$($script:vendorId)" -Headers $authHeaders
    if ($vendorDetails.vendorId -eq $script:vendorId) {
        Write-Pass "Get vendor by ID returns correct data"
        
        # Validate all fields
        $fieldsToCheck = @(
            @{ Field = "shoppeeName"; Expected = $testVendor.shoppeeName },
            @{ Field = "shopkeeperName"; Expected = $testVendor.shopkeeperName },
            @{ Field = "contactNumber"; Expected = $testVendor.contactNumber },
            @{ Field = "pinCode"; Expected = $testVendor.pinCode }
        )
        
        foreach ($check in $fieldsToCheck) {
            if ($vendorDetails.($check.Field) -eq $check.Expected) {
                Write-Pass "Vendor $($check.Field) correct"
            } else {
                Write-Fail "Vendor $($check.Field) correct" "Expected: $($check.Expected), Got: $($vendorDetails.($check.Field))"
            }
        }
    } else {
        Write-Fail "Get vendor by ID returns correct data" "Vendor ID mismatch"
    }
} else {
    Write-Skip "Get vendor by ID" "No vendor ID available"
}

# Test 3.2: Get all vendors (for vendor directory)
Write-Host "`n[Test 3.2] Get All Vendors API" -ForegroundColor White
$allVendors = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/get_all_vendors"
if ($allVendors -and $allVendors.Count -ge 1) {
    Write-Pass "Get all vendors API working"
    Write-Host "       Total vendors: $($allVendors.Count)" -ForegroundColor Gray
    
    # Check if our test vendor is in the list
    $ourVendor = $allVendors | Where-Object { $_.vendorId -eq $script:vendorId }
    if ($ourVendor) {
        Write-Pass "Test vendor found in all vendors list"
    } else {
        Write-Fail "Test vendor found in all vendors list" "Not found"
    }
} elseif ($allVendors.error) {
    Write-Fail "Get all vendors API working" $allVendors.message
} else {
    Write-Fail "Get all vendors API working" "Empty or invalid response"
}

# Test 3.3: Search vendors by state
Write-Host "`n[Test 3.3] Search Vendors by State" -ForegroundColor White
$stateVendors = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/search_by_state?state=$($testVendor.locationState)"
if ($stateVendors -and !$stateVendors.error) {
    Write-Pass "Search vendors by state API working"
    Write-Host "       Vendors in $($testVendor.locationState): $($stateVendors.Count)" -ForegroundColor Gray
} else {
    Write-Fail "Search vendors by state API working" $stateVendors.message
}

# Test 3.4: Search vendors by location
Write-Host "`n[Test 3.4] Search Vendors by Location (District + State)" -ForegroundColor White
$locationVendors = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/search_by_location?district=$($testVendor.locationDistrict)&state=$($testVendor.locationState)"
if ($locationVendors -and !$locationVendors.error) {
    Write-Pass "Search vendors by location API working"
    Write-Host "       Vendors in $($testVendor.locationDistrict): $($locationVendors.Count)" -ForegroundColor Gray
} else {
    Write-Fail "Search vendors by location API working" ($locationVendors.message ?? "Empty response")
}

# ============================================================================
# SECTION 4: PRODUCT MANAGEMENT TESTS
# ============================================================================
Write-TestHeader "SECTION 4: PRODUCT MANAGEMENT TESTS"

# Test 4.1: Get categories for product creation
Write-Host "`n[Test 4.1] Get Product Categories" -ForegroundColor White
$categories = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/productCategory/get_all_categories"
if ($categories -and $categories.Count -gt 0) {
    Write-Pass "Get product categories API working"
    Write-Host "       Total categories: $($categories.Count)" -ForegroundColor Gray
    $script:testCategoryId = $categories[0].prodCategoryId
    Write-Host "       Test Category: $($categories[0].categoryName)" -ForegroundColor Gray
} else {
    # Try alternate endpoint
    $categories = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/productCategory/get_all_categorie"
    if ($categories -and $categories.Count -gt 0) {
        Write-Pass "Get product categories API working (legacy endpoint)"
        $script:testCategoryId = $categories[0].prodCategoryId
    } else {
        Write-Fail "Get product categories API working" "No categories returned"
        $script:testCategoryId = $null
    }
}

# Test 4.2: Get vendor products
Write-Host "`n[Test 4.2] Get Vendor Products" -ForegroundColor White
if ($script:vendorId) {
    $vendorProducts = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/product/get_product_by_vendor_id/$($script:vendorId)"
    if (!$vendorProducts.error) {
        Write-Pass "Get vendor products API working"
        $productCount = if ($vendorProducts) { $vendorProducts.Count } else { 0 }
        Write-Host "       Products for vendor: $productCount" -ForegroundColor Gray
    } else {
        Write-Fail "Get vendor products API working" $vendorProducts.message
    }
} else {
    Write-Skip "Get vendor products" "No vendor ID available"
}

# Test 4.3: Get vendor products (paginated)
Write-Host "`n[Test 4.3] Get Vendor Products Paginated" -ForegroundColor White
if ($script:vendorId) {
    $vendorProductsPaged = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/product/vendor/$($script:vendorId)/paginated?page=0&size=10"
    if (!$vendorProductsPaged.error) {
        Write-Pass "Get vendor products paginated API working"
        Write-Host "       Page size: $($vendorProductsPaged.size ?? 10)" -ForegroundColor Gray
    } else {
        Write-Fail "Get vendor products paginated API working" $vendorProductsPaged.message
    }
} else {
    Write-Skip "Get vendor products paginated" "No vendor ID available"
}

# Test 4.4: Create new product (simulated data)
Write-Host "`n[Test 4.4] Create New Product API" -ForegroundColor White
if ($script:vendorId -and $script:testCategoryId) {
    $newProduct = @{
        productName = "Test Handloom Product $timestamp"
        productDescription = "Beautiful handcrafted traditional item created for testing"
        productPrice = 1500
        productDiscountPrice = 1200
        productQuantity = 50
        productRating = 0
        productTotalRating = 0
        productImageUrl = "https://res.cloudinary.com/lazerous/image/upload/v1634195883/pmfme-logo-website_y2qqqy.png"
        vendorId = $script:vendorId
        categoryId = $script:testCategoryId
        productWeight = "500g"
        productDimensions = "20x15x10 cm"
        productMaterial = "Cotton"
        productOrigin = "Varanasi"
        productCareInstructions = "Hand wash only"
        isActive = $true
    }
    
    # Try multipart form with JSON (since API expects FormData)
    try {
        $formFields = @{
            product = ($newProduct | ConvertTo-Json)
        }
        
        $response = Invoke-RestMethod -Method POST -Uri "$BASE_URL/product/save_product" -Body ($newProduct | ConvertTo-Json) -ContentType "application/json" -Headers $authHeaders -ErrorAction Stop
        
        if ($response.productId) {
            $script:testProductId = $response.productId
            Write-Pass "Create new product API working"
            Write-Host "       Product ID: $($script:testProductId)" -ForegroundColor Gray
        } else {
            Write-Fail "Create new product API working" "No product ID returned"
        }
    } catch {
        # API might require file upload - mark as needs manual test
        Write-Fail "Create new product API" "API may require file upload: $($_.Exception.Message)"
    }
} else {
    Write-Skip "Create new product" "Missing vendor ID or category ID"
}

# Test 4.5: Get all products (to verify product exists)
Write-Host "`n[Test 4.5] Get All Products API" -ForegroundColor White
$allProducts = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/product/get_all_products"
if ($allProducts -and $allProducts.Count -gt 0) {
    Write-Pass "Get all products API working"
    Write-Host "       Total products: $($allProducts.Count)" -ForegroundColor Gray
    
    # Get a product with our vendor ID or first available
    $testProduct = $allProducts | Where-Object { $_.vendorId -eq $script:vendorId } | Select-Object -First 1
    if (!$testProduct) {
        $testProduct = $allProducts | Select-Object -First 1
    }
    if ($testProduct) {
        $script:existingProductId = $testProduct.productId
        Write-Host "       Test Product: $($testProduct.productName)" -ForegroundColor Gray
    }
} else {
    Write-Fail "Get all products API working" "No products returned"
}

# Test 4.6: Update product
Write-Host "`n[Test 4.6] Update Product API" -ForegroundColor White
if ($script:existingProductId) {
    $updateData = @{
        productId = $script:existingProductId
        productName = "Updated Product Name $timestamp"
        productPrice = 2000
    }
    $updateResponse = Invoke-SafeRequest -Method PUT -Uri "$BASE_URL/product/update_product_by_id/$($script:existingProductId)" -Body $updateData -Headers $authHeaders
    if (!$updateResponse.error) {
        Write-Pass "Update product API working"
    } else {
        Write-Fail "Update product API working" $updateResponse.message
    }
} else {
    Write-Skip "Update product" "No product ID available"
}

# ============================================================================
# SECTION 5: ORDER MANAGEMENT TESTS
# ============================================================================
Write-TestHeader "SECTION 5: ORDER MANAGEMENT TESTS"

# Test 5.1: Get orders by vendor ID
Write-Host "`n[Test 5.1] Get Vendor Orders" -ForegroundColor White
if ($script:vendorId) {
    $vendorOrders = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/order/vendor/$($script:vendorId)" -Headers $authHeaders
    if (!$vendorOrders.error) {
        Write-Pass "Get vendor orders API working"
        $orderCount = if ($vendorOrders) { $vendorOrders.Count } else { 0 }
        Write-Host "       Orders for vendor: $orderCount" -ForegroundColor Gray
        
        if ($orderCount -gt 0) {
            $script:testOrderId = $vendorOrders[0].orderId
            Write-Host "       Test Order ID: $($script:testOrderId)" -ForegroundColor Gray
        }
    } else {
        Write-Fail "Get vendor orders API working" $vendorOrders.message
    }
} else {
    Write-Skip "Get vendor orders" "No vendor ID available"
}

# Test 5.2: Get all orders (for testing)
Write-Host "`n[Test 5.2] Get All Orders API" -ForegroundColor White
$allOrders = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/order/all" -Headers $authHeaders
if (!$allOrders.error) {
    Write-Pass "Get all orders API working"
    $orderCount = if ($allOrders) { $allOrders.Count } else { 0 }
    Write-Host "       Total orders: $orderCount" -ForegroundColor Gray
    
    if ($orderCount -gt 0 -and !$script:testOrderId) {
        $script:testOrderId = $allOrders[0].orderId
    }
} else {
    Write-Fail "Get all orders API working" $allOrders.message
}

# Test 5.3: Get order by ID
Write-Host "`n[Test 5.3] Get Order by ID" -ForegroundColor White
if ($script:testOrderId) {
    $orderDetails = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/order/get/$($script:testOrderId)" -Headers $authHeaders
    if ($orderDetails.orderId) {
        Write-Pass "Get order by ID API working"
        Write-Host "       Order Status: $($orderDetails.orderStatus)" -ForegroundColor Gray
        Write-Host "       Payment Status: $($orderDetails.paymentStatus)" -ForegroundColor Gray
    } else {
        Write-Fail "Get order by ID API working" "No order data returned"
    }
} else {
    Write-Skip "Get order by ID" "No order ID available"
}

# Test 5.4: Update order status
Write-Host "`n[Test 5.4] Update Order Status API" -ForegroundColor White
if ($script:testOrderId) {
    $statusUpdate = @{ status = "PROCESSING" }
    $updateResult = Invoke-SafeRequest -Method PATCH -Uri "$BASE_URL/order/update-status/$($script:testOrderId)" -Body $statusUpdate -Headers $authHeaders
    if (!$updateResult.error) {
        Write-Pass "Update order status API working"
    } else {
        Write-Fail "Update order status API working" $updateResult.message
    }
} else {
    Write-Skip "Update order status" "No order ID available"
}

# Test 5.5: Get orders by status
Write-Host "`n[Test 5.5] Get Orders by Status" -ForegroundColor White
$pendingOrders = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/order/status/PENDING" -Headers $authHeaders
if (!$pendingOrders.error) {
    Write-Pass "Get orders by status API working"
    $orderCount = if ($pendingOrders) { $pendingOrders.Count } else { 0 }
    Write-Host "       Pending orders: $orderCount" -ForegroundColor Gray
} else {
    Write-Fail "Get orders by status API working" $pendingOrders.message
}

# ============================================================================
# SECTION 6: PROFILE MANAGEMENT TESTS
# ============================================================================
Write-TestHeader "SECTION 6: PROFILE MANAGEMENT TESTS"

# Test 6.1: Update vendor profile
Write-Host "`n[Test 6.1] Update Vendor Profile" -ForegroundColor White
if ($script:vendorId) {
    $updatedProfile = $testVendor.Clone()
    $updatedProfile.vendorId = $script:vendorId
    $updatedProfile.shoppeeName = "Updated Shop Name $timestamp"
    $updatedProfile.businessDescription = "Updated description for testing"
    
    $updateResult = Invoke-SafeRequest -Method PUT -Uri "$BASE_URL/vendor/update_vendor_by_id/$($script:vendorId)" -Body $updatedProfile -Headers $authHeaders
    if ($updateResult.vendorId) {
        Write-Pass "Update vendor profile API working"
        
        # Verify update persisted
        $verifyVendor = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/get_vendor_id/$($script:vendorId)"
        if ($verifyVendor.shoppeeName -eq $updatedProfile.shoppeeName) {
            Write-Pass "Profile update persisted correctly"
        } else {
            Write-Fail "Profile update persisted correctly" "Shop name not updated"
        }
    } elseif ($updateResult.error) {
        Write-Fail "Update vendor profile API working" $updateResult.message
    } else {
        Write-Fail "Update vendor profile API working" "Unexpected response"
    }
} else {
    Write-Skip "Update vendor profile" "No vendor ID available"
}

# Test 6.2: Update vendor status
Write-Host "`n[Test 6.2] Update Vendor Status API" -ForegroundColor White
if ($script:vendorId) {
    $statusBody = @{ status = "ACTIVE" }
    $statusResult = Invoke-SafeRequest -Method PATCH -Uri "$BASE_URL/vendor/update_status/$($script:vendorId)" -Body $statusBody -Headers $authHeaders
    if (!$statusResult.error) {
        Write-Pass "Update vendor status API working"
    } else {
        Write-Fail "Update vendor status API working" $statusResult.message
    }
} else {
    Write-Skip "Update vendor status" "No vendor ID available"
}

# ============================================================================
# SECTION 7: VENDOR SHOP PAGE TESTS (Public APIs)
# ============================================================================
Write-TestHeader "SECTION 7: VENDOR SHOP PAGE TESTS"

# Test 7.1: Vendor shop page data available
Write-Host "`n[Test 7.1] Vendor Shop Page Data" -ForegroundColor White
if ($script:vendorId) {
    $shopData = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/vendor/get_vendor_id/$($script:vendorId)"
    if ($shopData.vendorId) {
        Write-Pass "Vendor shop data available publicly"
        
        # Check essential shop page fields
        $shopFields = @("shoppeeName", "locationState", "locationDistrict", "businessDescription")
        foreach ($field in $shopFields) {
            if ($shopData.$field) {
                Write-Pass "Shop field '$field' present"
            } else {
                Write-Fail "Shop field '$field' present" "Field is empty"
            }
        }
    } else {
        Write-Fail "Vendor shop data available publicly" "No data returned"
    }
} else {
    Write-Skip "Vendor shop page data" "No vendor ID available"
}

# Test 7.2: Get vendor products for shop page
Write-Host "`n[Test 7.2] Vendor Shop Products" -ForegroundColor White
if ($script:vendorId) {
    $shopProducts = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/product/get_product_by_vendor_id/$($script:vendorId)"
    if (!$shopProducts.error) {
        Write-Pass "Vendor shop products API working"
    } else {
        Write-Fail "Vendor shop products API working" $shopProducts.message
    }
} else {
    Write-Skip "Vendor shop products" "No vendor ID available"
}

# ============================================================================
# SECTION 8: CART & WISHLIST INTEGRATION
# ============================================================================
Write-TestHeader "SECTION 8: CART & WISHLIST INTEGRATION (Cross-check)"

# Test 8.1: Cart API availability
Write-Host "`n[Test 8.1] Cart API Health Check" -ForegroundColor White
$cartApi = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/cart/get_all_cart" -Headers $authHeaders
if (!$cartApi.error) {
    Write-Pass "Cart API accessible"
    $cartCount = if ($cartApi) { $cartApi.Count } else { 0 }
    Write-Host "       Total cart items: $cartCount" -ForegroundColor Gray
} else {
    Write-Fail "Cart API accessible" $cartApi.message
}

# Test 8.2: Wishlist API availability
Write-Host "`n[Test 8.2] Wishlist API Health Check" -ForegroundColor White
$wishlistApi = Invoke-SafeRequest -Method GET -Uri "$BASE_URL/wishlist/customer/test-customer-id" -Headers $authHeaders
if (!$wishlistApi.error) {
    Write-Pass "Wishlist API accessible"
} else {
    # 404 is acceptable if no customer exists
    if ($wishlistApi.statusCode -eq 404) {
        Write-Pass "Wishlist API accessible (404 for invalid customer expected)"
    } else {
        Write-Fail "Wishlist API accessible" $wishlistApi.message
    }
}

# ============================================================================
# FINAL REPORT
# ============================================================================
Write-TestHeader "TEST EXECUTION SUMMARY"

$passRate = if ($script:totalTests -gt 0) { [math]::Round(($script:passedTests / $script:totalTests) * 100, 1) } else { 0 }

Write-Host "`nTotal Tests Executed: $($script:totalTests)" -ForegroundColor White
Write-Host "Tests Passed: $($script:passedTests)" -ForegroundColor Green
Write-Host "Tests Failed: $($script:failedTests)" -ForegroundColor $(if ($script:failedTests -gt 0) { "Red" } else { "Green" })
Write-Host "Pass Rate: $passRate%" -ForegroundColor $(if ($passRate -ge 80) { "Green" } elseif ($passRate -ge 60) { "Yellow" } else { "Red" })

# List failed tests
if ($script:failedTests -gt 0) {
    Write-Host "`n=== FAILED TESTS ===" -ForegroundColor Red
    $script:testResults | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  - $($_.Name)" -ForegroundColor Red
        if ($_.Details) {
            Write-Host "    Reason: $($_.Details)" -ForegroundColor Yellow
        }
    }
}

# Test data for cleanup reference
Write-Host "`n=== TEST DATA CREATED ===" -ForegroundColor Cyan
Write-Host "  Vendor ID: $($script:vendorId)" -ForegroundColor Gray
Write-Host "  Vendor Email: $($testVendor.emailAddress)" -ForegroundColor Gray
Write-Host "  Product ID: $($script:testProductId ?? 'None')" -ForegroundColor Gray

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "VENDOR E2E TEST COMPLETED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Return results object
return @{
    TotalTests = $script:totalTests
    Passed = $script:passedTests
    Failed = $script:failedTests
    PassRate = $passRate
    TestResults = $script:testResults
    VendorId = $script:vendorId
    VendorEmail = $testVendor.emailAddress
}
