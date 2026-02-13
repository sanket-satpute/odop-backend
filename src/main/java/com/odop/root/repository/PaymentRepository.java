package com.odop.root.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.Payment;
import com.odop.root.models.PaymentStatus;

/**
 * Repository for Payment entity with Razorpay integration.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    // Find by Razorpay IDs
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    
    // Find by Order
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByOrderIdIn(List<String> orderIds);
    
    // Find by Customer
    List<Payment> findByCustomerId(String customerId);
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Payment> findByCustomerIdAndStatus(String customerId, PaymentStatus status);
    
    // Find by Vendor (for vendor dashboard/payouts)
    List<Payment> findByVendorId(String vendorId);
    List<Payment> findByVendorIdAndStatus(String vendorId, PaymentStatus status);
    
    // Find by Status
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
    
    // Find by date range
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Payment> findByCustomerIdAndCreatedAtBetween(String customerId, LocalDateTime startDate, LocalDateTime endDate);
    List<Payment> findByVendorIdAndCreatedAtBetween(String vendorId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find successful payments
    List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by receipt number
    Optional<Payment> findByReceiptNumber(String receiptNumber);
    
    // Count methods for analytics
    long countByStatus(PaymentStatus status);
    long countByCustomerId(String customerId);
    long countByVendorIdAndStatus(String vendorId, PaymentStatus status);
    
    // Check if exists
    boolean existsByRazorpayOrderId(String razorpayOrderId);
    boolean existsByOrderId(String orderId);
}


