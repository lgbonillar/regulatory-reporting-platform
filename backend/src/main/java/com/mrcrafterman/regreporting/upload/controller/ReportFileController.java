package com.mrcrafterman.regreporting.upload.controller;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.application.FileStorageService;
import com.mrcrafterman.regreporting.upload.application.ReportFileService;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/report-files")
public class ReportFileController {

    private final ReportFileService reportFileService;
    private final FileStorageService fileStorageService;

    public ReportFileController(
            ReportFileService reportFileService,
            FileStorageService fileStorageService
    ) {
        this.reportFileService = reportFileService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<UploadedFileResponse>> listReportFiles(
            @RequestParam String username
    ) {
        return ResponseEntity.ok(reportFileService.listUploadedFiles(username));
    }

    @PutMapping(path = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportFileUploadResponse> updateReportFile(
            @PathVariable UUID fileId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(reportFileService.updateReportFile(fileId, file));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportFileUploadResponse> uploadReportFile(
            @RequestParam("file") MultipartFile file
    ) {
        ReportFileUploadResponse response = reportFileService.uploadReportFile(file);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadReportFile(@PathVariable UUID fileId) {
        UploadedFile uploadedFile = reportFileService.getStoredUploadedFile(fileId);

        Resource resource;

        try {
            resource = fileStorageService.loadAsResource(uploadedFile.getStoragePath());
        } catch (ResourceNotFoundException exception) {
            reportFileService.markUploadedFileAsMissing(fileId);
            throw exception;
        }

        MediaType contentType = uploadedFile.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(uploadedFile.getContentType());

        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(uploadedFile.getFileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(uploadedFile.getOriginalFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteReportFile(@PathVariable UUID fileId) {
        reportFileService.deleteUploadedFile(fileId);

        return ResponseEntity.noContent().build();
    }

}
