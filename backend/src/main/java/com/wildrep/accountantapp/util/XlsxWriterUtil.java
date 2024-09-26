package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.ComparisonResult;
import com.wildrep.accountantapp.model.enums.InvoiceField;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class XlsxWriterUtil {

    private static final String FILE_STORAGE_DIRECTORY = "/opt/app/comparison-reports/";
    private static final int OFFSET = 4;

    public static String generateComparisonReport(List<ComparisonResult> comparisonResults) {

        ensureDirectoryExists();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = FILE_STORAGE_DIRECTORY + "ComparisonResults_" + timestamp + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Comparison Report");
            int rowNum = 0;

            //header
            createHeaderRow(workbook, sheet, rowNum++);

            CellStyle redCellStyle = workbook.createCellStyle();
            Font redFont = workbook.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            redCellStyle.setFont(redFont);

            CellStyle blackBackgroundStyle = workbook.createCellStyle();
            blackBackgroundStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            blackBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            //main contant
            for (ComparisonResult comparisonResult : comparisonResults) {
                rowNum = addCompanyNameAsTitle(comparisonResult, rowNum, sheet);
                rowNum = writeComparisonResultToSheet(comparisonResult, sheet, rowNum, redCellStyle);
                rowNum = addSeparator(blackBackgroundStyle, rowNum, sheet);
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i + OFFSET);
            }

            try(FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filePath;
    }

    private static int addCompanyNameAsTitle(ComparisonResult comparisonResult, int rowNum, Sheet sheet) {
        Row row = sheet.createRow(rowNum);

        String companyName = comparisonResult.getCsvInvoiceRecord() != null
                ? comparisonResult.getCsvInvoiceRecord().getCompanyName()
                : comparisonResult.getTxtInvoiceRecord() != null
                ? comparisonResult.getTxtInvoiceRecord().getCompanyName()
                : "Unknown Company";

        Cell cell = row.createCell(OFFSET);
        cell.setCellValue(companyName);

        CellRangeAddress mergedCells = new CellRangeAddress(rowNum, rowNum, OFFSET, OFFSET + 3);
        sheet.addMergedRegion(mergedCells);

        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14); // Make the font larger for the title
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(titleStyle);

        return rowNum + 1;
    }

    private static int addSeparator(CellStyle blackBackgroundStyle, int rowNum, Sheet sheet) {
        Row row = sheet.createRow(rowNum++);
        for (int i = 0; i < 4; i++) {
            Cell cell = row.createCell(i + OFFSET);
            cell.setCellStyle(blackBackgroundStyle);
        }

        return rowNum;
    }

    private static void ensureDirectoryExists() {
        Path path = Paths.get(XlsxWriterUtil.FILE_STORAGE_DIRECTORY);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + XlsxWriterUtil.FILE_STORAGE_DIRECTORY, e);
            }
        }
    }

    private static void createHeaderRow(Workbook workbook, Sheet sheet, int rowNum) {
        String[] headers = {"Field", "CSV Value", "TXT Value", "Matched"};

        Row headerRow = sheet.createRow(rowNum);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i + OFFSET);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private static int writeComparisonResultToSheet(ComparisonResult comparisonResult, Sheet sheet, int rowNum, CellStyle redCellStyle) {

        for (InvoiceField field : InvoiceField.values()) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(OFFSET);
            cell.setCellValue(field.name());

            Cell csvCell = row.createCell(1 + OFFSET);
            Cell txtCell = row.createCell(2 + OFFSET);
            Cell matchedCell = row.createCell(3 + OFFSET);

            switch (comparisonResult.getStatus()) {
                case MISSING_IN_CSV:
                    csvCell.setCellValue("CSV Record is missing");
                    csvCell.setCellStyle(redCellStyle);
                    break;
                case MISSING_IN_TXT:
                    txtCell.setCellValue("TXT Record is missing");
                    txtCell.setCellStyle(redCellStyle);
                    break;
                case MATCH:
                    String csvValueMatched = InvoiceComparisonUtil.getFieldValue(comparisonResult.getCsvInvoiceRecord(), field);
                    String txtValueMatched = InvoiceComparisonUtil.getFieldValue(comparisonResult.getTxtInvoiceRecord(), field);
                    csvCell.setCellValue(csvValueMatched);
                    txtCell.setCellValue(txtValueMatched);

                    matchedCell.setCellValue("Yes");
                    break;
                case MISMATCH:
                    String csvValue = InvoiceComparisonUtil.getFieldValue(comparisonResult.getCsvInvoiceRecord(), field);
                    String txtValue = InvoiceComparisonUtil.getFieldValue(comparisonResult.getTxtInvoiceRecord(), field);
                    csvCell.setCellValue(csvValue);
                    txtCell.setCellValue(txtValue);

                    if (comparisonResult.getMismatchedFields().contains(field)) {
                        csvCell.setCellStyle(redCellStyle);
                        txtCell.setCellStyle(redCellStyle);
                        matchedCell.setCellValue("No");
                    } else {
                        matchedCell.setCellValue("Yes");
                    }
                    break;
                default:
            }
        }
        return rowNum;
    }

}
