package com.odop.root.report.dto;

import com.odop.root.report.model.Report;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private String reportId;
    private String reportName;
    private String reportType;
    private String format;
    
    private String status;
    private int progressPercent;
    
    private String fileName;
    private String downloadUrl;
    private long fileSizeBytes;
    private String fileSizeFormatted;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    
    private long generationTimeMs;
    private String errorMessage;
    
    private boolean success;
    private String message;
    
    /**
     * Create from entity
     */
    public static ReportResponse fromReport(Report report) {
        return ReportResponse.builder()
                .reportId(report.getId())
                .reportName(report.getReportName())
                .reportType(report.getReportType() != null ? report.getReportType().name() : null)
                .format(report.getFormat() != null ? report.getFormat().name() : null)
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .fileName(report.getFileName())
                .downloadUrl(report.getFileUrl())
                .fileSizeBytes(report.getFileSizeBytes())
                .fileSizeFormatted(formatFileSize(report.getFileSizeBytes()))
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .createdAt(report.getCreatedAt())
                .generatedAt(report.getGeneratedAt())
                .expiresAt(report.getExpiresAt())
                .generationTimeMs(report.getGenerationTimeMs())
                .errorMessage(report.getErrorMessage())
                .success(report.getStatus() == Report.ReportStatus.COMPLETED)
                .build();
    }
    
    /**
     * Create started response
     */
    public static ReportResponse started(Report report) {
        ReportResponse response = fromReport(report);
        response.setMessage("Report generation started");
        response.setProgressPercent(0);
        return response;
    }
    
    /**
     * Create error response
     */
    public static ReportResponse error(String message) {
        return ReportResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
}
