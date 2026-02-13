package com.odop.root.analytics.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSeriesDataDto {
    private String date;
    private String label;
    private double value;
    private long count;
    private double growthPercent;
}
