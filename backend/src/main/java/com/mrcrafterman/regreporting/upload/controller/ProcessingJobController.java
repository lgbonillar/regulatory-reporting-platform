package com.mrcrafterman.regreporting.upload.controller;

import com.mrcrafterman.regreporting.upload.application.ProcessingJobService;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
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
}
