package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Payment;
import com.wildrep.accountantapp.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByStatus(PaymentStatus paymentStatus);

//    Optional<Payment> findPaymentBySessionId(String sessionId);
}
