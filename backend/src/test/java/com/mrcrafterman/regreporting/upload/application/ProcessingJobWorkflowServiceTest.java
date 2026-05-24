package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.BusinessConflictException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ProcessingJobResponse;
import com.mrcrafterman.regreporting.users.application.CurrentUserProvider;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingJobWorkflowServiceTest {

    @Mock
    private ProcessingJobQueryService processingJobQueryService;

    @Mock
    private ProcessingJobHistoryService processingJobHistoryService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ProcessingJobWorkflowService processingJobWorkflowService;

    @Test
    void startProcessingMovesJobToProcessingAndRecordsUserTransition() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = pendingJob(UploadedFileStatus.STORED);
        User admin = administrator();

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(currentUserProvider.getCurrentAdministrator()).thenReturn(admin);
        when(processingJobQueryService.toProcessingJobResponse(job))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        ProcessingJobResponse result = processingJobWorkflowService.startProcessing(jobId);

        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.PROCESSING.name());
        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PROCESSING);
        assertThat(job.getTriggeredBy()).isSameAs(admin);

        verify(processingJobHistoryService).recordTransition(
                job,
                ProcessingJobStatus.PENDING_EXECUTION,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobTransitionSource.USER,
                admin,
                "Administrator started processing"
        );
    }

    @Test
    void startProcessingFailsWhenUploadedFileCannotBeProcessed() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = pendingJob(UploadedFileStatus.DELETED);

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);

        assertThatThrownBy(() -> processingJobWorkflowService.startProcessing(jobId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining(UploadedFileStatus.DELETED.name());

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION);
        verifyNoInteractions(currentUserProvider);
        verifyNoInteractions(processingJobHistoryService);
    }

    @Test
    void completeProcessingMovesJobToAwaitingApprovalAndRecordsSystemTransition() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = processingJob();

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(processingJobQueryService.toProcessingJobResponse(job))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        ProcessingJobResponse result = processingJobWorkflowService.completeProcessing(jobId);

        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL.name());
        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL);

        verify(processingJobHistoryService).recordTransition(
                job,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobStatus.AWAITING_APPROVAL,
                ProcessingJobTransitionSource.SYSTEM,
                null,
                "Automatic processing completed successfully"
        );
    }

    @Test
    void approveMovesJobToApprovedAndRecordsUserTransition() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = awaitingApprovalJob();
        User admin = administrator();

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(currentUserProvider.getCurrentAdministrator()).thenReturn(admin);
        when(processingJobQueryService.toProcessingJobResponse(job))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        ProcessingJobResponse result = processingJobWorkflowService.approve(jobId);

        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.APPROVED.name());
        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.APPROVED);
        assertThat(job.getApprovedBy()).isSameAs(admin);

        verify(processingJobHistoryService).recordTransition(
                job,
                ProcessingJobStatus.AWAITING_APPROVAL,
                ProcessingJobStatus.APPROVED,
                ProcessingJobTransitionSource.USER,
                admin,
                "Administrator approved submission"
        );
    }

    private ProcessingJob pendingJob(UploadedFileStatus fileStatus) {
        return new ProcessingJob(uploadedFile(fileStatus), "File uploaded");
    }

    private ProcessingJob processingJob() {
        ProcessingJob job = pendingJob(UploadedFileStatus.STORED);
        job.startProcessing(administrator());
        return job;
    }

    private ProcessingJob awaitingApprovalJob() {
        ProcessingJob job = processingJob();
        job.markProcessingCompleted();
        return job;
    }

    private UploadedFile uploadedFile(UploadedFileStatus status) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                analyst()
        );
    }

    private User analyst() {
        return new User("analyst01", "analyst01@example.com", "Analyst 01", null, false,
                UserStatus.ACTIVE);
    }

    private User administrator() {
        return new User("admin01", "admin01@example.com", "Admin 01", null, false,
                UserStatus.ACTIVE);
    }

    private ProcessingJobResponse response(ProcessingJob job) {
        UploadedFile file = job.getUploadedFile();

        return new ProcessingJobResponse(
                job.getId(),
                file.getId(),
                file.getOriginalFilename(),
                file.getStatus().name(),
                job.getStatus().name(),
                job.getMessage(),
                file.getUploadedBy().getUsername(),
                job.getTriggeredBy() == null ? null : job.getTriggeredBy().getUsername(),
                job.getTriggeredAt(),
                job.getProcessingCompletedAt(),
                job.getFailureReason(),
                job.getApprovedBy() == null ? null : job.getApprovedBy().getUsername(),
                job.getApprovedAt(),
                job.getRejectedBy() == null ? null : job.getRejectedBy().getUsername(),
                job.getRejectedAt(),
                job.getRejectionReason(),
                job.getRevokedBy() == null ? null : job.getRevokedBy().getUsername(),
                job.getRevokedAt(),
                job.getRevocationReason(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

}