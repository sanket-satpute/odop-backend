package com.odop.root.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    // Optional role parameter to specify which user type to authenticate
    // Values: "admin", "customer", "vendor"
    // If not provided, the system will search in order: admin -> customer -> vendor
    private String role;
}
