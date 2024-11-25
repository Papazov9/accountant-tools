package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Invoice findFirstByOrderByIdDesc();
}
