package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.ComparisonResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComparisonResultRepository extends JpaRepository<ComparisonResult, Long> {
}
