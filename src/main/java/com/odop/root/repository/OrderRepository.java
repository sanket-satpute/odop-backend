package com.odop.root.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Order findByOrderId(String orderId);

    List<Order> findByCustomerId(String customerId);

    List<Order> findByVendorId(String vendorId);

    List<Order> findByCustomerIdAndOrderStatus(String customerId, String orderStatus);

    List<Order> findByVendorIdAndOrderStatus(String vendorId, String orderStatus);

    List<Order> findByOrderStatus(String orderStatus);

    List<Order> findByPaymentStatus(String paymentStatus);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Order> findByVendorIdOrderByCreatedAtDesc(String vendorId);

    // --- Pagination Support ---
    
    Page<Order> findAll(Pageable pageable);
    
    Page<Order> findByCustomerId(String customerId, Pageable pageable);
    
    Page<Order> findByVendorId(String vendorId, Pageable pageable);
    
    Page<Order> findByOrderStatus(String orderStatus, Pageable pageable);
    
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);
    
    Page<Order> findByCustomerIdAndOrderStatus(String customerId, String orderStatus, Pageable pageable);
    
    Page<Order> findByVendorIdAndOrderStatus(String vendorId, String orderStatus, Pageable pageable);
    
    // --- Date Range Queries for Analytics/Earnings ---
    
    List<Order> findByVendorIdAndCreatedAtBetweenAndOrderStatus(
        String vendorId, LocalDateTime startDate, LocalDateTime endDate, String orderStatus);
    
    List<Order> findByVendorIdAndCreatedAtBetween(
        String vendorId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByVendorIdAndOrderStatusIn(String vendorId, List<String> orderStatuses);
    
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByCreatedAtBetweenAndOrderStatus(
        LocalDateTime startDate, LocalDateTime endDate, String orderStatus);
}
