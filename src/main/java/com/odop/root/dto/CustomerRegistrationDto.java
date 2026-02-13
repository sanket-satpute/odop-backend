package com.odop.root.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerRegistrationDto {
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
    
    private String address;
    private String city;
    private String state;
    
    @Pattern(regexp = "^[0-9]{6}$", message = "Pin code must be 6 digits")
    private String pinCode;
}
