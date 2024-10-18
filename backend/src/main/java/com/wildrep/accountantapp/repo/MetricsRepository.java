package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Metrics;
import com.wildrep.accountantapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {
    Optional<Metrics> findByUser(User user);

    @Query("SELECT m from Metrics m WHERE m.user.username = ?1")
    Optional<Metrics> findByUsername(String username);
}
