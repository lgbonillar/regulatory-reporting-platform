package com.mrcrafterman.regreporting.processing.application;

import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.processing.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.processing.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.shared.ForbiddenOperationException;
import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.users.application.CurrentUserProvider;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobQueryService {

    private final ProcessingJobRepository processingJobRepository;
    private final CurrentUserProvider currentUserProvider;

    public ProcessingJobQueryService(
            ProcessingJobRepository processingJobRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.processingJobRepository = processingJobRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobResponse> listProcessingJobs(String username) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (currentUser.hasRole(UserRole.ANALYST)) {
            return processingJobRepository.findAllByUsername(currentUser.getUsername())
                    .stream()
                    .map(this::toProcessingJobResponse)
                    .toList();
        }

        requireRole(currentUser, UserRole.ADMINISTRATOR, "list processing jobs");

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
        requireCanView(processingJob);

        return toProcessingJobResponse(processingJob);
    }

    public ProcessingJob getJob(UUID jobId) {
        return processingJobRepository.findByIdWithUploadedFile(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processing job not found"
                ));
    }

    public void requireCanView(ProcessingJob processingJob) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (currentUser.hasRole(UserRole.ADMINISTRATOR)) {
            return;
        }

        if (currentUser.hasRole(UserRole.ANALYST) &&
                processingJob.getUploadedFile()
                        .getUploadedBy()
                        .getUsername()
                        .equals(currentUser.getUsername())) {
            return;
        }

        throw new ForbiddenOperationException(
                "You are not allowed to view this processing job"
        );
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

    private void requireRole(User user, UserRole role, String action) {
        if (!user.hasRole(role)) {
            throw new ForbiddenOperationException(
                    "You are not allowed to " + action
            );
        }
    }

}
