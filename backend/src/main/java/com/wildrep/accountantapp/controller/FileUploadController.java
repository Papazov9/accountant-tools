package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.model.dto.HistoryRecordResponse;
import com.wildrep.accountantapp.model.dto.OldComparisonReportDTO;
import com.wildrep.accountantapp.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("csvFiles") List<MultipartFile> csvFiles,
                                         @RequestParam("txtFiles") List<MultipartFile> txtFiles,
                                         @RequestParam("username") String username) {
        if (csvFiles.size() > 12 || txtFiles.size() > 12) {
            return ResponseEntity.badRequest().body("You can upload up to 12 files per format.");
        }
        if (csvFiles.isEmpty() || txtFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("Both CSV and TXT files are required.");
        }

        if (csvFiles.size() == txtFiles.size()) {
            try {
                ByteArrayOutputStream reportStream = fileService.processFiles(username, csvFiles, txtFiles);


                String timestamp = new SimpleDateFormat("dd-MM-yyyy_HH\\:mm\\:ss").format(new Date());
                String filename = "comparison_report_" + timestamp + ".xlsx";

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(reportStream.size())
                        .body(reportStream.toByteArray());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing files: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            }
        }
        return ResponseEntity.badRequest().body("Both CSV and TXT files should be equal in number.");
    }

    @GetMapping("/download/{batchId}")
    public ResponseEntity<?> downloadComparisonReport(@PathVariable String batchId) {
        OldComparisonReportDTO reportDTO = fileService.loadResourceFromOldComparison(batchId);
        ByteArrayOutputStream resource = reportDTO.reportStream();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedDateTime = reportDTO.comparisonTimestamp().format(formatter);
        String fileName = "ComparisonReport_" + formattedDateTime + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.size())
                .body(resource.toByteArray());
    }

    @GetMapping("/history/{username}")
    public ResponseEntity<List<HistoryRecordResponse>> loadHistory(@PathVariable String username) {
        try {
            List<HistoryRecordResponse> historyRecordResponses = this.fileService.loadHistory(username);
            return ResponseEntity.ok(historyRecordResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of(new HistoryRecordResponse(null, null, null, "Failed to load history!")));
        }

    }
}