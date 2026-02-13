package com.odop.root.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Admin entity for ODOP e-commerce platform.
 * Manages platform administration and settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admins")
public class Admin {

    @Id
    private String adminId;
    private String fullName;
    private String emailAddress;
    private String password;
    private long contactNumber;
    private String positionAndRole;
    private boolean active;
    private String authorizationKey;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convenience constructor for admin registration.
     */
    public Admin(String fullName, String emailAddress, String password, long contactNumber, String positionAndRole) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.contactNumber = contactNumber;
        this.positionAndRole = positionAndRole;
    }
}
