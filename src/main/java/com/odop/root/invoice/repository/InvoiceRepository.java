package com.odop.root.invoice.repository;

import com.odop.root.invoice.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice entity.
 */
@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    
    // Find by invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    // Find by order ID
    Optional<Invoice> findByOrderId(String orderId);
    
    // Find by customer
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    // Find by vendor
    List<Invoice> findByVendorIdOrderByCreatedAtDesc(String vendorId);
    
    // Find by status
    List<Invoice> findByStatus(String status);
    
    // Find by date range
    List<Invoice> findByInvoiceDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by customer and date range
    List<Invoice> findByCustomerIdAndInvoiceDateBetween(String customerId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by vendor and date range
    List<Invoice> findByVendorIdAndInvoiceDateBetween(String vendorId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Count by status
    long countByStatus(String status);
    
    // Check if invoice exists for order
    boolean existsByOrderId(String orderId);
    
    // Get latest invoice number for sequence generation
    Optional<Invoice> findTopByOrderByCreatedAtDesc();
}
