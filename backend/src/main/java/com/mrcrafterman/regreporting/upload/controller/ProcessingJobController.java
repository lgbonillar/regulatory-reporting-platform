package com.mrcrafterman.regreporting.upload.controller;

import com.mrcrafterman.regreporting.upload.application.ProcessingJobService;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobFailureRequest;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobReasonRequest;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobStatusHistoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ProcessingJobResponse>> listProcessingJobs(
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(processingJobService.listProcessingJobs(username));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ProcessingJobResponse> getProcessingJob(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.getProcessingJob(jobId));
    }

    @PostMapping("/{jobId}/start")
    public ResponseEntity<ProcessingJobResponse> startProcessing(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.startProcessing(jobId));
    }

    @PostMapping("/{jobId}/complete")
    public ResponseEntity<ProcessingJobResponse> completeProcessing(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.completeProcessing(jobId));
    }

    @PostMapping("/{jobId}/fail")
    public ResponseEntity<ProcessingJobResponse> failProcessing(
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobFailureRequest request
    ) {
        return ResponseEntity.ok(
                processingJobService.failProcessing(jobId, request.reason())
        );
    }

    @PostMapping("/{jobId}/approve")
    public ResponseEntity<ProcessingJobResponse> approve(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(processingJobService.approve(jobId));
    }

    @PostMapping("/{jobId}/reject")
    public ResponseEntity<ProcessingJobResponse> reject(
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobReasonRequest request
    ) {
        return ResponseEntity.ok(
                processingJobService.reject(jobId, request.reason())
        );
    }

    @PostMapping("/{jobId}/revoke")
    public ResponseEntity<ProcessingJobResponse> revoke(
            @PathVariable UUID jobId,
            @Valid @RequestBody ProcessingJobReasonRequest request
    ) {
        return ResponseEntity.ok(
                processingJobService.revoke(jobId, request.reason())
        );
    }

    @GetMapping("/{jobId}/history")
    public ResponseEntity<List<ProcessingJobStatusHistoryResponse>> getProcessingJobHistory(
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(
                processingJobService.getProcessingJobHistory(jobId)
        );
    }

}
