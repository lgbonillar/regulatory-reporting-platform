package dev.lgbonillar.regreporting.upload.controller;

import dev.lgbonillar.regreporting.shared.ApiResponse;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.application.ReportFileService;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Report Files",
        description = "Upload, list, update, download and delete regulatory report files."
)
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
    @Operation(
            summary = "List report files",
            description = """
                    Returns uploaded report files for a username. Analysts can only access
                    files allowed by application rules; administrators can inspect files
                    across analysts.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Report files retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UploadedFileResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to list report files"
                    )
            }
    )
    public ResponseEntity<ApiResponse<List<UploadedFileResponse>>> listReportFiles(
            @Parameter(description = "Username that owns the uploaded files", example = "analyst01")
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
    @Operation(
            summary = "Replace report file contents",
            description = """
                    Replaces an existing report file with a new uploaded file and creates
                    a new pending processing job for the updated content.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Report file updated successfully",
                            content = @Content(schema = @Schema(implementation = ReportFileUploadResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid multipart request"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Report file not found"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ReportFileUploadResponse>> updateReportFile(
            @Parameter(description = "Uploaded file identifier")
            @PathVariable UUID fileId,
            @Parameter(description = "Excel file to store and process")
            @RequestParam("file") MultipartFile file
    ) {
        ReportFileUploadResponse response = reportFileService.updateReportFile(fileId, file);

        return ResponseEntity.ok(ApiResponse.success("Report file updated successfully", response));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ANALYST')")
    @Operation(
            summary = "Upload report file",
            description = """
                    Stores a regulatory report file and creates a processing job in
                    PENDING_EXECUTION state.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Report file uploaded successfully",
                            content = @Content(schema = @Schema(implementation = ReportFileUploadResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid multipart request"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to upload report files"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ReportFileUploadResponse>> uploadReportFile(
            @Parameter(description = "Excel file to store and process")
            @RequestParam("file") MultipartFile file
    ) {
        ReportFileUploadResponse response = reportFileService.uploadReportFile(file);

        return ResponseEntity.ok(ApiResponse.success("Report file uploaded successfully", response));
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    @Operation(
            summary = "Download report file",
            description = """
                    Downloads the stored binary file. This endpoint intentionally does not use
                    the standard JSON response envelope because it returns file content.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Report file downloaded successfully",
                            content = @Content(mediaType = "application/octet-stream")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Report file or storage resource not found"
                    )
            }
    )
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
    @Operation(
            summary = "Delete report file",
            description = """
                    Marks an uploaded file as deleted. Deleted files are not available for
                    download or further processing.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "204",
                            description = "Report file deleted successfully"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Report file not found"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Report file cannot be deleted in its current state"
                    )
            }
    )
    public ResponseEntity<Void> deleteReportFile(@PathVariable UUID fileId) {
        reportFileService.deleteUploadedFile(fileId);

        return ResponseEntity.noContent().build();
    }

}
