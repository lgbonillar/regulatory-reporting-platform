package dev.lgbonillar.regreporting.processing.controller;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobService;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobFailureRequest;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobReasonRequest;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobStatusHistoryResponse;
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
    public ResponseEntity<List<ProcessingJobResponse>> listProcessingJobs(
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(processingJobService.listProcessingJobs(username));
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    public ResponseEntity<ProcessingJobResponse> getProcessingJob(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.getProcessingJob(jobId));
    }

    @PostMapping("/{jobId}/start")
    @PreAuthorize("hasRole('ANALYST')")
    public ResponseEntity<ProcessingJobResponse> startProcessing(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.startProcessing(jobId));
    }

    @PostMapping("/{jobId}/complete")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ProcessingJobResponse> completeProcessing(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.completeProcessing(jobId));
    }

    @PostMapping("/{jobId}/fail")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ProcessingJobResponse> failProcessing(
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobFailureRequest request
    ) {
        return ResponseEntity.ok(
                processingJobService.failProcessing(jobId, request.reason())
        );
    }

    @PostMapping("/{jobId}/approve")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ProcessingJobResponse> approve(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.approve(jobId));
    }

    @PostMapping("/{jobId}/reject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ProcessingJobResponse> reject(
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobReasonRequest request
    ) {
        return ResponseEntity.ok(
                processingJobService.reject(jobId, request.reason())
        );
    }

    @PostMapping("/{jobId}/revoke")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ProcessingJobResponse> revoke(
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobReasonRequest request
    ) {
        return ResponseEntity.ok(
                processingJobService.revoke(jobId, request.reason())
        );
    }

    @GetMapping("/{jobId}/history")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMINISTRATOR')")
    public ResponseEntity<List<ProcessingJobStatusHistoryResponse>> getProcessingJobHistory(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(
                processingJobService.getProcessingJobHistory(jobId)
        );
    }

}
