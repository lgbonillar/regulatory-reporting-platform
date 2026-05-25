package dev.lgbonillar.regreporting.upload.controller;

import dev.lgbonillar.regreporting.shared.ApiResponse;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.application.ReportFileService;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<UploadedFileResponse>>> listReportFiles(
            @RequestParam String username
    ) {
        List<UploadedFileResponse> response = reportFileService.listUploadedFiles(username);

        return ResponseEntity.ok(ApiResponse.successList(
                "Report files retrieved successfully",
                response
        ));
    }

    @PutMapping(path = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<ReportFileUploadResponse>> updateReportFile(
            @PathVariable UUID fileId,
            @RequestParam("file") MultipartFile file
    ) {
        ReportFileUploadResponse response = reportFileService.updateReportFile(fileId, file);

        return ResponseEntity.ok(ApiResponse.success("Report file updated successfully", response));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<ApiResponse<ReportFileUploadResponse>> uploadReportFile(
            @RequestParam("file") MultipartFile file
    ) {
        ReportFileUploadResponse response = reportFileService.uploadReportFile(file);

        return ResponseEntity.ok(ApiResponse.success("Report file uploaded successfully", response));
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
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
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<Void> deleteReportFile(@PathVariable UUID fileId) {
        reportFileService.deleteUploadedFile(fileId);

        return ResponseEntity.noContent().build();
    }

}
