package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComparisonRepository extends JpaRepository<Comparison, Long> {
    Optional<Comparison> findByBatchId(String batchId);
}
