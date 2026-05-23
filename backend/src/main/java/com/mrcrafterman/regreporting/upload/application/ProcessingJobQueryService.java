package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobQueryService {

    private final ProcessingJobRepository processingJobRepository;

    public ProcessingJobQueryService(ProcessingJobRepository processingJobRepository) {
        this.processingJobRepository = processingJobRepository;
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobResponse> listProcessingJobs(String username) {
        List<ProcessingJob> processingJobs = username == null || username.isBlank()
                ? processingJobRepository.findAllWithUploadedFile()
                : processingJobRepository.findAllByUsername(username);

        return processingJobs.stream()
                .map(this::toProcessingJobResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcessingJobResponse getProcessingJob(UUID jobId) {
        ProcessingJob processingJob = getJob(jobId);

        return toProcessingJobResponse(processingJob);
    }

    public ProcessingJob getJob(UUID jobId) {
        return processingJobRepository.findByIdWithUploadedFile(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processing job not found"
                ));
    }

    public ProcessingJobResponse toProcessingJobResponse(ProcessingJob processingJob) {
        UploadedFile uploadedFile = processingJob.getUploadedFile();

        return new ProcessingJobResponse(
                processingJob.getId(),
                uploadedFile.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStatus().name(),
                processingJob.getStatus().name(),
                processingJob.getMessage(),
                uploadedFile.getUploadedBy().getUsername(),
                processingJob.getTriggeredBy() == null
                        ? null
                        : processingJob.getTriggeredBy().getUsername(),
                processingJob.getTriggeredAt(),
                processingJob.getProcessingCompletedAt(),
                processingJob.getFailureReason(),
                processingJob.getApprovedBy() == null
                        ? null
                        : processingJob.getApprovedBy().getUsername(),
                processingJob.getApprovedAt(),
                processingJob.getRejectedBy() == null
                        ? null
                        : processingJob.getRejectedBy().getUsername(),
                processingJob.getRejectedAt(),
                processingJob.getRejectionReason(),
                processingJob.getRevokedBy() == null
                        ? null
                        : processingJob.getRevokedBy().getUsername(),
                processingJob.getRevokedAt(),
                processingJob.getRevocationReason(),
                processingJob.getCreatedAt(),
                processingJob.getUpdatedAt()
        );
    }

}
