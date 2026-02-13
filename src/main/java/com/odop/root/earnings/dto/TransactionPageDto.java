package com.odop.root.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for transactions and payouts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPageDto {
    
    private List<TransactionDto> transactions;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // Summary for the current filter
    private double totalCredits;
    private double totalDebits;
    private double netTotal;
}
