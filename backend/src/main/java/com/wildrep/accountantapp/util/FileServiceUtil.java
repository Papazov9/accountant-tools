package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.CSVInvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecordId;
import com.wildrep.accountantapp.repo.CSVRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileServiceUtil {

    private final CSVRecordRepository csvRecordRepository;

    private static final String FULL_TXT_REGEX = "(BG\\d{9})\\s+(\\d{6,7})\\s+(?:\\d\\s+)?(\\d+)(0[1-3])\\s*(\\d{10})\\s*(\\d{2}\\/\\d{2}\\/\\d{4})((?:BG)?\\d{9})\\s+(.+?)\\s{2,}\\s+([\\p{L}\\s\\-_=+]+)\\s{2,}\\s+(-?\\d+\\.\\d{2})\\s+(-?\\d+\\.\\d{2})\\s+(-?\\d+\\.\\d{2})\\s+(-?\\d+\\.\\d{2})\\s+(-?\\d+\\.\\d{2})\\s+(-?\\d+\\.\\d{2})\\s+(-?\\d+\\.\\d{2})";

    private static final String BULSTAT_REGEX = "([A-Z]{2}\\d{9}|\\d{9})";
    private static final Set<String> POSSIBLE_DOC_TYPES = Set.of("01", "02", "03");
    private static List<CSVInvoiceRecord> currentCSVRecords = new ArrayList<>();

    public FileServiceUtil(CSVRecordRepository csvRecordRepository) {
        this.csvRecordRepository = csvRecordRepository;
    }

    public static List<CSVInvoiceRecord> parseCsvFile(MultipartFile csvFile) throws IOException {
        List<CSVInvoiceRecord> csvResults = new ArrayList<>();
        String encoding = detectFileEncoding(new ByteArrayInputStream(csvFile.getBytes()));
        InputStream utf8InputStream = convertToUTF8(csvFile.getInputStream(), encoding);

        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(utf8InputStream, StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(';').withIgnoreHeaderCase().withIgnoreEmptyLines();
            Pattern bulstatPattern = Pattern.compile(BULSTAT_REGEX);

            try (CSVParser csvParser = new CSVParser(bfr, csvFormat)) {
                for (CSVRecord record : csvParser) {
                    String possibleBulstat = record.get(0);
                    if (!bulstatPattern.matcher(possibleBulstat).matches()) {
                        continue;
                    }

                    String docType = String.format("%02d", Integer.parseInt(record.get(3).trim()));
                    if (!POSSIBLE_DOC_TYPES.contains(docType)) {
                        continue;
                    }

                    String docNumber = record.get(4).trim();
                    String formattedDocNumber;
                    try {
                        formattedDocNumber = String.format("%010d", Long.parseLong(docNumber));
                    } catch (NumberFormatException e) {
                        formattedDocNumber = "Invalid document number";
                    }
                    InvoiceRecordId invoiceRecordId = new InvoiceRecordId(record.get(0).trim(), docType, formattedDocNumber);
                    CSVInvoiceRecord csvInvoiceRecord = CSVInvoiceRecord
                            .builder()
                            .id(invoiceRecordId)
                            .companyName(record.get(1).trim())
                            .accountingPeriod(record.get(2).trim())
                            .issueDate(DateParser.parseDate(record.get(5).trim()))
                            .totalAmount(calculateTotalAmount(record))
                            .vatAmount(parseDouble(record.get(10))).build();
                    csvResults.add(csvInvoiceRecord);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        currentCSVRecords = csvResults;
        return csvResults;
    }

    private static Double calculateTotalAmount(CSVRecord record) {
        double result = 0;
        for (int i = 9; i < 15; i++) {
            result += Double.parseDouble(record.get(i).trim());
        }

        return Double.parseDouble(String.format(Locale.US, "%.2f", result));
    }

    public static List<InvoiceRecord> parseTxtFile(MultipartFile txtFile) throws IOException {
        List<InvoiceRecord> txtResults = new ArrayList<>();
        String encoding = detectFileEncoding(new ByteArrayInputStream(txtFile.getBytes()));
        InputStream utf8InputStream = convertToUTF8(txtFile.getInputStream(), encoding);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(utf8InputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                Pattern fullTxtRegexPattern = Pattern.compile(FULL_TXT_REGEX);
                Matcher matcher = fullTxtRegexPattern.matcher(line.trim());
                if (matcher.matches()) {
                    String accountingPeriod = validateAccountingPeriod(matcher.group(2).trim());
                    String docType = matcher.group(4).trim();
                    if (!POSSIBLE_DOC_TYPES.contains(docType)) {
                        continue;
                    }
                    String docNumber = matcher.group(5).trim();
                    String bulstat = matcher.group(7).trim();
                    if (!bulstat.startsWith("BG")) {
                        if (verifyIfOldRecord(bulstat, docNumber, docType)) {
                            continue;
                        } else {
                            bulstat = "BG" + bulstat;
                        }
                    }
                    String companyName = matcher.group(8).trim();
                    Double vatAmount = Double.parseDouble(matcher.group(12).trim());
                    Double totalAmount = calculateTXTAmount(matcher);
                    InvoiceRecordId invoiceRecordId = new InvoiceRecordId(bulstat, docType, docNumber);
                    InvoiceRecord invoiceRecord = InvoiceRecord
                            .builder()
                            .id(invoiceRecordId)
                            .companyName(companyName)
                            .accountingPeriod(accountingPeriod)
                            .issueDate(DateParser.parseDate(matcher.group(6).trim()))
                            .totalAmount(totalAmount)
                            .vatAmount(vatAmount).build();
                    txtResults.add(invoiceRecord);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return txtResults;
    }

    private static boolean verifyIfOldRecord(String bulstat, String docNumber, String docType) {
        bulstat = "BG" + bulstat;

        String finalBulstat = bulstat;
        return currentCSVRecords.stream().filter(c -> (c.getId().getBulstat().equals(finalBulstat) && c.getId().getDocumentNumber().equals(docNumber))).toList().isEmpty();
    }

    private static String validateAccountingPeriod(String accountingPeriod) {
        String result = accountingPeriod;
        if (accountingPeriod.length() == 7 && accountingPeriod.endsWith("0")) {
            result = accountingPeriod.substring(0, 6);
        }

        return result;
    }

    private static Double calculateTXTAmount(Matcher columns) {
        double result = 0.0;
        for (int i = 10; i < 17; i++) {
            result += parseDouble(columns.group(i).trim());
        }

        return Double.parseDouble(String.format(Locale.US, "%.2f", result));
    }

    public static InputStream convertToUTF8(InputStream inputStream, String encoding) {
        if (!"UTF-8".equalsIgnoreCase(encoding)) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(encoding));
            StringBuilder utf8Content = new StringBuilder();

            try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    utf8Content.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new ByteArrayInputStream(utf8Content.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            return inputStream;
        }
    }

    public static String detectFileEncoding(InputStream inputStream) throws IOException {
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(inputStream);
        CharsetMatch charsetMatch = charsetDetector.detect();

        if (charsetMatch != null) {
            return charsetMatch.getName();
        }

        return StandardCharsets.UTF_8.toString();
    }

    private static Double parseDouble(String value) {
        return value != null && !value.trim().isEmpty() ? Double.parseDouble(value.trim()) : 0.0;
    }
}
