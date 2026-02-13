# Test Razorpay Payment Gateway
# Run this in PowerShell to test the payment endpoints

Write-Host "=== TESTING RAZORPAY PAYMENT GATEWAY ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "1. Health Check:" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:50982/odop/payment/health" -Method GET
    Write-Host "   ✅ $response" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Create Payment Order
Write-Host "2. Create Payment Order (₹100):" -ForegroundColor Yellow
$paymentRequest = @{
    amount = 100
    currency = "INR"
    customerName = "Test Customer"
    customerEmail = "test@example.com"
    customerPhone = "9876543210"
    description = "Test ODOP Payment"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:50982/odop/payment/create-order" -Method POST -Body $paymentRequest -ContentType "application/json"
    Write-Host "   ✅ Payment Order Created!" -ForegroundColor Green
    Write-Host "   Razorpay Order ID: $($response.razorpayOrderId)" -ForegroundColor Cyan
    Write-Host "   Amount: ₹$($response.amount)" -ForegroundColor Cyan
    Write-Host "   Status: $($response.status)" -ForegroundColor Cyan
    Write-Host "   Razorpay Key ID: $($response.razorpayKeyId)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "   Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== TEST COMPLETE ===" -ForegroundColor Cyan
