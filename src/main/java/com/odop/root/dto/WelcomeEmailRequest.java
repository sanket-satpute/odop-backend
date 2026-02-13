package com.odop.root.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for welcome email after registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelcomeEmailRequest {
    
    private String email;
    private String customerName;
    private String accountType; // CUSTOMER, VENDOR, ADMIN
    private String loginUrl;
    private String supportEmail;
}
