package dev.lgbonillar.regreporting.processing.application;

import dev.lgbonillar.regreporting.shared.BusinessConflictException;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobStatus;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJobTransitionSource;
import dev.lgbonillar.regreporting.processing.processor.ProcessingResult;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessor;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessorRegistry;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.processing.dto.ProcessingJobResponse;
import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.Role;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @Mock
    private ProcessingJobFindingService processingJobFindingService;

    @Mock
    private RegulatoryReportProcessorRegistry regulatoryReportProcessorRegistry;

    @Mock
    private RegulatoryReportProcessor regulatoryReportProcessor;

    @Test
    void startProcessingRunsProcessorAndMovesJobToAwaitingApproval() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = pendingJob(UploadedFileStatus.STORED);
        User analyst = analyst();

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(currentUserProvider.getCurrentUser()).thenReturn(analyst);
        when(regulatoryReportProcessorRegistry.getDefaultProcessor()).thenReturn(regulatoryReportProcessor);
        when(regulatoryReportProcessor.process(job)).thenReturn(
                ProcessingResult.successful(
                        "DEMO_REGULATORY_REPORT",
                        "Demo regulatory report processed successfully"
                )
        );
        when(processingJobQueryService.toProcessingJobResponse(job))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        ProcessingJobResponse result = processingJobWorkflowService.startProcessing(jobId);

        assertThat(result.jobStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL.name());
        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL);
        assertThat(job.getTriggeredBy()).isSameAs(analyst);

        verify(processingJobHistoryService).recordTransition(
                job,
                ProcessingJobStatus.PENDING_EXECUTION,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobTransitionSource.USER,
                analyst,
                "Analyst started processing"
        );

        verify(regulatoryReportProcessorRegistry).getDefaultProcessor();
        verify(regulatoryReportProcessor).process(job);
        verify(processingJobFindingService).replaceProcessingJobFindings(job, List.of());

        verify(processingJobHistoryService).recordTransition(
                job,
                ProcessingJobStatus.PROCESSING,
                ProcessingJobStatus.AWAITING_APPROVAL,
                ProcessingJobTransitionSource.SYSTEM,
                null,
                "Demo regulatory report processed successfully"
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
    void startProcessingFailsWhenCurrentUserIsNotAnalyst() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = pendingJob(UploadedFileStatus.STORED);

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(currentUserProvider.getCurrentUser()).thenReturn(administrator());

        assertThatThrownBy(() -> processingJobWorkflowService.startProcessing(jobId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You are not allowed to start processing");

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION);
        verifyNoInteractions(processingJobHistoryService);
    }

    @Test
    void startProcessingFailsWhenAnalystDoesNotOwnJob() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = new ProcessingJob(uploadedFile(UploadedFileStatus.STORED, "otherAnalyst"), "File uploaded");

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(currentUserProvider.getCurrentUser()).thenReturn(analyst());

        assertThatThrownBy(() -> processingJobWorkflowService.startProcessing(jobId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You can only start processing jobs uploaded by your user");

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION);
        verifyNoInteractions(processingJobHistoryService);
    }

    @ParameterizedTest
    @EnumSource(
            value = UploadedFileStatus.class,
            names = {
                    "PENDING_CORRECTION",
                    "FAILED",
                    "MISSING",
                    "DELETED"
            }
    )
    void startProcessingThrowsBusinessConflictWhenUploadedFileCannotBeProcessed(
            UploadedFileStatus fileStatus
    ) {
        UUID jobId = UUID.randomUUID();
        ProcessingJob processingJob = new ProcessingJob(
                uploadedFile(fileStatus, "analyst01"),
                "File uploaded"
        );

        when(processingJobQueryService.getJob(jobId)).thenReturn(processingJob);

        assertThatThrownBy(() -> processingJobWorkflowService.startProcessing(jobId))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("The file cannot be processed because it is in state " + fileStatus);

        verify(processingJobQueryService).requireCanView(processingJob);
        verifyNoInteractions(currentUserProvider);
        verifyNoInteractions(processingJobHistoryService);
        verifyNoInteractions(regulatoryReportProcessorRegistry);
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
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
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

    @Test
    void approveFailsWhenCurrentUserIsNotAdministrator() {
        UUID jobId = UUID.randomUUID();
        ProcessingJob job = awaitingApprovalJob();

        when(processingJobQueryService.getJob(jobId)).thenReturn(job);
        when(currentUserProvider.getCurrentUser()).thenReturn(analyst());

        assertThatThrownBy(() -> processingJobWorkflowService.approve(jobId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You are not allowed to approve processing jobs");

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL);
        verifyNoInteractions(processingJobHistoryService);
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
        return uploadedFile(status, "analyst01");
    }

    private UploadedFile uploadedFile(UploadedFileStatus status, String username) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                status,
                analyst(username)
        );
    }

    private User analyst() {
        return analyst("analyst01");
    }

    private User analyst(String username) {
        User user = new User(username, username + "@example.com", "Analyst", null, false,
                UserStatus.ACTIVE);
        user.getRoles().add(role(UserRole.ANALYST));
        return user;
    }

    private User administrator() {
        User user = new User("admin01", "admin01@example.com", "Admin 01", null, false,
                UserStatus.ACTIVE);
        user.getRoles().add(role(UserRole.ADMINISTRATOR));
        return user;
    }

    private Role role(UserRole userRole) {
        return new Role(userRole.name(), userRole.name(), null);
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
