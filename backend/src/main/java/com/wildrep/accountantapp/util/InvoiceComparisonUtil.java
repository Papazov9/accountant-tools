package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.CSVInvoiceRecord;
import com.wildrep.accountantapp.model.ComparisonResult;
import com.wildrep.accountantapp.model.InvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecordId;
import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.model.enums.InvoiceField;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InvoiceComparisonUtil {
    public static List<ComparisonResult> compareInvoices(List<CSVInvoiceRecord> csvInvoiceRecords, List<InvoiceRecord> txtInvoiceRecords) {
        List<ComparisonResult> results = new ArrayList<>();

        Map<InvoiceRecordId, CSVInvoiceRecord> csvrecordMap = csvInvoiceRecords.stream().collect(Collectors.toMap(CSVInvoiceRecord::getId, Function.identity()));
        Map<InvoiceRecordId, InvoiceRecord> txtRecordMap = txtInvoiceRecords.stream().collect(Collectors.toMap(InvoiceRecord::getId, Function.identity()));

        Set<InvoiceRecordId> allKeys = new HashSet<>();
        allKeys.addAll(csvrecordMap.keySet());
        allKeys.addAll(txtRecordMap.keySet());

        for (InvoiceRecordId key : allKeys) {
            CSVInvoiceRecord csvInvoiceRecord = csvrecordMap.get(key);
            InvoiceRecord txtInvoiceRecord = txtRecordMap.get(key);

            ComparisonResult.ComparisonResultBuilder comparisonResultBuilder = ComparisonResult.builder()
                    .invoiceRecordId(key)
                    .csvInvoiceRecord(csvInvoiceRecord)
                    .txtInvoiceRecord(txtInvoiceRecord);

            if (csvInvoiceRecord != null && txtInvoiceRecord != null) {
                Set<InvoiceField> mismatchedFields = compareFields(csvInvoiceRecord, txtInvoiceRecord);
                if (mismatchedFields.isEmpty()) {
                    comparisonResultBuilder
                            .status(ComparisonStatus.MATCH);
                } else {
                    comparisonResultBuilder
                            .status(ComparisonStatus.MISMATCH)
                            .mismatchedFields(mismatchedFields);
                }
            } else if (csvInvoiceRecord != null) {
                comparisonResultBuilder
                        .status(ComparisonStatus.MISSING_IN_TXT);
            } else {
                comparisonResultBuilder
                        .status(ComparisonStatus.MISSING_IN_CSV);
            }

            results.add(comparisonResultBuilder.build());
        }

        return results;
    }

    private static Set<InvoiceField> compareFields(CSVInvoiceRecord csvInvoiceRecord, InvoiceRecord txtInvoiceRecord) {
        Set<InvoiceField> mismatchedFields = new HashSet<>();

        for (InvoiceField field : InvoiceField.values()) {
            String csvValue = getFieldValue(csvInvoiceRecord, field);
            String txtValue = getFieldValue(txtInvoiceRecord, field);
            if (!Objects.equals(csvValue, txtValue)) {
                mismatchedFields.add(field);
            }
        }
        return mismatchedFields;
    }


    public static String getFieldValue(InvoiceRecord txtInvoiceRecord, InvoiceField field) {
        return switch (field) {
            case BULSTAT -> txtInvoiceRecord.getId().getBulstat();
            case ACCOUNTING_PERIOD -> txtInvoiceRecord.getAccountingPeriod();
            case DOCUMENT_TYPE -> txtInvoiceRecord.getId().getDocumentType();
            case DOCUMENT_NUMBER -> txtInvoiceRecord.getId().getDocumentNumber();
            case ISSUE_DATE -> txtInvoiceRecord.getIssueDate() != null
                    ? new SimpleDateFormat("dd-MM-yyyy").format(txtInvoiceRecord.getIssueDate())
                    : "";
            case TOTAL_AMOUNT ->
                    txtInvoiceRecord.getTotalAmount() != null ? String.valueOf(txtInvoiceRecord.getTotalAmount()) : "";
            case VAT_AMOUNT ->
                    txtInvoiceRecord.getVatAmount() != null ? String.valueOf(txtInvoiceRecord.getVatAmount()) : "";
            };
    }

    public static String getFieldValue(CSVInvoiceRecord csvInvoiceRecord, InvoiceField field) {
        return switch (field) {
            case BULSTAT -> csvInvoiceRecord.getId().getBulstat();
            case ACCOUNTING_PERIOD -> csvInvoiceRecord.getAccountingPeriod();
            case DOCUMENT_TYPE -> csvInvoiceRecord.getId().getDocumentType();
            case DOCUMENT_NUMBER -> csvInvoiceRecord.getId().getDocumentNumber();
            case ISSUE_DATE -> csvInvoiceRecord.getIssueDate() != null
                    ? new SimpleDateFormat("dd-MM-yyyy").format(csvInvoiceRecord.getIssueDate())
                    : "";
            case TOTAL_AMOUNT ->
                    csvInvoiceRecord.getTotalAmount() != null ? String.valueOf(csvInvoiceRecord.getTotalAmount()) : "";
            case VAT_AMOUNT ->
                    csvInvoiceRecord.getVatAmount() != null ? String.valueOf(csvInvoiceRecord.getVatAmount()) : "";
            };
    }

}
