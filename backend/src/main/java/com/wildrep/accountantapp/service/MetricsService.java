package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.model.Metrics;
import com.wildrep.accountantapp.model.User;
import com.wildrep.accountantapp.model.dto.MetricsResponse;
import com.wildrep.accountantapp.repo.MetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MetricsRepository metricsRepository;

    public void updateMetricsAfterComparison(User user, boolean isSuccessful, long mismatches) {
        Metrics metrics = this.metricsRepository.findByUsername(user.getUsername()).orElse(null);

        if (metrics == null) {
            metrics = Metrics.builder()
                    .user(user)
                    .failedComparisons(0)
                    .successfulComparisons(0)
                    .totalMismatches(0)
                    .totalComparisons(0)
                    .build();
        }

        metrics.setTotalComparisons(metrics.getTotalComparisons() + 1);

        if (isSuccessful) {
            metrics.setSuccessfulComparisons(metrics.getSuccessfulComparisons() + 1);
        }else {
            metrics.setFailedComparisons(metrics.getFailedComparisons() + 1);
        }

        metrics.setTotalMismatches(metrics.getTotalMismatches() + mismatches);

        this.metricsRepository.save(metrics);
    }

    public MetricsResponse loadMetricsByUsername(String username) {
        Metrics metrics = this.metricsRepository.findByUsername(username).orElse(null);

        if (metrics == null) {
            return MetricsResponse.builder()
                    .isEmpty(true)
                    .build();
        }
        return MetricsResponse.builder()
                .message("Successfully loaded metrics")
                .comparisonCount(metrics.getUser().getComparisonCount())
                .successfulComparisons(metrics.getSuccessfulComparisons())
                .totalComparisons(metrics.getTotalComparisons())
                .failedComparisons(metrics.getFailedComparisons())
                .totalMismatches(metrics.getTotalMismatches())
                .isEmpty(false)
                .build();
    }
}
