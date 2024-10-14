package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.InvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, InvoiceRecordId> {

}
