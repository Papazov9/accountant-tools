package com.wildrep.accountantapp.model;

import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.model.enums.InvoiceField;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "comparison_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "csv_bulstat", referencedColumnName = "bulstat"),
            @JoinColumn(name = "csv_document_type", referencedColumnName = "documentType"),
            @JoinColumn(name = "csv_document_number", referencedColumnName = "documentNumber")
    })
    private CSVInvoiceRecord csvInvoiceRecord;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "txt_bulstat", referencedColumnName = "bulstat"),
            @JoinColumn(name = "txt_document_type", referencedColumnName = "documentType"),
            @JoinColumn(name = "txt_document_number", referencedColumnName = "documentNumber")
    })
    private InvoiceRecord txtInvoiceRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComparisonStatus status;

    @ElementCollection(targetClass = InvoiceField.class)
    @CollectionTable(name = "mismatched_fields", joinColumns = @JoinColumn(name = "comparison_result_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "field")
    private Set<InvoiceField> mismatchedFields;

    @Column(name = "match_column")
    private String matchColumn;
}
