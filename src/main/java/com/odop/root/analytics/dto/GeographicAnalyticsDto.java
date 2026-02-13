package com.odop.root.analytics.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeographicAnalyticsDto {
    
    private List<StateAnalytics> stateWiseData;
    private List<DistrictAnalytics> districtWiseData;
    private List<StateAnalytics> topRevenueStates;
    private List<DistrictAnalytics> topRevenueDistricts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StateAnalytics {
        private String state;
        private double revenue;
        private long orders;
        private long products;
        private long vendors;
        private long customers;
        private long giTagProducts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistrictAnalytics {
        private String district;
        private String state;
        private double revenue;
        private long orders;
        private long products;
        private long vendors;
        private long giTagProducts;
    }
}
