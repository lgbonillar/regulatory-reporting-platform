package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatusHistory;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobStatusHistoryResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobStatusHistoryRepository;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProcessingJobService {

    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository;
    private final UserRepository userRepository;

    public ProcessingJobService(
            ProcessingJobRepository processingJobRepository,
            ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository,
            UserRepository userRepository
    ) {
        this.processingJobRepository = processingJobRepository;
        this.processingJobStatusHistoryRepository = processingJobStatusHistoryRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public ProcessingJobResponse startProcessing(UUID jobId) {
        ProcessingJob job = getJob(jobId);
        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.startProcessing(administrator);

        recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                "Administrator started processing"
        );

        return toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse completeProcessing(UUID jobId) {
        ProcessingJob job = getJob(jobId);
        ProcessingJobStatus previousStatus = job.getStatus();

        job.markProcessingCompleted();

        recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                "Automatic processing completed successfully"
        );

        return toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse failProcessing(UUID jobId, String reason) {
        ProcessingJob job = getJob(jobId);
        ProcessingJobStatus previousStatus = job.getStatus();

        job.markProcessingFailed(reason);

        recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                reason.trim()
        );

        return toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse approve(UUID jobId) {
        ProcessingJob job = getJob(jobId);
        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.approve(administrator);

        recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                "Administrator approved submission"
        );

        return toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse reject(UUID jobId, String reason) {
        ProcessingJob job = getJob(jobId);
        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.reject(administrator, reason);

        recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                reason.trim()
        );

        return toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse revoke(UUID jobId, String reason) {
        ProcessingJob job = getJob(jobId);
        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.revoke(administrator, reason);

        recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                reason.trim()
        );

        return toProcessingJobResponse(job);
    }

    @Transactional(readOnly = true)
    public List<ProcessingJobStatusHistoryResponse> getProcessingJobHistory(UUID jobId) {
        if (!processingJobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Processing job not found");
        }

        return processingJobStatusHistoryRepository
                .findByProcessingJobIdOrderByCreatedAtAsc(jobId)
                .stream()
                .map(this::toProcessingJobStatusHistoryResponse)
                .toList();
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

    private ProcessingJobStatusHistoryResponse toProcessingJobStatusHistoryResponse(
            ProcessingJobStatusHistory history
    ) {
        return new ProcessingJobStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus() == null
                        ? null
                        : history.getPreviousStatus().name(),
                history.getNewStatus().name(),
                history.getTransitionSource().name(),
                history.getTransitionedBy() == null
                        ? null
                        : history.getTransitionedBy().getUsername(),
                history.getReason(),
                history.getCreatedAt()
        );
    }

    private void recordTransition(
            ProcessingJob job,
            ProcessingJobStatus previousStatus,
            ProcessingJobStatus newStatus,
            ProcessingJobTransitionSource source,
            User transitionedBy,
            String reason
    ) {
        processingJobStatusHistoryRepository.save(
                new ProcessingJobStatusHistory(
                        job,
                        previousStatus,
                        newStatus,
                        source,
                        transitionedBy,
                        reason
                )
        );
    }

    private ProcessingJob getJob(UUID jobId) {
        return processingJobRepository.findByIdWithUploadedFile(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processing job not found"
                ));
    }

    private User getCurrentAdministrator() {
        return userRepository.findByUsername("admin01")
                .orElseThrow(() -> new IllegalStateException(
                        "Current development administrator not found"
                ));
    }

}
