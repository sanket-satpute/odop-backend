package com.odop.root.dto;

import lombok.Data;

@Data
public class AdminDto {
    private String adminId;
    private String fullName;
    private String emailAddress;
    private long contactNumber;
    private String positionAndRole;
    private boolean active;
}
