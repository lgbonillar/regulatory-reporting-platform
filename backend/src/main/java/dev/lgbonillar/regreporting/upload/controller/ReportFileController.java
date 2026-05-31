package dev.lgbonillar.regreporting.upload.controller;

import dev.lgbonillar.regreporting.shared.ApiResponse;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.application.DeleteFileService;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.application.MarkFileMissingService;
import dev.lgbonillar.regreporting.upload.application.UpdateFileService;
import dev.lgbonillar.regreporting.upload.application.UploadFileService;
import dev.lgbonillar.regreporting.upload.application.UploadedFileFindingService;
import dev.lgbonillar.regreporting.upload.application.UploadedFileQueryService;
import dev.lgbonillar.regreporting.upload.application.UploadedFileValidationRunService;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
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

    private final UploadFileService uploadFileService;
    private final UpdateFileService updateFileService;
    private final DeleteFileService deleteFileService;
    private final UploadedFileQueryService uploadedFileQueryService;
    private final UploadedFileValidationRunService validationRunService;
    private final UploadedFileFindingService findingService;
    private final MarkFileMissingService markFileMissingService;
    private final FileStorageService fileStorageService;

    public ReportFileController(
            UploadFileService uploadFileService,
            UpdateFileService updateFileService,
            DeleteFileService deleteFileService,
            UploadedFileQueryService uploadedFileQueryService,
            UploadedFileValidationRunService validationRunService,
            UploadedFileFindingService findingService,
            MarkFileMissingService markFileMissingService,
            FileStorageService fileStorageService
    ) {
        this.uploadFileService = uploadFileService;
        this.updateFileService = updateFileService;
        this.deleteFileService = deleteFileService;
        this.uploadedFileQueryService = uploadedFileQueryService;
        this.validationRunService = validationRunService;
        this.findingService = findingService;
        this.markFileMissingService = markFileMissingService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR', 'ROOT')")
    @Operation(
            summary = "List report files",
            description = """
                    Returns uploaded report files for a username. Analysts can only access
                    own files and allowed by application rules; administrators can inspect all analysts files.
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
        List<UploadedFileResponse> response = uploadedFileQueryService.listUploadedFiles(username);

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
        ReportFileUploadResponse response = updateFileService.updateFile(fileId, file);

        return ResponseEntity.ok(ApiResponse.success("Report file updated successfully", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ANALYST')")
    @Operation(
            summary = "Upload report file",
            description = """
                    Stores and validates a regulatory report file.
                    If validation succeeds, creates a processing job in PENDING_EXECUTION state; otherwise, changes the file status to PENDING_CORRECTION.
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
        ReportFileUploadResponse response = uploadFileService.uploadFile(file);

        return ResponseEntity.ok(ApiResponse.success("Report file uploaded successfully", response));
    }

    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR', 'ROOT')")
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
        UploadedFile uploadedFile = uploadedFileQueryService.getDownloadableUploadedFile(fileId);

        Resource resource;

        try {
            resource = fileStorageService.loadAsResource(uploadedFile.getStoragePath());
        } catch (ResourceNotFoundException exception) {
            markFileMissingService.markFileAsMissing(fileId);
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

    @GetMapping("/{fileId}/validation-runs")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR', 'ROOT')")
    @Operation(
            summary = "List uploaded file validation runs",
            description = """
                    Returns validation executions for an uploaded file. Analysts can only
                    inspect their own files; administrators and root can inspect all files.
                    Auditors do not have access to uploaded file details.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Validation runs retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UploadedFileValidationRunResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to view uploaded file validation data"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Uploaded file not found"
                    )
            }
    )
    public ResponseEntity<ApiResponse<List<UploadedFileValidationRunResponse>>> listValidationRuns(
            @Parameter(description = "Uploaded file identifier")
            @PathVariable UUID fileId
    ) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        List<UploadedFileValidationRunResponse> response =
                validationRunService.listValidationRuns(uploadedFile.getId())
                        .stream()
                        .map(this::toValidationRunResponse)
                        .toList();

        return ResponseEntity.ok(ApiResponse.successList(
                "Validation runs retrieved successfully",
                response
        ));
    }

    @GetMapping("/{fileId}/findings")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR', 'ROOT')")
    @Operation(
            summary = "List uploaded file validation findings",
            description = """
                    Returns all validation findings stored for an uploaded file across
                    validation runs.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Validation findings retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UploadedFileFindingResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to view uploaded file validation data"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Uploaded file not found"
                    )
            }
    )
    public ResponseEntity<ApiResponse<List<UploadedFileFindingResponse>>> listFindings(
            @Parameter(description = "Uploaded file identifier")
            @PathVariable UUID fileId
    ) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        List<UploadedFileFindingResponse> response =
                findingService.listFindings(uploadedFile.getId())
                        .stream()
                        .map(this::toFindingResponse)
                        .toList();

        return ResponseEntity.ok(ApiResponse.successList(
                "Validation findings retrieved successfully",
                response
        ));
    }

    @GetMapping("/{fileId}/validation-runs/{validationRunId}/findings")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR', 'ROOT')")
    @Operation(
            summary = "List findings for one uploaded file validation run",
            description = """
                    Returns validation findings for a specific validation run associated
                    with the uploaded file.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Validation run findings retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UploadedFileFindingResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to view uploaded file validation data"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Uploaded file or validation run not found"
                    )
            }
    )
    public ResponseEntity<ApiResponse<List<UploadedFileFindingResponse>>> listValidationRunFindings(
            @Parameter(description = "Uploaded file identifier")
            @PathVariable UUID fileId,
            @Parameter(description = "Validation run identifier")
            @PathVariable UUID validationRunId
    ) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);
        UploadedFileValidationRun validationRun = validationRunService.getValidationRun(
                uploadedFile.getId(),
                validationRunId
        );

        List<UploadedFileFindingResponse> response =
                findingService.listFindingsByValidationRun(validationRun.getId())
                        .stream()
                        .map(this::toFindingResponse)
                        .toList();

        return ResponseEntity.ok(ApiResponse.successList(
                "Validation run findings retrieved successfully",
                response
        ));
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
        deleteFileService.deleteFile(fileId);

        return ResponseEntity.noContent().build();
    }

    private UploadedFileValidationRunResponse toValidationRunResponse(
            UploadedFileValidationRun validationRun
    ) {
        return new UploadedFileValidationRunResponse(
                validationRun.getId(),
                validationRun.getUploadedFile().getId(),
                validationRun.getStatus().name(),
                validationRun.getSource().name(),
                validationRun.getSummaryMessage(),
                validationRun.getCreatedBy(),
                validationRun.getCreatedAt()
        );
    }

    private UploadedFileFindingResponse toFindingResponse(UploadedFileFinding finding) {
        return new UploadedFileFindingResponse(
                finding.getId(),
                finding.getValidationRun().getId(),
                finding.getUploadedFile().getId(),
                finding.getSeverity().name(),
                finding.getScope().name(),
                finding.getCode(),
                finding.getMessage(),
                finding.getSheetName(),
                finding.getRowNumber(),
                finding.getColumnName(),
                finding.getFieldName(),
                finding.getRejectedValue(),
                finding.getExpectedValue(),
                finding.getActualValue(),
                finding.getCreatedAt()
        );
    }

}