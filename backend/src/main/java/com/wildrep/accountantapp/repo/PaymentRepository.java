package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findPaymentByPaymentIntentId(String paymentIntentId);

    Optional<Payment> findPaymentBySessionId(String sessionId);
    Optional<Payment> findPaymentByInvoiceId(String invoiceId);
}
