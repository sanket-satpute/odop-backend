package com.odop.root.dto;

import lombok.Data;

@Data
public class CustomerDto {
    private String customerId;
    private String fullName;
    private String emailAddress;
    private long contactNumber;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String status;
    private String profilePictureUrl;
}
