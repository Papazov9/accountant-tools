package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.ComparisonMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComparisonMetricRepository extends JpaRepository<ComparisonMetric, Long> {
    @Query("SELECT cm FROM ComparisonMetric cm WHERE cm.user.username = ?1 ORDER BY cm.comparisonDate ASC")
    List<ComparisonMetric> findByUserUsernameOrderByComparisonDateAsc(String username);
}
