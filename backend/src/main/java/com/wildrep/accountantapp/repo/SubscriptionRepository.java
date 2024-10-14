package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Subscription;
import com.wildrep.accountantapp.model.enums.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByType(SubscriptionType subscriptionType);
}
