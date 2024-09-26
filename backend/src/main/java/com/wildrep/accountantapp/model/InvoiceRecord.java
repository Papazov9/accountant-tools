package com.wildrep.accountantapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "invoice_records")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InvoiceRecord {
    @EmbeddedId
    private InvoiceRecordId id;

    private String companyName;
    private String accountingPeriod;
    private Date issueDate;

    private Double totalAmount;
    private Double vatAmount;

    private String batchId;
}
