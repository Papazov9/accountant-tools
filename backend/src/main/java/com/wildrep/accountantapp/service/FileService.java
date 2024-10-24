package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.exceptions.ComparisonNotFoundException;
import com.wildrep.accountantapp.exceptions.NoComparisonsLeftException;
import com.wildrep.accountantapp.exceptions.UserDoesNotExistException;
import com.wildrep.accountantapp.model.*;
import com.wildrep.accountantapp.model.dto.OldComparisonReportDTO;
import com.wildrep.accountantapp.repo.*;
import com.wildrep.accountantapp.util.FileServiceUtil;
import com.wildrep.accountantapp.util.InvoiceComparisonUtil;
import com.wildrep.accountantapp.util.XlsxWriterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class FileService {

    private final InvoiceRecordRepository invoiceRecordRepository;
    private final CSVRecordRepository csvRecordRepository;
    private final UserRepository userRepository;
    private final ComparisonRepository comparisonRepository;
    private final ComparisonResultRepository comparisonResultRepository;
    private final MetricsService metricsService;

    public ByteArrayOutputStream processFiles(String username, List<MultipartFile> csvFiles, List<MultipartFile> txtFiles) throws IOException {
        String batchId = UUID.randomUUID().toString();
        List<CSVInvoiceRecord> csvRecords = new ArrayList<>();
        List<InvoiceRecord> invoiceRecords = new ArrayList<>();


        User user = this.userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserDoesNotExistException(username));

        if (user.getComparisonCount() == 0) {
            throw new NoComparisonsLeftException();
        }

        Comparison comparison = Comparison.builder()
                .comparisonDate(LocalDateTime.now())
                .batchId(batchId)
                .user(user)
                .status("IN_PROGRESS")
                .build();
        this.comparisonRepository.saveAndFlush(comparison);
        try {
            for (int i = 0; i < csvFiles.size(); i++) {
                MultipartFile csvFile = csvFiles.get(i);
                MultipartFile txtFile = txtFiles.get(i);


                List<CSVInvoiceRecord> csvInvoiceRecords = FileServiceUtil.parseCsvFile(csvFile);
                List<InvoiceRecord> txtRecords = FileServiceUtil.parseTxtFile(txtFile);
                csvRecordRepository.saveAllAndFlush(csvInvoiceRecords);
                invoiceRecordRepository.saveAllAndFlush(txtRecords);
                csvRecords.addAll(csvInvoiceRecords);
                invoiceRecords.addAll(txtRecords);
            }
        } catch (RuntimeException e) {
            comparison.setStatus("FAILED");
            this.comparisonRepository.save(comparison);
            metricsService.updateMetricsAfterComparison(user, false, 0);
            throw new RuntimeException(e);
        }
        List<ComparisonResult> results = InvoiceComparisonUtil.compareInvoicesAndStoreResults(csvRecords, invoiceRecords);
        this.comparisonResultRepository.saveAllAndFlush(results);
        comparison.setComparisonResults(results);
        this.comparisonRepository.saveAndFlush(comparison);
        comparison.setStatus("COMPLETED");
        comparisonRepository.save(comparison);

        AtomicLong mismatches = new AtomicLong();
        results.forEach(c -> mismatches.addAndGet(c.getMismatchedFields().size()));
        metricsService.updateMetricsAfterComparison(user, true, mismatches.get());
        ByteArrayOutputStream byteArrayOutputStream = XlsxWriterUtil.generateComparisonReport(results);

        user.decrementComparisonCount();
        this.userRepository.saveAndFlush(user);
        return byteArrayOutputStream;
    }

    public OldComparisonReportDTO loadResourceFromOldComparison(String batchId) {
        Comparison comparison = this.comparisonRepository.findByBatchId(batchId)
                .orElseThrow(() -> new ComparisonNotFoundException(batchId));

        ByteArrayOutputStream reportStream = XlsxWriterUtil.generateComparisonReport(comparison.getComparisonResults());
        return new OldComparisonReportDTO(reportStream, comparison.getComparisonDate());
    }
}
