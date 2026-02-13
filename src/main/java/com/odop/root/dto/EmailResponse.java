package com.odop.root.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for email operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResponse {
    
    private boolean success;
    private String message;
    private String emailId;
    private String recipientEmail;
    private String timestamp;
}
