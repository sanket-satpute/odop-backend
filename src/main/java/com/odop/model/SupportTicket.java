package com.odop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "support_tickets")
public class SupportTicket {

    @Id
    private String ticketId;

    @Indexed
    private String customerId;

    private String customerName;
    private String customerEmail;

    private String subject;
    private String description;

    @Indexed
    private String category; // order, product, payment, delivery, refund, account, other

    @Indexed
    private String status; // open, in-progress, resolved, closed

    @Indexed
    private String priority; // low, medium, high, urgent

    private String orderId; // Optional - linked order if applicable

    private List<TicketMessage> messages = new ArrayList<>();

    private List<String> attachments = new ArrayList<>();

    private String assignedTo; // Admin/Support staff ID

    private String resolution;

    private Date createdAt;
    private Date updatedAt;
    private Date resolvedAt;
    private Date closedAt;

    // Nested class for ticket messages/conversation
    public static class TicketMessage {
        private String messageId;
        private String senderId;
        private String senderName;
        private String senderRole; // customer, support, admin
        private String message;
        private List<String> attachments = new ArrayList<>();
        private Date timestamp;
        private boolean isRead;

        public TicketMessage() {
            this.timestamp = new Date();
            this.isRead = false;
        }

        // Getters and Setters
        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getSenderRole() {
            return senderRole;
        }

        public void setSenderRole(String senderRole) {
            this.senderRole = senderRole;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<String> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<String> attachments) {
            this.attachments = attachments;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isRead() {
            return isRead;
        }

        public void setRead(boolean isRead) {
            this.isRead = isRead;
        }
    }

    // Constructor
    public SupportTicket() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = "open";
        this.priority = "medium";
    }

    // Pre-save hook
    public void preSave() {
        this.updatedAt = new Date();
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }

    // Getters and Setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<TicketMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessage> messages) {
        this.messages = messages;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    // Helper methods
    public void addMessage(TicketMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        message.setMessageId("MSG-" + System.currentTimeMillis());
        this.messages.add(message);
        this.updatedAt = new Date();
    }

    public int getUnreadMessageCount(String userId) {
        if (messages == null) return 0;
        return (int) messages.stream()
            .filter(m -> !m.isRead() && !m.getSenderId().equals(userId))
            .count();
    }
}
