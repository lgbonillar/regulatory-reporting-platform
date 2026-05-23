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
public class ProcessingJobService {
    private final ProcessingJobRepository processingJobRepository;

    public ProcessingJobService(ProcessingJobRepository processingJobRepository) {
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
        ProcessingJob processingJob = processingJobRepository.findByIdWithUploadedFile(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Processing job not found"));

        return toProcessingJobResponse(processingJob);
    }

    private ProcessingJobResponse toProcessingJobResponse(ProcessingJob processingJob) {
        UploadedFile uploadedFile = processingJob.getUploadedFile();

        return new ProcessingJobResponse(
                processingJob.getId(),
                uploadedFile.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStatus().name(),
                processingJob.getStatus().name(),
                processingJob.getMessage(),
                uploadedFile.getUploadedBy().getUsername(),
                processingJob.getCreatedAt(),
                processingJob.getUpdatedAt()
        );
    }
}
