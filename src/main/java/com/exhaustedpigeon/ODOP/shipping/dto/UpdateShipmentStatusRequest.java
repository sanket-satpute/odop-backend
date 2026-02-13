package com.exhaustedpigeon.ODOP.shipping.dto;

import com.exhaustedpigeon.ODOP.shipping.model.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating shipment status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShipmentStatusRequest {
    
    @NotNull(message = "Status is required")
    private ShipmentStatus status;
    
    private String location;
    
    private String description;
    
    private String remarks;
    
    // For delivery
    private String deliveredTo;
    private String deliveryProofUrl;
    
    // For returns
    private String returnReason;
}
