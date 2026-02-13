package com.odop.root.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic email request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    
    private String to;
    private String subject;
    private String body;
    private boolean isHtml;
}
