package com.odop.root.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.odop.root.dto.CreatePaymentRequest;
import com.odop.root.dto.PaymentResponse;
import com.odop.root.dto.PaymentVerificationRequest;
import com.odop.root.dto.RefundRequest;
import com.odop.root.models.Payment;
import com.odop.root.models.PaymentStatus;
import com.odop.root.services.PaymentService;

/**
 * REST Controller for Razorpay Payment Gateway operations.
 * 
 * FLOW:
 * 1. Frontend calls /create-order with amount and customer details
 * 2. Backend returns razorpayOrderId and razorpayKeyId
 * 3. Frontend uses these to open Razorpay checkout popup
 * 4. After payment, Razorpay returns razorpay_payment_id and razorpay_signature
 * 5. Frontend calls /verify with these values
 * 6. Backend verifies signature and marks payment as success
 * 
 * TEST MODE:
 * - Use test card: 4111 1111 1111 1111 (any CVV, any future expiry)
 * - Use test UPI: success@razorpay (auto-success)
 */
@RestController
@RequestMapping("odop/payment")
@CrossOrigin
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * Create a new payment order.
     * Returns razorpayOrderId which frontend uses for checkout.
     * 
     * POST /odop/payment/create-order
     * Body: { amount, currency, customerId, customerName, customerEmail, customerPhone, orderId (optional) }
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createPaymentOrder(@RequestBody CreatePaymentRequest request) {
        logger.info("Creating payment order for amount: {} {}", request.getAmount(), request.getCurrency());
        
        // Validate amount
        if (request.getAmount() <= 0) {
            PaymentResponse error = PaymentResponse.error("Amount must be greater than 0");
            return ResponseEntity.badRequest().body(error);
        }
        
        PaymentResponse response = paymentService.createPaymentOrder(request);
        
        if (response.getStatus() == PaymentStatus.CREATED) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify payment after Razorpay checkout completion.
     * MUST be called after payment to confirm it's genuine.
     * 
     * POST /odop/payment/verify
     * Body: { razorpayOrderId, razorpayPaymentId, razorpaySignature }
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        logger.info("Verifying payment for order: {}", request.getRazorpayOrderId());
        
        // Validate required fields
        if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null 
                || request.getRazorpaySignature() == null) {
            PaymentResponse error = PaymentResponse.error("Missing required fields: razorpayOrderId, razorpayPaymentId, razorpaySignature");
            return ResponseEntity.badRequest().body(error);
        }
        
        PaymentResponse response = paymentService.verifyPayment(request);
        
        if (response.isVerified()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get payment status by internal payment ID.
     * 
     * GET /odop/payment/status/{paymentId}
     */
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentId) {
        logger.info("Getting payment status for: {}", paymentId);
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);
        
        if (response.getStatus() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by Razorpay order ID.
     * 
     * GET /odop/payment/by-razorpay-order/{razorpayOrderId}
     */
    @GetMapping("/by-razorpay-order/{razorpayOrderId}")
    public ResponseEntity<PaymentResponse> getPaymentByRazorpayOrderId(@PathVariable String razorpayOrderId) {
        logger.info("Getting payment by Razorpay order ID: {}", razorpayOrderId);
        PaymentResponse response = paymentService.getPaymentByRazorpayOrderId(razorpayOrderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by order ID.
     * 
     * GET /odop/payment/by-order/{orderId}
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable String orderId) {
        logger.info("Getting payment by order ID: {}", orderId);
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payments for a customer.
     * 
     * GET /odop/payment/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Payment>> getCustomerPayments(@PathVariable String customerId) {
        logger.info("Getting payments for customer: {}", customerId);
        List<Payment> payments = paymentService.getCustomerPayments(customerId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all payments for a vendor.
     * 
     * GET /odop/payment/vendor/{vendorId}
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Payment>> getVendorPayments(@PathVariable String vendorId) {
        logger.info("Getting payments for vendor: {}", vendorId);
        List<Payment> payments = paymentService.getVendorPayments(vendorId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Process a refund.
     * 
     * POST /odop/payment/refund
     * Body: { paymentId OR razorpayPaymentId, refundAmount (optional - 0 for full), reason }
     */
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> processRefund(@RequestBody RefundRequest request) {
        logger.info("Processing refund for payment: {}", 
            request.getPaymentId() != null ? request.getPaymentId() : request.getRazorpayPaymentId());
        
        if (request.getPaymentId() == null && request.getRazorpayPaymentId() == null) {
            PaymentResponse error = PaymentResponse.error("Either paymentId or razorpayPaymentId is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        PaymentResponse response = paymentService.processRefund(request);
        
        if (response.getStatus() == PaymentStatus.REFUNDED) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Handle payment failure (for webhook or manual reporting).
     * 
     * POST /odop/payment/failed
     */
    @PostMapping("/failed")
    public ResponseEntity<PaymentResponse> handlePaymentFailed(
            @RequestParam String razorpayPaymentId,
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorDescription) {
        
        logger.info("Marking payment as failed: {}", razorpayPaymentId);
        PaymentResponse response = paymentService.markPaymentFailed(
            razorpayPaymentId, 
            errorCode != null ? errorCode : "PAYMENT_FAILED",
            errorDescription != null ? errorDescription : "Payment failed"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for payment service.
     * 
     * GET /odop/payment/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment service is running. Razorpay integration active.");
    }
}
