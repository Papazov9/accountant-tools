package com.wildrep.accountantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metrics")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_comparisons", nullable = false)
    private int totalComparisons;

    @Column(name = "successful_comparisons", nullable = false)
    private int successfulComparisons;

    @Column(name = "failed_comparisons", nullable = false)
    private int failedComparisons;

    @Column(name = "total_mismatches", nullable = false)
    private int totalMismatches;

    @Column(name = "remaining_comparisons", nullable = false)
    private int remainingComparisons;
}
