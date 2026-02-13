package com.odop.root.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.models.Order;
import com.odop.root.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private static final Logger logger = LogManager.getLogger(OrderService.class);

    public Order createOrder(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        if (order.getOrderStatus() == null) {
            order.setOrderStatus("PENDING");
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("PENDING");
        }
        return orderRepository.save(order);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> getOrdersByVendorId(String vendorId) {
        return orderRepository.findByVendorIdOrderByCreatedAtDesc(vendorId);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByOrderStatus(status);
    }

    public List<Order> getOrdersByCustomerAndStatus(String customerId, String status) {
        return orderRepository.findByCustomerIdAndOrderStatus(customerId, status);
    }

    public List<Order> getOrdersByVendorAndStatus(String vendorId, String status) {
        return orderRepository.findByVendorIdAndOrderStatus(vendorId, status);
    }

    // --- Pagination Support ---
    
    /**
     * Get all orders with pagination.
     */
    public Page<Order> getAllOrdersPaginated(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByCustomerIdPaginated(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Order> getOrdersByVendorIdPaginated(String vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByVendorId(vendorId, pageable);
    }

    public Page<Order> getOrdersByStatusPaginated(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByOrderStatus(status, pageable);
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setOrderStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            
            // Auto-set delivery date if delivered
            if ("DELIVERED".equals(status)) {
                order.setActualDeliveryDate(LocalDateTime.now());
            }
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public Order updatePaymentStatus(String orderId, String paymentStatus, String transactionId) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setPaymentStatus(paymentStatus);
            order.setPaymentTransactionId(transactionId);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public Order updateTrackingInfo(String orderId, String trackingNumber, String courierPartner) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setTrackingNumber(trackingNumber);
            order.setCourierPartner(courierPartner);
            order.setOrderStatus("SHIPPED");
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public Order cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setOrderStatus("CANCELLED");
            order.setCancellationReason(reason);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public boolean deleteOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            orderRepository.delete(order);
            return true;
        }
        return false;
    }
}
