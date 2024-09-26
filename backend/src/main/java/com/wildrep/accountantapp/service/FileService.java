package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.model.CSVInvoiceRecord;
import com.wildrep.accountantapp.model.ComparisonResult;
import com.wildrep.accountantapp.model.InvoiceRecord;
import com.wildrep.accountantapp.model.InvoiceRecordId;
import com.wildrep.accountantapp.model.enums.ComparisonStatus;
import com.wildrep.accountantapp.repo.CSVRecordRepository;
import com.wildrep.accountantapp.repo.InvoiceRecordRepository;
import com.wildrep.accountantapp.util.FileServiceUtil;
import com.wildrep.accountantapp.util.InvoiceComparisonUtil;
import com.wildrep.accountantapp.util.XlsxWriterUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final InvoiceRecordRepository invoiceRecordRepository;
    private final CSVRecordRepository csvRecordRepository;

    public String processFiles(List<MultipartFile> csvFiles, List<MultipartFile> txtFiles) throws IOException {
        List<ComparisonResult> comparisonResults = new ArrayList<>();
        String batchId = UUID.randomUUID().toString();

        for (int i = 0; i < csvFiles.size(); i++) {
            MultipartFile csvFile = csvFiles.get(i);
            MultipartFile txtFile = txtFiles.get(i);

            List<CSVInvoiceRecord> csvInvoiceRecords = FileServiceUtil.parseCsvFile(csvFile);
            csvInvoiceRecords.forEach(r -> r.setBatchId(batchId));
            List<InvoiceRecord> txtInvoiceRecords = FileServiceUtil.parseTxtFile(txtFile);
            txtInvoiceRecords.forEach(r -> r.setBatchId(batchId));

            this.csvRecordRepository.saveAllAndFlush(csvInvoiceRecords);
            this.invoiceRecordRepository.saveAllAndFlush(txtInvoiceRecords);

            comparisonResults = InvoiceComparisonUtil.compareInvoices(csvRecordRepository.findAllByBatchId(batchId), invoiceRecordRepository.findAllByBatchId(batchId));
        }


        return XlsxWriterUtil.generateComparisonReport(comparisonResults);
    }


}
