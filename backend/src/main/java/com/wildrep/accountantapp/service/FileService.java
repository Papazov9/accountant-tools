package com.wildrep.accountantapp.service;

import com.wildrep.accountantapp.exceptions.ComparisonNotFoundException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileService {

    private final InvoiceRecordRepository invoiceRecordRepository;
    private final CSVRecordRepository csvRecordRepository;
    private final UserRepository userRepository;
    private final ComparisonRepository comparisonRepository;
    private final ComparisonResultRepository comparisonResultRepository;

    public ByteArrayOutputStream  processFiles(String username, List<MultipartFile> csvFiles, List<MultipartFile> txtFiles) throws IOException {
        String batchId = UUID.randomUUID().toString();
        List<ComparisonResult> results = new ArrayList<>();


        User user = this.userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserDoesNotExistException(username));

        Comparison comparison = Comparison.builder()
                .comparisonDate(LocalDateTime.now())
                .batchId(batchId)
                .user(user)
                .status("IN_PROGRESS")
                .build();
        this.comparisonRepository.saveAndFlush(comparison);

        for (int i = 0; i < csvFiles.size(); i++) {
            MultipartFile csvFile = csvFiles.get(i);
            MultipartFile txtFile = txtFiles.get(i);

            try {
                List<CSVInvoiceRecord> csvInvoiceRecords = FileServiceUtil.parseCsvFile(csvFile);
                List<InvoiceRecord> txtRecords = FileServiceUtil.parseTxtFile(txtFile);
                csvRecordRepository.saveAllAndFlush(csvInvoiceRecords);
                invoiceRecordRepository.saveAllAndFlush(txtRecords);
                List<ComparisonResult> partition = InvoiceComparisonUtil.compareInvoicesAndStoreResults(csvInvoiceRecords, txtRecords);
                results.addAll(partition);
            }catch (RuntimeException e) {
                comparison.setStatus("FAILED");
                throw new RuntimeException(e);
            }
        }
        this.comparisonResultRepository.saveAllAndFlush(results);
        comparison.setComparisonResults(results);
        this.comparisonRepository.saveAndFlush(comparison);
        comparison.setStatus("COMPLETED");
        comparisonRepository.save(comparison);

        return XlsxWriterUtil.generateComparisonReport(results);
    }


    public OldComparisonReportDTO loadResourceFromOldComparison(String batchId) {
        Comparison comparison = this.comparisonRepository.findByBatchId(batchId)
                .orElseThrow(() -> new ComparisonNotFoundException(batchId));

        ByteArrayOutputStream reportStream = XlsxWriterUtil.generateComparisonReport(comparison.getComparisonResults());

        return new OldComparisonReportDTO(reportStream, comparison.getComparisonDate());
    }
}
