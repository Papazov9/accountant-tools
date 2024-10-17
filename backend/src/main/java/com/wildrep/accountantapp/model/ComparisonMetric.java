package com.wildrep.accountantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comparison_metric")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ComparisonMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "mismatches", nullable = false)
    private long mismatches;

    @Column(name = "comparison_date", nullable = false)
    private LocalDateTime comparisonDate;
}
