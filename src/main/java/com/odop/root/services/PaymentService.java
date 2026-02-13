package com.odop.root.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.odop.root.dto.CreatePaymentRequest;
import com.odop.root.dto.PaymentResponse;
import com.odop.root.dto.PaymentVerificationRequest;
import com.odop.root.dto.RefundRequest;
import com.odop.root.models.Order;
import com.odop.root.models.Payment;
import com.odop.root.models.PaymentStatus;
import com.odop.root.repository.OrderRepository;
import com.odop.root.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;

/**
 * Service for handling Razorpay payment operations.
 * Supports order creation, payment verification, refunds, and status tracking.
 */
@Service
public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    /**
     * Creates a Razorpay order for payment.
     * Frontend uses the returned order_id to initiate checkout.
     */
    public PaymentResponse createPaymentOrder(CreatePaymentRequest request) {
        try {
            logger.info("Creating Razorpay order for amount: {} {}", request.getAmount(), request.getCurrency());

            // Convert amount to paise (Razorpay expects amount in smallest currency unit)
            int amountInPaise = (int) (request.getAmount() * 100);

            // Generate unique receipt number
            String receiptNumber = "ODOP_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            orderRequest.put("receipt", receiptNumber);
            
            // Add notes
            JSONObject notes = new JSONObject();
            notes.put("orderId", request.getOrderId() != null ? request.getOrderId() : "");
            notes.put("customerId", request.getCustomerId() != null ? request.getCustomerId() : "");
            notes.put("vendorId", request.getVendorId() != null ? request.getVendorId() : "");
            notes.put("description", request.getDescription() != null ? request.getDescription() : "ODOP Purchase");
            orderRequest.put("notes", notes);

            // Create order via Razorpay API
            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            logger.info("Razorpay order created: {}", razorpayOrderId);

            // Save payment record in database
            Payment payment = new Payment();
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setOrderId(request.getOrderId());
            payment.setCustomerId(request.getCustomerId());
            payment.setVendorId(request.getVendorId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
            payment.setCustomerName(request.getCustomerName());
            payment.setCustomerEmail(request.getCustomerEmail());
            payment.setCustomerPhone(request.getCustomerPhone());
            payment.setDescription(request.getDescription());
            payment.setReceiptNumber(receiptNumber);
            payment.setNotes(request.getNotes());
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            // Build response
            PaymentResponse response = new PaymentResponse();
            response.setPaymentId(savedPayment.getPaymentId());
            response.setOrderId(request.getOrderId());
            response.setRazorpayOrderId(razorpayOrderId);
            response.setAmount(request.getAmount());
            response.setAmountInPaise(amountInPaise);
            response.setCurrency(payment.getCurrency());
            response.setStatus(PaymentStatus.CREATED);
            response.setStatusMessage("Payment order created successfully. Proceed to checkout.");
            response.setRazorpayKeyId(razorpayKeyId);  // Frontend needs this for checkout
            response.setCustomerName(request.getCustomerName());
            response.setCustomerEmail(request.getCustomerEmail());
            response.setCustomerPhone(request.getCustomerPhone());
            response.setReceiptNumber(receiptNumber);
            response.setDescription(request.getDescription());
            response.setCreatedAt(savedPayment.getCreatedAt());

            return response;

        } catch (RazorpayException e) {
            logger.error("Razorpay error creating order: {}", e.getMessage());
            PaymentResponse errorResponse = PaymentResponse.error("Failed to create payment order: " + e.getMessage());
            errorResponse.setErrorCode("RAZORPAY_ERROR");
            errorResponse.setErrorDescription(e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            logger.error("Error creating payment order: {}", e.getMessage(), e);
            return PaymentResponse.error("Failed to create payment order: " + e.getMessage());
        }
    }

    /**
     * Verifies payment signature after successful payment.
     * This MUST be called after Razorpay checkout to ensure payment is genuine.
     */
    public PaymentResponse verifyPayment(PaymentVerificationRequest request) {
        try {
            logger.info("Verifying payment - Order: {}, Payment: {}", 
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());

            // Find payment record
            Optional<Payment> optPayment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId());
            if (optPayment.isEmpty()) {
                logger.error("Payment record not found for order: {}", request.getRazorpayOrderId());
                return PaymentResponse.error("Payment record not found");
            }

            Payment payment = optPayment.get();

            // Verify signature
            String generatedSignature = generateSignature(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                razorpayKeySecret
            );

            boolean isValidSignature = generatedSignature.equals(request.getRazorpaySignature());

            if (isValidSignature) {
                // Payment verified successfully
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Update order status if orderId exists
                if (payment.getOrderId() != null && !payment.getOrderId().isEmpty()) {
                    updateOrderPaymentStatus(payment.getOrderId(), "PAID", request.getRazorpayPaymentId());
                }

                logger.info("Payment verified successfully: {}", request.getRazorpayPaymentId());

                PaymentResponse response = PaymentResponse.success("Payment verified successfully!");
                response.setPaymentId(payment.getPaymentId());
                response.setOrderId(payment.getOrderId());
                response.setRazorpayOrderId(request.getRazorpayOrderId());
                response.setRazorpayPaymentId(request.getRazorpayPaymentId());
                response.setAmount(payment.getAmount());
                response.setVerified(true);
                response.setVerificationMessage("Signature verified. Payment is genuine.");
                response.setCompletedAt(payment.getCompletedAt());
                return response;

            } else {
                // Signature mismatch - possible fraud
                logger.error("Payment signature verification failed for order: {}", request.getRazorpayOrderId());
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode("SIGNATURE_MISMATCH");
                payment.setErrorDescription("Payment signature verification failed");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                PaymentResponse response = PaymentResponse.error("Payment verification failed. Signature mismatch.");
                response.setVerified(false);
                response.setVerificationMessage("Signature verification failed. This payment might be fraudulent.");
                return response;
            }

        } catch (Exception e) {
            logger.error("Error verifying payment: {}", e.getMessage(), e);
            return PaymentResponse.error("Payment verification error: " + e.getMessage());
        }
    }

    /**
     * Process a refund for a payment.
     */
    public PaymentResponse processRefund(RefundRequest request) {
        try {
            // Find payment record
            Payment payment = null;
            if (request.getPaymentId() != null) {
                payment = paymentRepository.findById(request.getPaymentId()).orElse(null);
            } else if (request.getRazorpayPaymentId() != null) {
                payment = paymentRepository.findByRazorpayPaymentId(request.getRazorpayPaymentId()).orElse(null);
            }

            if (payment == null) {
                return PaymentResponse.error("Payment record not found");
            }

            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                return PaymentResponse.error("Can only refund successful payments. Current status: " + payment.getStatus());
            }

            // Calculate refund amount
            double refundAmount = request.getRefundAmount() > 0 ? request.getRefundAmount() : payment.getAmount();
            int refundAmountInPaise = (int) (refundAmount * 100);

            // Create refund via Razorpay API
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", refundAmountInPaise);
            refundRequest.put("speed", request.getSpeed() != null ? request.getSpeed() : "normal");
            
            JSONObject notes = new JSONObject();
            notes.put("reason", request.getReason() != null ? request.getReason() : "Customer requested refund");
            refundRequest.put("notes", notes);

            Refund refund = razorpayClient.payments.refund(payment.getRazorpayPaymentId(), refundRequest);
            String refundId = refund.get("id");

            logger.info("Refund processed: {} for payment: {}", refundId, payment.getRazorpayPaymentId());

            // Update payment record
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundId(refundId);
            payment.setRefundAmount(refundAmount);
            payment.setRefundReason(request.getReason());
            payment.setRefundedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Update order status
            if (payment.getOrderId() != null) {
                updateOrderPaymentStatus(payment.getOrderId(), "REFUNDED", null);
            }

            PaymentResponse response = PaymentResponse.success("Refund processed successfully");
            response.setPaymentId(payment.getPaymentId());
            response.setRefundId(refundId);
            response.setRefundAmount(refundAmount);
            response.setRefundReason(request.getReason());
            response.setStatus(PaymentStatus.REFUNDED);
            return response;

        } catch (RazorpayException e) {
            logger.error("Razorpay error processing refund: {}", e.getMessage());
            return PaymentResponse.error("Refund failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing refund: {}", e.getMessage(), e);
            return PaymentResponse.error("Refund error: " + e.getMessage());
        }
    }

    /**
     * Get payment status by payment ID.
     */
    public PaymentResponse getPaymentStatus(String paymentId) {
        Optional<Payment> optPayment = paymentRepository.findById(paymentId);
        if (optPayment.isEmpty()) {
            return PaymentResponse.error("Payment not found");
        }

        Payment payment = optPayment.get();
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrderId());
        response.setRazorpayOrderId(payment.getRazorpayOrderId());
        response.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setStatusMessage("Payment status: " + payment.getStatus());
        response.setCustomerName(payment.getCustomerName());
        response.setCustomerEmail(payment.getCustomerEmail());
        response.setReceiptNumber(payment.getReceiptNumber());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        
        if (payment.getRefundId() != null) {
            response.setRefundId(payment.getRefundId());
            response.setRefundAmount(payment.getRefundAmount());
            response.setRefundReason(payment.getRefundReason());
        }
        
        if (payment.getErrorCode() != null) {
            response.setErrorCode(payment.getErrorCode());
            response.setErrorDescription(payment.getErrorDescription());
        }

        return response;
    }

    /**
     * Get payment by Razorpay order ID.
     */
    public PaymentResponse getPaymentByRazorpayOrderId(String razorpayOrderId) {
        Optional<Payment> optPayment = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        if (optPayment.isEmpty()) {
            return PaymentResponse.error("Payment not found");
        }
        return getPaymentStatus(optPayment.get().getPaymentId());
    }

    /**
     * Get all payments for a customer.
     */
    public List<Payment> getCustomerPayments(String customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Get all payments for a vendor.
     */
    public List<Payment> getVendorPayments(String vendorId) {
        return paymentRepository.findByVendorId(vendorId);
    }

    /**
     * Get payment by order ID.
     */
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Optional<Payment> optPayment = paymentRepository.findByOrderId(orderId);
        if (optPayment.isEmpty()) {
            return PaymentResponse.error("No payment found for this order");
        }
        return getPaymentStatus(optPayment.get().getPaymentId());
    }

    /**
     * Mark payment as failed (for webhook or manual update).
     */
    public PaymentResponse markPaymentFailed(String razorpayPaymentId, String errorCode, String errorDescription) {
        Optional<Payment> optPayment = paymentRepository.findByRazorpayPaymentId(razorpayPaymentId);
        if (optPayment.isEmpty()) {
            return PaymentResponse.error("Payment not found");
        }

        Payment payment = optPayment.get();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update order status
        if (payment.getOrderId() != null) {
            updateOrderPaymentStatus(payment.getOrderId(), "FAILED", null);
        }

        return PaymentResponse.error("Payment marked as failed: " + errorDescription);
    }

    // ============ HELPER METHODS ============

    /**
     * Generates HMAC-SHA256 signature for verification.
     */
    private String generateSignature(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Updates the order payment status after payment verification.
     */
    private void updateOrderPaymentStatus(String orderId, String paymentStatus, String transactionId) {
        try {
            Optional<Order> optOrder = orderRepository.findById(orderId);
            if (optOrder.isPresent()) {
                Order order = optOrder.get();
                order.setPaymentStatus(paymentStatus);
                if (transactionId != null) {
                    order.setPaymentTransactionId(transactionId);
                }
                if ("PAID".equals(paymentStatus)) {
                    order.setOrderStatus("CONFIRMED");
                }
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                logger.info("Order {} payment status updated to: {}", orderId, paymentStatus);
            }
        } catch (Exception e) {
            logger.error("Error updating order payment status: {}", e.getMessage());
        }
    }
}
