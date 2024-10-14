package com.wildrep.accountantapp.repo;

import com.wildrep.accountantapp.model.CSVInvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CSVRecordRepository extends JpaRepository<CSVInvoiceRecord, InvoiceRecordId> {

}
