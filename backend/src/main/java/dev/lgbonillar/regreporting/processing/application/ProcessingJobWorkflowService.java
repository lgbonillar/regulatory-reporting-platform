package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobTransitionSource;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.processing.processor.ProcessingResult;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessor;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessorRegistry;
import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessingJobWorkflowService {

    private final ProcessingJobQueryService processingJobQueryService;
    private final ProcessingJobHistoryService processingJobHistoryService;
    private final CurrentUserProvider currentUserProvider;
    private final ProcessingJobFindingService processingJobFindingService;
    private final RegulatoryReportProcessorRegistry regulatoryReportProcessorRegistry;

    public ProcessingJobWorkflowService(
            ProcessingJobQueryService processingJobQueryService,
            ProcessingJobHistoryService processingJobHistoryService,
            CurrentUserProvider currentUserProvider,
            ProcessingJobFindingService processingJobFindingService,
            RegulatoryReportProcessorRegistry regulatoryReportProcessorRegistry
    ) {
        this.processingJobQueryService = processingJobQueryService;
        this.processingJobHistoryService = processingJobHistoryService;
        this.currentUserProvider = currentUserProvider;
        this.processingJobFindingService = processingJobFindingService;
        this.regulatoryReportProcessorRegistry = regulatoryReportProcessorRegistry;
    }

    @Transactional
    public ProcessingJobResponse startProcessing(UUID jobId) {
        ProcessingJob job = processingJobQueryService.getJob(jobId);
        job.getUploadedFile().ensureCanBeProcessed();

        User currentUser = currentUserProvider.getCurrentUser();
        requireRole(currentUser, UserRole.ANALYST, "start processing");
        requireOwnsJob(currentUser, job);

        ProcessingJobStatus previousStatus = job.getStatus();

        job.startProcessing(currentUser);

        processingJobHistoryService.recordTransition(
                job,
                previousStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.USER,
                currentUser,
                "Analyst started processing"
        );

        RegulatoryReportProcessor processor = regulatoryReportProcessorRegistry.getDefaultProcessor();
        ProcessingResult result = processor.process(job);

        processingJobFindingService.replaceProcessingJobFindings(
                job,
                result.findings()
        );

        ProcessingJobStatus processingStatus = job.getStatus();

        if (result.hasErrors()) {
            job.markProcessingFailed(result.message());
        } else {
            job.markProcessingCompleted();
        }

        processingJobHistoryService.recordTransition(
                job,
                processingStatus,
                job.getStatus(),
                ProcessingJobTransitionSource.SYSTEM,
                null,
                result.message()
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

        User administrator = currentUserProvider.getCurrentUser();
        requireRole(administrator, UserRole.ADMINISTRATOR, "approve processing jobs");

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
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = currentUserProvider.getCurrentUser();
        requireRole(administrator, UserRole.ADMINISTRATOR, "reject processing jobs");

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
        job.getUploadedFile().ensureCanBeProcessed();

        User administrator = currentUserProvider.getCurrentUser();
        requireRole(administrator, UserRole.ADMINISTRATOR, "revoke processing jobs");

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

    private void requireRole(User user, UserRole role, String action) {
        if (!user.hasRole(role)) {
            throw new ForbiddenOperationException(
                    "You are not allowed to " + action
            );
        }
    }

    private void requireOwnsJob(User user, ProcessingJob job) {
        String uploadedBy = job.getUploadedFile()
                .getUploadedBy()
                .getUsername();

        if (!uploadedBy.equals(user.getUsername())) {
            throw new ForbiddenOperationException(
                    "You can only start processing jobs uploaded by your user"
            );
        }
    }

}
