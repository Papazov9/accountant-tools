package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.*;
import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.model.enums.InvoiceField;

import java.util.*;
import java.util.stream.Collectors;

public class InvoiceComparisonUtil {

    public static List<ComparisonResult> compareInvoicesAndStoreResults(List<CSVInvoiceRecord> csvRecords, List<InvoiceRecord> txtRecords) {
        List<ComparisonResult> results = new ArrayList<>();

        Map<InvoiceRecordId, List<CSVInvoiceRecord>> csvRecordMap = csvRecords.stream()
                .collect(Collectors.groupingBy(CSVInvoiceRecord::getId));
        Map<InvoiceRecordId, List<InvoiceRecord>> txtRecordMap = txtRecords.stream()
                .collect(Collectors.groupingBy(InvoiceRecord::getId));

        Set<CSVInvoiceRecord> unmatchedCSVs = new HashSet<>(csvRecords);
        Set<InvoiceRecord> unmatchedTxts = new HashSet<>(txtRecords);

        Set<InvoiceRecordId> allKeys = new HashSet<>();
        allKeys.addAll(csvRecordMap.keySet());
        allKeys.addAll(txtRecordMap.keySet());

        for (InvoiceRecordId key : allKeys) {
            List<CSVInvoiceRecord> csvRecordList = csvRecordMap.getOrDefault(key, Collections.emptyList());
            List<InvoiceRecord> txtRecordList = txtRecordMap.getOrDefault(key, Collections.emptyList());

            boolean isDuplicate = csvRecordList.size() > 1 || txtRecordList.size() > 1;

            for (CSVInvoiceRecord csvRecord : csvRecordList) {
                if (!txtRecordList.isEmpty()) {
                    for (InvoiceRecord txtRecord : txtRecordList) {
                        unmatchedCSVs.remove(csvRecord);
                        unmatchedTxts.remove(txtRecord);
                        results.add(compareAndCreateResult(csvRecord, txtRecord, isDuplicate));
                    }
                }
            }
        }

        Map<String, List<CSVInvoiceRecord>> unmatchedNewGroupingCSV = unmatchedCSVs.stream()
                .collect(Collectors.groupingBy(record -> record.getId().getBulstat() + "|" + record.getId().getDocumentType() + "|" + record.getTotalAmount()));
        Map<String, List<InvoiceRecord>> unmatchedNewGroupingTXT = unmatchedTxts.stream()
                .collect(Collectors.groupingBy(record -> record.getId().getBulstat() + "|" + record.getId().getDocumentType() + "|" + record.getTotalAmount()));

        Set<String> allUnmatchedKeys = new HashSet<>();
        allUnmatchedKeys.addAll(unmatchedNewGroupingCSV.keySet());
        allUnmatchedKeys.addAll(unmatchedNewGroupingTXT.keySet());

        for (String key : allUnmatchedKeys) {
            List<CSVInvoiceRecord> csvInvoiceRecords = unmatchedNewGroupingCSV.getOrDefault(key, Collections.emptyList());
            List<InvoiceRecord> invoiceRecords = unmatchedNewGroupingTXT.getOrDefault(key, Collections.emptyList());

            for (CSVInvoiceRecord csvInvoiceRecord : csvInvoiceRecords) {
                for (InvoiceRecord invoiceRecord : invoiceRecords) {
                    unmatchedCSVs.remove(csvInvoiceRecord);
                    unmatchedTxts.remove(invoiceRecord);
                    results.add(compareAndCreateResult(csvInvoiceRecord, invoiceRecord, false));
                }
            }
        }

        unmatchedCSVs.forEach(r -> results.add(createMissingTxtResult(r)));
        unmatchedTxts.forEach(r -> results.add(createMissingCsvResult(r)));
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