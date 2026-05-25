package dev.lgbonillar.regreporting.processing.controller;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobService;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFailureRequest;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobReasonRequest;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import dev.lgbonillar.regreporting.shared.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processing-jobs")
@Tag(
        name = "Processing Jobs",
        description = "Processing workflow operations for uploaded regulatory report files."
)
public class ProcessingJobController {

    private final ProcessingJobService processingJobService;

    public ProcessingJobController(ProcessingJobService processingJobService) {
        this.processingJobService = processingJobService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    @Operation(
            summary = "List processing jobs",
            description = """
                    Returns processing jobs visible to the current user. Analysts only receive
                    their own jobs. Administrators can list every job or filter by username.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing jobs retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid JWT"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to list processing jobs"
                    )
            }
    )
    public ResponseEntity<ApiResponse<List<ProcessingJobResponse>>> listProcessingJobs(
            @Parameter(description = "Optional username filter. Only administrators can use it to view another user's jobs.")
            @RequestParam(required = false) String username
    ) {
        List<ProcessingJobResponse> response = processingJobService.listProcessingJobs(username);

        return ResponseEntity.ok(ApiResponse.successList(
                "Processing jobs retrieved successfully",
                response
        ));
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    @Operation(
            summary = "Get processing job details",
            description = "Returns workflow, file and approval details for a processing job.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to view this processing job"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Processing job not found"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> getProcessingJob(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId
    ) {
        ProcessingJobResponse response = processingJobService.getProcessingJob(jobId);

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job retrieved successfully",
                response
        ));
    }

    @PostMapping("/{jobId}/start")
    @PreAuthorize("hasRole('ANALYST')")
    @Operation(
            summary = "Start processing job",
            description = """
                    Starts processing for a pending job. Analysts can only start jobs created
                    from their own uploaded files.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job started successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to start this job"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Job or file cannot be processed in its current state"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> startProcessing(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId
    ) {
        ProcessingJobResponse response = processingJobService.startProcessing(jobId);

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job started successfully",
                response
        ));
    }

    @PostMapping("/{jobId}/complete")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Complete processing job",
            description = """
                    Marks a processing job as completed and moves it to awaiting approval.
                    This endpoint represents the system completing technical processing.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job completed successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to complete processing jobs"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Job cannot be completed in its current state"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> completeProcessing(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId
    ) {
        ProcessingJobResponse response = processingJobService.completeProcessing(jobId);

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job completed successfully",
                response
        ));
    }

    @PostMapping("/{jobId}/fail")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Fail processing job",
            description = """
                    Marks a processing job as failed with a technical or validation reason.
                    Future validation findings should be exposed through a dedicated findings endpoint.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job marked as failed",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid failure reason"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to fail processing jobs"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> failProcessing(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobFailureRequest request
    ) {
        ProcessingJobResponse response = processingJobService.failProcessing(
                jobId,
                request.reason()
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job failed successfully",
                response
        ));
    }

    @PostMapping("/{jobId}/approve")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Approve processing result",
            description = "Approves a job that is awaiting administrative approval.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job approved successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to approve processing jobs"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Job cannot be approved in its current state"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> approve(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId
    ) {
        ProcessingJobResponse response = processingJobService.approve(jobId);

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job approved successfully",
                response
        ));
    }

    @PostMapping("/{jobId}/reject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Reject processing result",
            description = "Rejects a job that is awaiting administrative approval and stores the rejection reason.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job rejected successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid rejection reason"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Job cannot be rejected in its current state"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> reject(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobReasonRequest request
    ) {
        ProcessingJobResponse response = processingJobService.reject(
                jobId,
                request.reason()
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job rejected successfully",
                response
        ));
    }

    @PostMapping("/{jobId}/revoke")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Revoke previous approval",
            description = "Invalidates a previously approved processing result and stores the revocation reason.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job revoked successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid revocation reason"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "Job cannot be revoked in its current state"
                    )
            }
    )
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> revoke(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobReasonRequest request
    ) {
        ProcessingJobResponse response = processingJobService.revoke(
                jobId,
                request.reason()
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Processing job revoked successfully",
                response
        ));
    }

    @GetMapping("/{jobId}/history")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    @Operation(
            summary = "Get processing job status history",
            description = "Returns the audit trail of workflow status transitions for a processing job.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Processing job history retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ProcessingJobStatusHistoryResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to view this processing job history"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Processing job not found"
                    )
            }
    )
    public ResponseEntity<ApiResponse<List<ProcessingJobStatusHistoryResponse>>> getProcessingJobHistory(
            @Parameter(description = "Processing job identifier")
            @PathVariable UUID jobId
    ) {
        List<ProcessingJobStatusHistoryResponse> response =
                processingJobService.getProcessingJobHistory(jobId);

        return ResponseEntity.ok(ApiResponse.successList(
                "Processing job history retrieved successfully",
                response
        ));
    }

}
