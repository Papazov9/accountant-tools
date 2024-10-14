package com.wildrep.accountantapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comparisons")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Comparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime comparisonDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ComparisonResult> comparisonResults;

    @Column(name = "status", nullable = false)
    private String status; // e.g., COMPLETED, IN_PROGRESS, FAILED

    @Column(name = "batch_id", nullable = false)
    private String batchId;

}