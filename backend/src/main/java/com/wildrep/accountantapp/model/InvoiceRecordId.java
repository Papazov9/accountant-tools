package com.wildrep.accountantapp.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRecordId implements Serializable {

    private String bulstat;
    private String documentType;
    private String documentNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceRecordId that = (InvoiceRecordId) o;
        return Objects.equals(bulstat, that.bulstat) && Objects.equals(documentType, that.documentType) && Objects.equals(documentNumber, that.documentNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bulstat, documentType, documentNumber);
    }
}