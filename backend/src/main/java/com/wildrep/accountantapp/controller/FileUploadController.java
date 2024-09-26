package com.wildrep.accountantapp.controller;

import com.wildrep.accountantapp.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("csvFiles") List<MultipartFile> csvFiles,
                                         @RequestParam("txtFiles") List<MultipartFile> txtFiles) {

        // Validations
        if (csvFiles.size() > 12 || txtFiles.size() > 12) {
            return ResponseEntity.badRequest().body("You can upload up to 12 files per format.");
        }
        if (csvFiles.isEmpty() || txtFiles.isEmpty()) {
            return ResponseEntity.badRequest().body("Both CSV and TXT files are required.");
        }

        if (csvFiles.size() == txtFiles.size()) {
            try {

                String resultPath = this.fileService.processFiles(csvFiles, txtFiles);
                File file = new File(resultPath);

                if (file.exists()) {
                    FileSystemResource resource = new FileSystemResource(file);

                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentLength(file.length())
                            .body(resource);
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comparison result file not found.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing files: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            }
        }
        return ResponseEntity.badRequest().body("Both CSV and TXT files should be equal in number.");
    }
}