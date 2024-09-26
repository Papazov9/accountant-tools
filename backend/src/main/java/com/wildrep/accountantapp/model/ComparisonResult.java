package com.wildrep.accountantapp.model;

import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.model.enums.InvoiceField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ComparisonResult {

    private InvoiceRecordId invoiceRecordId;
    private CSVInvoiceRecord csvInvoiceRecord;
    private InvoiceRecord txtInvoiceRecord;
    private Set<InvoiceField> mismatchedFields;
    private ComparisonStatus status;

}