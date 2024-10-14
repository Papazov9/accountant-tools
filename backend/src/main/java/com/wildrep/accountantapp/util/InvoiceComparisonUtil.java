package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.*;
import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.model.enums.InvoiceField;

import java.util.*;
import java.util.stream.Collectors;

public class InvoiceComparisonUtil {

    public static List<ComparisonResult> compareInvoicesAndStoreResults(List<CSVInvoiceRecord> csvRecords, List<InvoiceRecord> txtRecords) {
        List<ComparisonResult> results = new ArrayList<>();

        // Group CSV and TXT records by their IDs
        Map<InvoiceRecordId, List<CSVInvoiceRecord>> csvRecordMap = csvRecords.stream()
                .collect(Collectors.groupingBy(CSVInvoiceRecord::getId));
        Map<InvoiceRecordId, List<InvoiceRecord>> txtRecordMap = txtRecords.stream()
                .collect(Collectors.groupingBy(InvoiceRecord::getId));

        // Combine all keys from both CSV and TXT maps
        Set<InvoiceRecordId> allKeys = new HashSet<>();
        allKeys.addAll(csvRecordMap.keySet());
        allKeys.addAll(txtRecordMap.keySet());

        // Iterate through all unique keys and compare records
        for (InvoiceRecordId key : allKeys) {
            List<CSVInvoiceRecord> csvRecordList = csvRecordMap.getOrDefault(key, Collections.emptyList());
            List<InvoiceRecord> txtRecordList = txtRecordMap.getOrDefault(key, Collections.emptyList());

            boolean isDuplicate = csvRecordList.size() > 1 || txtRecordList.size() > 1;

            // Compare CSV and TXT records for each combination
            for (CSVInvoiceRecord csvRecord : csvRecordList) {
                if (!txtRecordList.isEmpty()) {
                    for (InvoiceRecord txtRecord : txtRecordList) {
                        results.add(compareAndCreateResult(csvRecord, txtRecord, isDuplicate));
                    }
                } else {
                    // TXT record is missing
                    results.add(createMissingTxtResult(csvRecord));
                }
            }

            // Handle cases where the CSV record is missing
            for (InvoiceRecord txtRecord : txtRecordList) {
                if (csvRecordList.isEmpty()) {
                    results.add(createMissingCsvResult(txtRecord));
                }
            }
        }

        return results;
    }

    private static ComparisonResult compareAndCreateResult(CSVInvoiceRecord csvRecord, InvoiceRecord txtRecord, boolean isDuplicate) {
        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setCsvInvoiceRecord(csvRecord);
        comparisonResult.setTxtInvoiceRecord(txtRecord);

        if (isDuplicate) {
            comparisonResult.setStatus(ComparisonStatus.DUPLICATE);
            comparisonResult.setMatchColumn("Duplicate");
            comparisonResult.setMismatchedFields(Arrays.stream(InvoiceField.values()).collect(Collectors.toSet()));
        } else {
            Set<InvoiceField> mismatchedFields = compareFields(csvRecord, txtRecord);
            if (mismatchedFields.isEmpty()) {
                comparisonResult.setStatus(ComparisonStatus.MATCH);
                comparisonResult.setMismatchedFields(new HashSet<>());
            } else {
                comparisonResult.setStatus(ComparisonStatus.MISMATCH);
                comparisonResult.setMismatchedFields(mismatchedFields);
            }
        }

        return comparisonResult;
    }

    private static ComparisonResult createMissingTxtResult(CSVInvoiceRecord csvRecord) {
        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setCsvInvoiceRecord(csvRecord);
        comparisonResult.setStatus(ComparisonStatus.MISSING_IN_TXT);
        comparisonResult.setMismatchedFields(Arrays.stream(InvoiceField.values()).collect(Collectors.toSet()));
        return comparisonResult;
    }

    private static ComparisonResult createMissingCsvResult(InvoiceRecord txtRecord) {
        ComparisonResult comparisonResult = new ComparisonResult();
        comparisonResult.setTxtInvoiceRecord(txtRecord);
        comparisonResult.setStatus(ComparisonStatus.MISSING_IN_CSV);
        comparisonResult.setMismatchedFields(Arrays.stream(InvoiceField.values()).collect(Collectors.toSet()));
        return comparisonResult;
    }

    private static Set<InvoiceField> compareFields(CSVInvoiceRecord csvRecord, InvoiceRecord txtRecord) {
        Set<InvoiceField> mismatchedFields = new HashSet<>();

        for (InvoiceField field : InvoiceField.values()) {
            String csvValue = getFieldValue(csvRecord, field);
            String txtValue = getFieldValue(txtRecord, field);

            if (!areValuesEqual(csvValue, txtValue)) {
                mismatchedFields.add(field);
            }
        }

        return mismatchedFields;
    }

    private static boolean areValuesEqual(String csvValue, String txtValue) {
        return Objects.equals(csvValue != null ? csvValue.trim() : null, txtValue != null ? txtValue.trim() : null);
    }

    public static String getFieldValue(CSVInvoiceRecord csvRecord, InvoiceField field) {
        if (csvRecord == null) return "";
        return switch (field) {
            case BULSTAT -> csvRecord.getId() != null ? csvRecord.getId().getBulstat() : "";
            case ACCOUNTING_PERIOD -> csvRecord.getAccountingPeriod() != null ? csvRecord.getAccountingPeriod() : "";
            case DOCUMENT_TYPE -> csvRecord.getId() != null ? csvRecord.getId().getDocumentType() : "";
            case DOCUMENT_NUMBER -> csvRecord.getId() != null ? csvRecord.getId().getDocumentNumber() : "";
            case ISSUE_DATE -> csvRecord.getIssueDate() != null ? csvRecord.getIssueDate().toString() : "";
            case TOTAL_AMOUNT -> csvRecord.getTotalAmount() != null ? String.valueOf(csvRecord.getTotalAmount()) : "";
            case VAT_AMOUNT -> csvRecord.getVatAmount() != null ? String.valueOf(csvRecord.getVatAmount()) : "";
        };
    }

    public static String getFieldValue(InvoiceRecord txtRecord, InvoiceField field) {
        if (txtRecord == null) return "";
        return switch (field) {
            case BULSTAT -> txtRecord.getId() != null ? txtRecord.getId().getBulstat() : "";
            case ACCOUNTING_PERIOD -> txtRecord.getAccountingPeriod() != null ? txtRecord.getAccountingPeriod() : "";
            case DOCUMENT_TYPE -> txtRecord.getId() != null ? txtRecord.getId().getDocumentType() : "";
            case DOCUMENT_NUMBER -> txtRecord.getId() != null ? txtRecord.getId().getDocumentNumber() : "";
            case ISSUE_DATE -> txtRecord.getIssueDate() != null ? txtRecord.getIssueDate().toString() : "";
            case TOTAL_AMOUNT -> txtRecord.getTotalAmount() != null ? String.valueOf(txtRecord.getTotalAmount()) : "";
            case VAT_AMOUNT -> txtRecord.getVatAmount() != null ? String.valueOf(txtRecord.getVatAmount()) : "";
        };
    }
}