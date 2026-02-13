package com.odop.root.models;

/**
 * Enum representing the various states a payment can be in.
 */
public enum PaymentStatus {
    CREATED,        // Payment order created, awaiting payment
    PENDING,        // Payment initiated, waiting for confirmation
    SUCCESS,        // Payment successfully completed
    FAILED,         // Payment failed
    REFUND_INITIATED, // Refund process started
    REFUNDED,       // Payment refunded successfully
    CANCELLED       // Payment cancelled by user
}
