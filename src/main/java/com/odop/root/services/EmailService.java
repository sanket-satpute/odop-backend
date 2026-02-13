package com.odop.root.services;

import com.odop.root.dto.*;
// MessagingException import removed - not used
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Email Service for sending various types of emails
 * Uses async execution to avoid blocking API responses
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${email.from:noreply@odop.com}")
    private String fromEmail;

    @Value("${email.from-name:ODOP Store}")
    private String fromName;

    /**
     * Send a simple text email
     */
    public EmailResponse sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            
            logger.info("Simple email sent successfully to: {}", to);
            return buildSuccessResponse(to, "Email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send simple email to: {}", to, e);
            return buildErrorResponse(to, "Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Send an HTML email
     */
    public EmailResponse sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            
            mailSender.send(message);
            
            logger.info("HTML email sent successfully to: {}", to);
            return buildSuccessResponse(to, "HTML email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            return buildErrorResponse(to, "Failed to send HTML email: " + e.getMessage());
        }
    }

    /**
     * Send welcome email after registration (Async)
     */
    @Async("emailExecutor")
    public void sendWelcomeEmailAsync(WelcomeEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("accountType", request.getAccountType());
            context.setVariable("loginUrl", request.getLoginUrl() != null ? request.getLoginUrl() : "http://localhost:4200/login");
            context.setVariable("supportEmail", request.getSupportEmail() != null ? request.getSupportEmail() : fromEmail);
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/welcome", context);
            
            sendHtmlEmail(request.getEmail(), "Welcome to ODOP Store! 🎉", htmlContent);
            logger.info("Welcome email sent to: {}", request.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", request.getEmail(), e);
        }
    }

    /**
     * Send welcome email (Sync version)
     */
    public EmailResponse sendWelcomeEmail(WelcomeEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("accountType", request.getAccountType());
            context.setVariable("loginUrl", request.getLoginUrl() != null ? request.getLoginUrl() : "http://localhost:4200/login");
            context.setVariable("supportEmail", request.getSupportEmail() != null ? request.getSupportEmail() : fromEmail);
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/welcome", context);
            
            return sendHtmlEmail(request.getEmail(), "Welcome to ODOP Store! 🎉", htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", request.getEmail(), e);
            return buildErrorResponse(request.getEmail(), "Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Send order confirmation email (Async)
     */
    @Async("emailExecutor")
    public void sendOrderConfirmationAsync(OrderEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("orderId", request.getOrderId());
            context.setVariable("orderDate", request.getOrderDate());
            context.setVariable("items", request.getItems());
            context.setVariable("subtotal", request.getSubtotal());
            context.setVariable("shippingCost", request.getShippingCost());
            context.setVariable("tax", request.getTax());
            context.setVariable("totalAmount", request.getTotalAmount());
            context.setVariable("paymentMethod", request.getPaymentMethod());
            context.setVariable("paymentStatus", request.getPaymentStatus());
            context.setVariable("shippingAddress", request.getShippingAddress());
            context.setVariable("estimatedDelivery", request.getEstimatedDelivery());
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/order-confirmation", context);
            
            sendHtmlEmail(request.getCustomerEmail(), 
                "Order Confirmed! #" + request.getOrderId() + " 📦", 
                htmlContent);
            logger.info("Order confirmation email sent to: {}", request.getCustomerEmail());
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email to: {}", request.getCustomerEmail(), e);
        }
    }

    /**
     * Send order confirmation email (Sync version)
     */
    public EmailResponse sendOrderConfirmation(OrderEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("orderId", request.getOrderId());
            context.setVariable("orderDate", request.getOrderDate());
            context.setVariable("items", request.getItems());
            context.setVariable("subtotal", request.getSubtotal());
            context.setVariable("shippingCost", request.getShippingCost());
            context.setVariable("tax", request.getTax());
            context.setVariable("totalAmount", request.getTotalAmount());
            context.setVariable("paymentMethod", request.getPaymentMethod());
            context.setVariable("paymentStatus", request.getPaymentStatus());
            context.setVariable("shippingAddress", request.getShippingAddress());
            context.setVariable("estimatedDelivery", request.getEstimatedDelivery());
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/order-confirmation", context);
            
            return sendHtmlEmail(request.getCustomerEmail(), 
                "Order Confirmed! #" + request.getOrderId() + " 📦", 
                htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email to: {}", request.getCustomerEmail(), e);
            return buildErrorResponse(request.getCustomerEmail(), "Failed to send order confirmation: " + e.getMessage());
        }
    }

    /**
     * Send payment success email (Async)
     */
    @Async("emailExecutor")
    public void sendPaymentSuccessAsync(PaymentEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("orderId", request.getOrderId());
            context.setVariable("paymentId", request.getPaymentId());
            context.setVariable("razorpayPaymentId", request.getRazorpayPaymentId());
            context.setVariable("amount", request.getAmount());
            context.setVariable("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            context.setVariable("paymentMethod", request.getPaymentMethod());
            context.setVariable("transactionDate", request.getTransactionDate());
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/payment-success", context);
            
            sendHtmlEmail(request.getCustomerEmail(), 
                "Payment Successful! ✅ Order #" + request.getOrderId(), 
                htmlContent);
            logger.info("Payment success email sent to: {}", request.getCustomerEmail());
        } catch (Exception e) {
            logger.error("Failed to send payment success email to: {}", request.getCustomerEmail(), e);
        }
    }

    /**
     * Send payment success email (Sync version)
     */
    public EmailResponse sendPaymentSuccess(PaymentEmailRequest request) {
        try {
            Context context = new Context();
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("orderId", request.getOrderId());
            context.setVariable("paymentId", request.getPaymentId());
            context.setVariable("razorpayPaymentId", request.getRazorpayPaymentId());
            context.setVariable("amount", request.getAmount());
            context.setVariable("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            context.setVariable("paymentMethod", request.getPaymentMethod());
            context.setVariable("transactionDate", request.getTransactionDate());
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/payment-success", context);
            
            return sendHtmlEmail(request.getCustomerEmail(), 
                "Payment Successful! ✅ Order #" + request.getOrderId(), 
                htmlContent);
        } catch (Exception e) {
            logger.error("Failed to send payment success email to: {}", request.getCustomerEmail(), e);
            return buildErrorResponse(request.getCustomerEmail(), "Failed to send payment email: " + e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    @Async("emailExecutor")
    public void sendPasswordResetAsync(String email, String resetToken, String resetUrl) {
        try {
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl + "?token=" + resetToken);
            context.setVariable("expiryMinutes", 30);
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/password-reset", context);
            
            sendHtmlEmail(email, "Password Reset Request 🔐", htmlContent);
            logger.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", email, e);
        }
    }

    /**
     * Send order shipped notification (Async)
     */
    @Async("emailExecutor")
    public void sendOrderShippedAsync(String email, String customerName, String orderId, 
                                       String trackingNumber, String courierName, String trackingUrl) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("trackingNumber", trackingNumber);
            context.setVariable("courierName", courierName);
            context.setVariable("trackingUrl", trackingUrl);
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/order-shipped", context);
            
            sendHtmlEmail(email, "Your Order #" + orderId + " Has Been Shipped! 🚚", htmlContent);
            logger.info("Order shipped email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send order shipped email to: {}", email, e);
        }
    }

    /**
     * Send order delivered notification (Async)
     */
    @Async("emailExecutor")
    public void sendOrderDeliveredAsync(String email, String customerName, String orderId) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("reviewUrl", "http://localhost:4200/orders/" + orderId + "/review");
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/order-delivered", context);
            
            sendHtmlEmail(email, "Your Order #" + orderId + " Has Been Delivered! 📦✅", htmlContent);
            logger.info("Order delivered email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send order delivered email to: {}", email, e);
        }
    }

    // ========== Helper Methods ==========

    private EmailResponse buildSuccessResponse(String email, String message) {
        return EmailResponse.builder()
                .success(true)
                .message(message)
                .emailId(UUID.randomUUID().toString())
                .recipientEmail(email)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private EmailResponse buildErrorResponse(String email, String message) {
        return EmailResponse.builder()
                .success(false)
                .message(message)
                .recipientEmail(email)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}
