package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.ComparisonResult;
import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.model.enums.InvoiceField;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class XlsxWriterUtil {

    private static final int OFFSET = 4;
    private static final int FLUSH_ROWS = 100;

    public static ByteArrayOutputStream generateComparisonReport(List<ComparisonResult> comparisonResults) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(FLUSH_ROWS)) {

            // Create separate sheets for each type of mismatch
            Sheet allRecordsSheet = workbook.createSheet("All Records");
            Sheet mismatchedSheet = workbook.createSheet("Mismatches");
            Sheet missingInCsvSheet = workbook.createSheet("Missing in CSV");
            Sheet missingInTxtSheet = workbook.createSheet("Missing in TXT");
            Sheet duplicateSheet = workbook.createSheet("Duplicates");

            CellStyle defaultStyle = createDefaultStyle(workbook);
            CellStyle redTextStyle = createRedTextStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Initialize row numbers for each sheet
            int allRecordsRowNum = 0;
            int mismatchedRowNum = 0;
            int missingInCsvRowNum = 0;
            int missingInTxtRowNum = 0;
            int duplicateRowNum = 0;

            // Create headers for all sheets
            createHeaderRowForDetailedRecords(workbook, allRecordsSheet, allRecordsRowNum++, headerStyle);
            createHeaderRowForDetailedRecords(workbook, mismatchedSheet, mismatchedRowNum++, headerStyle);
            createHeaderRowForDetailedRecords(workbook, missingInCsvSheet, missingInCsvRowNum++, headerStyle);
            createHeaderRowForDetailedRecords(workbook, missingInTxtSheet, missingInTxtRowNum++, headerStyle);
            createHeaderRowForDetailedRecords(workbook, duplicateSheet, duplicateRowNum++, headerStyle);

            // Process each comparison result and write it to the appropriate sheet(s)
            for (ComparisonResult comparisonResult : comparisonResults) {
                // Write all records to the "All Records" sheet
                allRecordsRowNum = writeComparisonResultToSheet(comparisonResult, allRecordsSheet, allRecordsRowNum, workbook, defaultStyle, redTextStyle);

                // Sort by comparison status
                switch (comparisonResult.getStatus()) {
                    case MISMATCH -> mismatchedRowNum = writeComparisonResultToSheet(comparisonResult, mismatchedSheet, mismatchedRowNum, workbook, defaultStyle, redTextStyle);
                    case MISSING_IN_CSV -> missingInCsvRowNum = writeComparisonResultToSheet(comparisonResult, missingInCsvSheet, missingInCsvRowNum, workbook, defaultStyle, redTextStyle);
                    case MISSING_IN_TXT -> missingInTxtRowNum = writeComparisonResultToSheet(comparisonResult, missingInTxtSheet, missingInTxtRowNum, workbook, defaultStyle, redTextStyle);
                    case DUPLICATE -> duplicateRowNum = writeComparisonResultToSheet(comparisonResult, duplicateSheet, duplicateRowNum, workbook, defaultStyle, redTextStyle);
                    default -> {}  // Do nothing for matches
                }
            }

            // Adjust column widths for each sheet
            for (int i = 0; i < 4; i++) {
                allRecordsSheet.setColumnWidth(i + OFFSET, 6000);
                mismatchedSheet.setColumnWidth(i + OFFSET, 6000);
                missingInCsvSheet.setColumnWidth(i + OFFSET, 6000);
                missingInTxtSheet.setColumnWidth(i + OFFSET, 6000);
                duplicateSheet.setColumnWidth(i + OFFSET, 6000);
            }

            // Write the workbook to a ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate comparison report: " + e.getMessage(), e);
        }
    }

    private static void createHeaderRowForDetailedRecords(Workbook workbook, Sheet sheet, int rowNum, CellStyle headerStyle) {
        String[] headers = {"Field", "CSV Value", "TXT Value", "Matched"};

        Row headerRow = sheet.createRow(rowNum);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i + OFFSET);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static int writeComparisonResultToSheet(ComparisonResult comparisonResult, Sheet sheet, int rowNum, Workbook workbook, CellStyle defaultStyle, CellStyle redTextStyle) {
        CellStyle boldCenterStyle = createBoldCenterStyle(workbook);

        Row companyRow = sheet.createRow(rowNum++);
        Cell companyCell = companyRow.createCell(OFFSET);
        companyCell.setCellValue(comparisonResult.getCsvInvoiceRecord() != null
                ? comparisonResult.getCsvInvoiceRecord().getCompanyName()
                : comparisonResult.getTxtInvoiceRecord().getCompanyName());

        CellRangeAddress mergedCells = new CellRangeAddress(rowNum - 1, rowNum - 1, OFFSET, OFFSET + 3);
        sheet.addMergedRegion(mergedCells);
        companyCell.setCellStyle(boldCenterStyle);

        for (InvoiceField field : InvoiceField.values()) {
            String csvValue = InvoiceComparisonUtil.getFieldValue(comparisonResult.getCsvInvoiceRecord(), field);
            String txtValue = InvoiceComparisonUtil.getFieldValue(comparisonResult.getTxtInvoiceRecord(), field);
            CellStyle styleToApply = (comparisonResult.getMismatchedFields().contains(field)) ? redTextStyle : defaultStyle;

            // Write each record detail (field, csvValue, txtValue, matched or not)
            rowNum = writeRecordDetails(sheet, rowNum, csvValue, txtValue, styleToApply, comparisonResult.getMismatchedFields(), field.name(), defaultStyle);
        }

        // Add a separator row
        Row separatorRow = sheet.createRow(rowNum++);
        Cell separatorCell = separatorRow.createCell(OFFSET);
        CellStyle separatorStyle = workbook.createCellStyle();
        separatorStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        separatorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        separatorCell.setCellStyle(separatorStyle);

        for (int i = OFFSET + 1; i <= OFFSET + 3; i++) {
            Cell cell = separatorRow.createCell(i);
            cell.setCellStyle(separatorStyle);
        }

        return rowNum;
    }

    private static int writeRecordDetails(Sheet sheet, int rowNum, String csvValue, String txtValue, CellStyle styleToApply, Set<InvoiceField> mismatchedFields, String fieldName, CellStyle defaultStyle) {
        Row row = sheet.createRow(rowNum++);
        Cell fieldCell = row.createCell(OFFSET);
        fieldCell.setCellValue(fieldName);
        fieldCell.setCellStyle(defaultStyle);

        Cell csvCell = row.createCell(OFFSET + 1);
        csvCell.setCellValue(csvValue != null ? csvValue : "Missing");
        csvCell.setCellStyle(styleToApply);

        Cell txtCell = row.createCell(OFFSET + 2);
        txtCell.setCellValue(txtValue != null ? txtValue : "Missing");
        txtCell.setCellStyle(styleToApply);

        Cell matchedCell = row.createCell(OFFSET + 3);
        matchedCell.setCellValue(csvValue != null && csvValue.equals(txtValue) ? "Yes" : "No");
        matchedCell.setCellStyle(styleToApply);

        return rowNum;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        return headerStyle;
    }

    private static CellStyle createBoldCenterStyle(Workbook workbook) {
        CellStyle boldCenterStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldCenterStyle.setFont(boldFont);
        boldCenterStyle.setAlignment(HorizontalAlignment.CENTER);
        return boldCenterStyle;
    }

    private static CellStyle createRedTextStyle(Workbook workbook) {
        CellStyle redTextStyle = workbook.createCellStyle();
        Font redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        redTextStyle.setFont(redFont);
        return redTextStyle;
    }

    private static CellStyle createDefaultStyle(Workbook workbook) {
        return workbook.createCellStyle();  // Can be customized if needed
    }
}