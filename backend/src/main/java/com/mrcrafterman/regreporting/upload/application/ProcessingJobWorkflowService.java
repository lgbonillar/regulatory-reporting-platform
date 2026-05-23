package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessingJobWorkflowService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobHistoryService processingJobHistoryService;
    private final UserRepository userRepository;

    public ProcessingJobWorkflowService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobHistoryService processingJobHistoryService,
            UserRepository userRepository
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobHistoryService = processingJobHistoryService;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProcessingJobResponse startProcessing(UUID jobId) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.startProcessing(administrator);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                "Administrator started processing"
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse completeProcessing(UUID jobId) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        ProcessingJobStatus previousStatus = job.getStatus();

        job.markProcessingCompleted();

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                "Automatic processing completed successfully"
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse failProcessing(UUID jobId, String reason) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        ProcessingJobStatus previousStatus = job.getStatus();

        job.markProcessingFailed(reason);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                reason.trim()
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse approve(UUID jobId) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.approve(administrator);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                "Administrator approved submission"
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse reject(UUID jobId, String reason) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.reject(administrator, reason);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                reason.trim()
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    @Transactional
    public ProcessingJobResponse revoke(UUID jobId, String reason) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        User administrator = getCurrentAdministrator();
        ProcessingJobStatus previousStatus = job.getStatus();

        job.revoke(administrator, reason);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                administrator,
                reason.trim()
        );

        return processingJobQueryService.toProcessingJobResponse(job);
    }

    private User getCurrentAdministrator() {
        return userRepository.findByUsername("admin01")
                .orElseThrow(() -> new IllegalStateException(
                        "Current development administrator not found"
                ));
    }

}
