package com.odop.root.report.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model for generated reports
 */
@Document(collection = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    private String userRole;  // ADMIN, VENDOR
    
    // Report info
    private String reportName;
    private ReportType reportType;
    private ReportFormat format;
    
    // Time range
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // Filters applied
    private Map<String, Object> filters;
    
    // Generated file
    private String fileName;
    private String fileUrl;
    private long fileSizeBytes;
    
    // Status
    @Indexed
    private ReportStatus status;
    private String errorMessage;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;  // Auto-delete after this date
    
    private long generationTimeMs;
    
    /**
     * Report types
     */
    public enum ReportType {
        // Sales reports
        SALES_SUMMARY,          // Total sales by period
        SALES_BY_PRODUCT,       // Sales breakdown by product
        SALES_BY_CATEGORY,      // Sales by category
        SALES_BY_REGION,        // Sales by geographic region (ODOP focus)
        SALES_BY_VENDOR,        // Sales by vendor (Admin)
        
        // Order reports
        ORDER_HISTORY,          // All orders
        ORDER_STATUS,           // Orders by status
        PENDING_ORDERS,         // Orders awaiting action
        RETURNED_ORDERS,        // Returns and refunds
        
        // Product reports
        PRODUCT_INVENTORY,      // Stock levels
        LOW_STOCK_ALERT,        // Low stock items
        PRODUCT_PERFORMANCE,    // Best/worst sellers
        
        // Financial reports
        REVENUE_REPORT,         // Revenue summary
        TAX_REPORT,             // GST/Tax breakdown
        PAYOUT_REPORT,          // Vendor payouts
        COMMISSION_REPORT,      // Platform commissions
        
        // Customer reports
        CUSTOMER_ACTIVITY,      // Customer behavior
        TOP_CUSTOMERS,          // Best customers
        
        // ODOP specific
        ODOP_DISTRICT_SALES,    // Sales by ODOP district
        GI_TAG_PRODUCTS,        // GI tagged product performance
        ARTISAN_PERFORMANCE     // Artisan/maker performance
    }
    
    /**
     * Report formats
     */
    public enum ReportFormat {
        EXCEL,      // .xlsx
        CSV,        // .csv
        PDF         // .pdf
    }
    
    /**
     * Report status
     */
    public enum ReportStatus {
        PENDING,
        GENERATING,
        COMPLETED,
        FAILED,
        EXPIRED
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
