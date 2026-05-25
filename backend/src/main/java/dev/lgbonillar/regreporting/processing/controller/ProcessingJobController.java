package dev.lgbonillar.regreporting.processing.controller;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobService;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFailureRequest;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobReasonRequest;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
import dev.lgbonillar.regreporting.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/processing-jobs")
public class ProcessingJobController {

    private final ProcessingJobService processingJobService;

    public ProcessingJobController(ProcessingJobService processingJobService) {
        this.processingJobService = processingJobService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<ProcessingJobResponse>>> listProcessingJobs(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> getProcessingJob(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> startProcessing(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> completeProcessing(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> failProcessing(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> approve(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> reject(
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
    public ResponseEntity<ApiResponse<ProcessingJobResponse>> revoke(
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
    public ResponseEntity<ApiResponse<List<ProcessingJobStatusHistoryResponse>>> getProcessingJobHistory(
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
