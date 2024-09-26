package com.wildrep.accountantapp.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "csv_records")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CSVInvoiceRecord {
    @EmbeddedId
    private InvoiceRecordId id;

    private String companyName;
    private String accountingPeriod;
    private Date issueDate;

    private Double totalAmount;
    private Double vatAmount;

    private String batchId;
}