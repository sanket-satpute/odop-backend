package com.odop.root.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.dto.OrderDto;
import com.odop.root.dto.OrderItemDto;
import com.odop.root.dto.PageResponse;
import com.odop.root.models.Order;
import com.odop.root.models.OrderItem;
import com.odop.root.services.OrderService;

@RestController
@RequestMapping("odop/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    private static final Logger logger = LogManager.getLogger(OrderController.class);

    @PostMapping("/create")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        Order order = toEntity(orderDto);
        Order savedOrder = orderService.createOrder(order);
        return ResponseEntity.ok(toDto(savedOrder));
    }

    @GetMapping("/get/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(@PathVariable String customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<OrderDto>> getOrdersByVendorId(@PathVariable String vendorId) {
        List<Order> orders = orderService.getOrdersByVendorId(vendorId);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @PatchMapping("/update-status/{orderId}")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(toDto(updatedOrder));
    }

    @PatchMapping("/update-payment/{orderId}")
    public ResponseEntity<OrderDto> updatePaymentStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String paymentStatus = body.get("paymentStatus");
        String transactionId = body.get("transactionId");
        Order updatedOrder = orderService.updatePaymentStatus(orderId, paymentStatus, transactionId);
        return ResponseEntity.ok(toDto(updatedOrder));
    }

    @PatchMapping("/update-tracking/{orderId}")
    public ResponseEntity<OrderDto> updateTrackingInfo(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String trackingNumber = body.get("trackingNumber");
        String courierPartner = body.get("courierPartner");
        Order updatedOrder = orderService.updateTrackingInfo(orderId, trackingNumber, courierPartner);
        return ResponseEntity.ok(toDto(updatedOrder));
    }

    @PatchMapping("/cancel/{orderId}")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        Order cancelledOrder = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(toDto(cancelledOrder));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders.stream().map(this::toDto).collect(Collectors.toList()));
    }

    // --- Paginated Endpoints ---
    
    /**
     * Get all orders with pagination.
     */
    @GetMapping("/paginated")
    public PageResponse<OrderDto> getAllOrdersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Page<Order> orderPage = orderService.getAllOrdersPaginated(page, size, sortBy, sortDir);
        List<OrderDto> dtos = orderPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(orderPage, dtos);
    }

    @GetMapping("/customer/{customerId}/paginated")
    public PageResponse<OrderDto> getOrdersByCustomerPaginated(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orderPage = orderService.getOrdersByCustomerIdPaginated(customerId, page, size);
        List<OrderDto> dtos = orderPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(orderPage, dtos);
    }

    @GetMapping("/vendor/{vendorId}/paginated")
    public PageResponse<OrderDto> getOrdersByVendorPaginated(
            @PathVariable String vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orderPage = orderService.getOrdersByVendorIdPaginated(vendorId, page, size);
        List<OrderDto> dtos = orderPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(orderPage, dtos);
    }

    @GetMapping("/status/{status}/paginated")
    public PageResponse<OrderDto> getOrdersByStatusPaginated(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orderPage = orderService.getOrdersByStatusPaginated(status, page, size);
        List<OrderDto> dtos = orderPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.of(orderPage, dtos);
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<Boolean> deleteOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.deleteOrder(orderId));
    }

    // DTO/Entity mapping helpers
    private OrderDto toDto(Order order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomerId());
        dto.setVendorId(order.getVendorId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setDeliveryCharges(order.getDeliveryCharges());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingDistrict(order.getShippingDistrict());
        dto.setShippingState(order.getShippingState());
        dto.setShippingPinCode(order.getShippingPinCode());
        dto.setShippingContactNumber(order.getShippingContactNumber());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentTransactionId(order.getPaymentTransactionId());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setCourierPartner(order.getCourierPartner());
        dto.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(order.getActualDeliveryDate());
        dto.setCustomerNotes(order.getCustomerNotes());
        dto.setVendorNotes(order.getVendorNotes());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private OrderItemDto toItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setProductImageURL(item.getProductImageURL());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscount(item.getDiscount());
        dto.setTotalPrice(item.getTotalPrice());
        return dto;
    }

    private Order toEntity(OrderDto dto) {
        Order order = new Order();
        order.setOrderId(dto.getOrderId());
        order.setCustomerId(dto.getCustomerId());
        order.setVendorId(dto.getVendorId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setDiscountAmount(dto.getDiscountAmount());
        order.setDeliveryCharges(dto.getDeliveryCharges());
        order.setFinalAmount(dto.getFinalAmount());
        order.setShippingAddress(dto.getShippingAddress());
        order.setShippingDistrict(dto.getShippingDistrict());
        order.setShippingState(dto.getShippingState());
        order.setShippingPinCode(dto.getShippingPinCode());
        order.setShippingContactNumber(dto.getShippingContactNumber());
        order.setOrderStatus(dto.getOrderStatus());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setCustomerNotes(dto.getCustomerNotes());
        
        if (dto.getOrderItems() != null) {
            order.setOrderItems(dto.getOrderItems().stream()
                    .map(this::toItemEntity)
                    .collect(Collectors.toList()));
        }
        return order;
    }

    private OrderItem toItemEntity(OrderItemDto dto) {
        return new OrderItem(
                dto.getProductId(),
                dto.getProductName(),
                dto.getProductImageURL(),
                dto.getQuantity(),
                dto.getUnitPrice(),
                dto.getDiscount()
        );
    }
}
