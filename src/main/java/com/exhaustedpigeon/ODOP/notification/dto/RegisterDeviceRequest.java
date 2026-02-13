package com.exhaustedpigeon.ODOP.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for registering device token for push notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceRequest {
    
    @NotBlank(message = "Device token is required")
    private String token;
    
    @NotBlank(message = "Platform is required")
    private String platform;        // WEB, ANDROID, IOS
    
    private String deviceId;
    private String deviceName;
}
