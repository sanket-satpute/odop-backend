package com.odop.root.report.dto;

import com.odop.root.report.model.Report;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Request DTO for generating reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    // Report type - required
    private String reportType;
    
    // Output format
    @Builder.Default
    private String format = "EXCEL";
    
    // Custom report name (optional)
    private String reportName;
    
    // Date range
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Filters
    private Map<String, Object> filters;
    
    // Common filter fields for convenience
    private String vendorId;
    private String categoryId;
    private String productId;
    private String customerId;
    private String district;
    private String state;
    private String orderStatus;
    
    // Pagination for large reports
    @Builder.Default
    private int limit = 10000;
    
    // Include details
    @Builder.Default
    private boolean includeDetails = true;
    
    @Builder.Default
    private boolean includeSummary = true;
    
    /**
     * Get start datetime
     */
    public LocalDateTime getStartDateTime() {
        return startDate != null ? startDate.atStartOfDay() : null;
    }
    
    /**
     * Get end datetime
     */
    public LocalDateTime getEndDateTime() {
        return endDate != null ? endDate.atTime(23, 59, 59) : null;
    }
    
    /**
     * Get report type enum
     */
    public Report.ReportType getReportTypeEnum() {
        return Report.ReportType.valueOf(reportType);
    }
    
    /**
     * Get format enum
     */
    public Report.ReportFormat getFormatEnum() {
        return Report.ReportFormat.valueOf(format);
    }
}
