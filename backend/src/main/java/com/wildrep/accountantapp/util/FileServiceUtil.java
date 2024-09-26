package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.CSVInvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecordId;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileServiceUtil {

    private static final String BULSTAT_REGEX = "([A-Z]{2}\\d{9}|\\d{9})";
    private static final String DOCUMENT_REGEX = "(\\d{2}/\\d{2}/\\d{4})([A-Z]{2}\\d{9}|\\d{9})";
    private static final String DOCUMENT_PATTERN = "(.*?)(\\d{2})(\\d{10})$";
    private static final Set<String> POSSIBLE_DOC_TYPES = Set.of("01", "02", "03");

    public static List<CSVInvoiceRecord> parseCsvFile(MultipartFile csvFile) throws IOException {
        List<CSVInvoiceRecord> csvResults = new ArrayList<>();
        String encoding = detectFileEncoding(new ByteArrayInputStream(csvFile.getBytes()));
        InputStream utf8InputStream = convertToUTF8(csvFile.getInputStream(), encoding);

        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(utf8InputStream, Charset.forName(encoding)))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withIgnoreHeaderCase()
                    .withIgnoreEmptyLines();
            Pattern bulstatPattern = Pattern.compile(BULSTAT_REGEX);

            try (CSVParser csvParser = new CSVParser(bfr, csvFormat)) {
                for (CSVRecord record : csvParser) {
                    String possibleBulstat = record.get(0);
                    if (!bulstatPattern.matcher(possibleBulstat).matches()){
                        continue;
                    }

                    String docType = String.format("%02d", Integer.parseInt(record.get(3).trim()));
                    if (!POSSIBLE_DOC_TYPES.contains(docType)){
                        continue;
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    InvoiceRecordId invoiceRecordId = new InvoiceRecordId(record.get(0).trim(), docType, String.format("%010d", Long.parseLong(record.get(4).trim())));
                    CSVInvoiceRecord invoiceRecord = CSVInvoiceRecord.builder()
                            .id(invoiceRecordId)
                            .companyName(record.get(1).trim())
                            .accountingPeriod(record.get(2).trim())
                            .issueDate(dateFormat.parse(record.get(5).trim()))
                            .totalAmount(calculateTotalAmount(record))
                            .vatAmount(parseDouble(record.get(10)))
                            .build();
                    csvResults.add(invoiceRecord);
                }
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return csvResults;
    }

    private static Double calculateTotalAmount(CSVRecord record) {
        double result = 0;
        for (int i = 9; i < 15; i++) {
            result += Double.parseDouble(record.get(i));
        }

        return Double.parseDouble(String.format(Locale.US,"%.2f", result));
    }

    public static List<InvoiceRecord> parseTxtFile(MultipartFile txtFile) throws IOException {
        List<InvoiceRecord> txtResults = new ArrayList<>();
        String encoding = detectFileEncoding(new ByteArrayInputStream(txtFile.getBytes()));
        InputStream utf8InputStream = convertToUTF8(txtFile.getInputStream(), encoding);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(utf8InputStream, StandardCharsets.UTF_8))) {
            String line;
            Pattern documentPattern = Pattern.compile(DOCUMENT_PATTERN);
            Pattern dateBulstat = Pattern.compile(DOCUMENT_REGEX);

            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\\s{3,}");
                String documentInfo = columns[3].trim();
                Matcher docMatcher = documentPattern.matcher(documentInfo);
                String docType;
                String docNumber;
                if (docMatcher.find()) {
                    docType = docMatcher.group(2);
                    docNumber = docMatcher.group(3);
                } else {
                    continue;
                }

                if (!POSSIBLE_DOC_TYPES.contains(docType)) {
                    continue;
                }

                String accountingPeriod = columns[1].trim();
                String bulstatDateInfo = columns[4].trim();
                Matcher bulstatMatcher = dateBulstat.matcher(bulstatDateInfo);
                String issueDate;
                String bulstat;

                if (bulstatMatcher.find()) {
                    issueDate = bulstatMatcher.group(1);
                    bulstat = bulstatMatcher.group(2);
                } else {
                    throw new RuntimeException("Invalid issue date and Bulstat format: " + bulstatDateInfo);
                }

                SimpleDateFormat txtDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date issueDateFormatted = txtDateFormat.parse(issueDate);

                InvoiceRecordId invoiceRecordId = new InvoiceRecordId(bulstat, docType, docNumber);
                InvoiceRecord invoiceRecord = InvoiceRecord.builder()
                        .id(invoiceRecordId)
                        .companyName(columns[5].trim())
                        .accountingPeriod(accountingPeriod)
                        .issueDate(issueDateFormatted)
                        .totalAmount(calculateTXTAmount(columns))
                        .vatAmount(parseDouble(columns[9]))
                        .build();
                txtResults.add(invoiceRecord);
            }
        } catch (IOException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }

        return txtResults;
    }

    private static Double calculateTXTAmount(String[] columns) {
        double res = 0;

        for (int i = 7; i < 13; i++) {
            res += Double.parseDouble(columns[i]);
        }
        return Double.parseDouble(String.format(Locale.US,"%.2f", res));
    }

    public static InputStream convertToUTF8(InputStream inputStream, String encoding) {
        if (!"UTF-8".equalsIgnoreCase(encoding)) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(encoding));
            StringBuilder utf8Content = new StringBuilder();

            try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    utf8Content.append(line).append("\n");  // Convert line by line to UTF-8
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
