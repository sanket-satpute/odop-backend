package com.odop.root.controller;

import com.odop.root.dto.*;
import com.odop.root.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Email operations
 * Base URL: /odop/email
 */
@RestController
@RequestMapping("/odop/email")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:63699"})
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Email Service");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Send a simple text email
     * POST /odop/email/send
     */
    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendSimpleEmail(@RequestBody EmailRequest request) {
        logger.info("Sending simple email to: {}", request.getTo());
        
        EmailResponse response;
        if (request.isHtml()) {
            response = emailService.sendHtmlEmail(request.getTo(), request.getSubject(), request.getBody());
        } else {
            response = emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getBody());
        }
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }

    /**
     * Send welcome email
     * POST /odop/email/welcome
     */
    @PostMapping("/welcome")
    public ResponseEntity<EmailResponse> sendWelcomeEmail(@RequestBody WelcomeEmailRequest request) {
        logger.info("Sending welcome email to: {}", request.getEmail());
        
        EmailResponse response = emailService.sendWelcomeEmail(request);
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }

    /**
     * Send welcome email (async - fire and forget)
     * POST /odop/email/welcome/async
     */
    @PostMapping("/welcome/async")
    public ResponseEntity<Map<String, String>> sendWelcomeEmailAsync(@RequestBody WelcomeEmailRequest request) {
        logger.info("Queuing welcome email for: {}", request.getEmail());
        
        emailService.sendWelcomeEmailAsync(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "queued");
        response.put("message", "Welcome email has been queued for delivery");
        response.put("recipient", request.getEmail());
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Send order confirmation email
     * POST /odop/email/order-confirmation
     */
    @PostMapping("/order-confirmation")
    public ResponseEntity<EmailResponse> sendOrderConfirmation(@RequestBody OrderEmailRequest request) {
        logger.info("Sending order confirmation to: {} for order: {}", 
            request.getCustomerEmail(), request.getOrderId());
        
        EmailResponse response = emailService.sendOrderConfirmation(request);
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }

    /**
     * Send order confirmation email (async)
     * POST /odop/email/order-confirmation/async
     */
    @PostMapping("/order-confirmation/async")
    public ResponseEntity<Map<String, String>> sendOrderConfirmationAsync(@RequestBody OrderEmailRequest request) {
        logger.info("Queuing order confirmation for: {} - order: {}", 
            request.getCustomerEmail(), request.getOrderId());
        
        emailService.sendOrderConfirmationAsync(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "queued");
        response.put("message", "Order confirmation email has been queued for delivery");
        response.put("orderId", request.getOrderId());
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Send payment success email
     * POST /odop/email/payment-success
     */
    @PostMapping("/payment-success")
    public ResponseEntity<EmailResponse> sendPaymentSuccess(@RequestBody PaymentEmailRequest request) {
        logger.info("Sending payment success email to: {} for payment: {}", 
            request.getCustomerEmail(), request.getPaymentId());
        
        EmailResponse response = emailService.sendPaymentSuccess(request);
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }

    /**
     * Send payment success email (async)
     * POST /odop/email/payment-success/async
     */
    @PostMapping("/payment-success/async")
    public ResponseEntity<Map<String, String>> sendPaymentSuccessAsync(@RequestBody PaymentEmailRequest request) {
        logger.info("Queuing payment success email for: {}", request.getCustomerEmail());
        
        emailService.sendPaymentSuccessAsync(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "queued");
        response.put("message", "Payment success email has been queued for delivery");
        response.put("paymentId", request.getPaymentId());
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Test endpoint - sends a test email
     * POST /odop/email/test
     */
    @PostMapping("/test")
    public ResponseEntity<EmailResponse> sendTestEmail(@RequestParam String to) {
        logger.info("Sending test email to: {}", to);
        
        String subject = "ODOP Email Test 🧪";
        String body = "Hello!\n\nThis is a test email from ODOP Store.\n\n" +
                      "If you received this email, your email configuration is working correctly!\n\n" +
                      "Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
                      "Best regards,\nODOP Team";
        
        EmailResponse response = emailService.sendSimpleEmail(to, subject, body);
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }

    /**
     * Test endpoint - sends a sample order confirmation
     * GET /odop/email/test/order-confirmation?email=...
     */
    @GetMapping("/test/order-confirmation")
    public ResponseEntity<EmailResponse> testOrderConfirmation(@RequestParam String email) {
        logger.info("Sending test order confirmation to: {}", email);
        
        // Create sample order data
        OrderEmailRequest.OrderItemDetail item1 = OrderEmailRequest.OrderItemDetail.builder()
            .productName("Traditional Handwoven Silk Saree")
            .quantity(1)
            .unitPrice(new BigDecimal("2499.00"))
            .totalPrice(new BigDecimal("2499.00"))
            .build();
        
        OrderEmailRequest.OrderItemDetail item2 = OrderEmailRequest.OrderItemDetail.builder()
            .productName("Brass Decorative Lamp")
            .quantity(2)
            .unitPrice(new BigDecimal("799.00"))
            .totalPrice(new BigDecimal("1598.00"))
            .build();
        
        OrderEmailRequest.ShippingAddress address = OrderEmailRequest.ShippingAddress.builder()
            .fullName("Test Customer")
            .addressLine1("123, Sample Street")
            .city("Mumbai")
            .state("Maharashtra")
            .postalCode("400001")
            .country("India")
            .phone("+91 9876543210")
            .build();
        
        OrderEmailRequest request = OrderEmailRequest.builder()
            .customerEmail(email)
            .customerName("Test Customer")
            .orderId("ORD-TEST-" + System.currentTimeMillis())
            .orderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")))
            .items(Arrays.asList(item1, item2))
            .subtotal(new BigDecimal("4097.00"))
            .shippingCost(new BigDecimal("99.00"))
            .tax(new BigDecimal("737.46"))
            .totalAmount(new BigDecimal("4933.46"))
            .paymentMethod("Razorpay")
            .paymentStatus("SUCCESS")
            .shippingAddress(address)
            .estimatedDelivery("5-7 Business Days")
            .build();
        
        EmailResponse response = emailService.sendOrderConfirmation(request);
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }

    /**
     * Test endpoint - sends a sample welcome email
     * GET /odop/email/test/welcome?email=...&name=...
     */
    @GetMapping("/test/welcome")
    public ResponseEntity<EmailResponse> testWelcomeEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "Test User") String name) {
        logger.info("Sending test welcome email to: {}", email);
        
        WelcomeEmailRequest request = WelcomeEmailRequest.builder()
            .email(email)
            .customerName(name)
            .accountType("CUSTOMER")
            .loginUrl("http://localhost:4200/login")
            .supportEmail("support@odop.com")
            .build();
        
        EmailResponse response = emailService.sendWelcomeEmail(request);
        
        return response.isSuccess() 
            ? ResponseEntity.ok(response) 
            : ResponseEntity.internalServerError().body(response);
    }
}
