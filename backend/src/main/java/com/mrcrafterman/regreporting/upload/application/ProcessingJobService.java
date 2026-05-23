package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatusHistory;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
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
        return processingJobRepository.findById(jobId)
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
