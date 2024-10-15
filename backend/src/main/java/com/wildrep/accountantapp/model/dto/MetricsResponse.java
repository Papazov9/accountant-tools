package com.wildrep.accountantapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MetricsResponse {

    private String message;
    private boolean isEmpty;
    private Integer comparisonCount;
    private Long totalComparisons;
    private Long successfulComparisons;
    private Long failedComparisons;
    private Long totalMismatches;
}
