package com.wildrep.accountantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "csv_records")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CSVInvoiceRecord {

    @EmbeddedId
    private InvoiceRecordId id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "accounting_period", nullable = false)
    private String accountingPeriod;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "vat_amount")
    private Double vatAmount;

    @Column(name = "issue_date")
    private LocalDate issueDate;
}
