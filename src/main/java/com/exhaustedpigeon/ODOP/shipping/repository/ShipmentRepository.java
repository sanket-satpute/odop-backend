package com.exhaustedpigeon.ODOP.shipping.repository;

import com.exhaustedpigeon.ODOP.shipping.model.Shipment;
import com.exhaustedpigeon.ODOP.shipping.model.ShipmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Shipment operations
 */
@Repository
public interface ShipmentRepository extends MongoRepository<Shipment, String> {
    
    /**
     * Find by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find by order ID
     */
    Optional<Shipment> findByOrderId(String orderId);
    
    /**
     * Find all shipments for a customer
     */
    List<Shipment> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    /**
     * Find all shipments for a vendor
     */
    List<Shipment> findByVendorIdOrderByCreatedAtDesc(String vendorId);
    
    /**
     * Find by status
     */
    List<Shipment> findByStatus(ShipmentStatus status);
    
    /**
     * Find by courier tracking ID
     */
    Optional<Shipment> findByCourierTrackingId(String courierTrackingId);
    
    /**
     * Find active shipments (not delivered/cancelled/returned)
     */
    @Query("{ 'customerId': ?0, 'status': { $nin: ['DELIVERED', 'CANCELLED', 'RETURNED', 'LOST', 'DAMAGED'] } }")
    List<Shipment> findActiveShipmentsByCustomerId(String customerId);
    
    /**
     * Find shipments by status list
     */
    List<Shipment> findByStatusIn(List<ShipmentStatus> statuses);
    
    /**
     * Find shipments created after a date
     */
    List<Shipment> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find shipments needing status update (not updated in last 24 hours and still active)
     */
    @Query("{ 'lastUpdatedAt': { $lt: ?0 }, 'status': { $nin: ['DELIVERED', 'CANCELLED', 'RETURNED', 'LOST', 'DAMAGED'] } }")
    List<Shipment> findShipmentsNeedingUpdate(LocalDateTime cutoffTime);
    
    /**
     * Find delayed shipments (past estimated delivery and not delivered)
     */
    @Query("{ 'estimatedDeliveryDate': { $lt: ?0 }, 'status': { $nin: ['DELIVERED', 'CANCELLED', 'RETURNED', 'LOST', 'DAMAGED'] } }")
    List<Shipment> findDelayedShipments(LocalDateTime currentTime);
    
    /**
     * Count active shipments by vendor
     */
    long countByVendorIdAndStatusIn(String vendorId, List<ShipmentStatus> statuses);
    
    /**
     * Check if shipment exists for order
     */
    boolean existsByOrderId(String orderId);
}
