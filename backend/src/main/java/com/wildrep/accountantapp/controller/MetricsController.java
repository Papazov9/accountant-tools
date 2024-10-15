package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.model.dto.MetricsResponse;
import com.wildrep.accountantapp.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/all/{username}")
    public ResponseEntity<MetricsResponse> loadAllMetrics(@PathVariable String username) {
        try {
            MetricsResponse metricsResponse = this.metricsService.loadMetricsByUsername(username);

            return ResponseEntity.ok(metricsResponse);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(MetricsResponse.builder()
                            .message(e.getMessage())
                            .build());
        }
    }
}
