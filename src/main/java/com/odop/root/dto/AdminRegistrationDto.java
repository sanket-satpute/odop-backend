package com.odop.root.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminRegistrationDto {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Positive(message = "Contact number must be positive")
    private long contactNumber;
    
    @NotBlank(message = "Position and role is required")
    private String positionAndRole;
    
    @NotBlank(message = "Authorization key is required")
    private String authorizationKey;
}
