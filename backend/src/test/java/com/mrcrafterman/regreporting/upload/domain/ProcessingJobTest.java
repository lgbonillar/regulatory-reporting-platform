package com.mrcrafterman.regreporting.upload.domain;

import com.mrcrafterman.regreporting.shared.BusinessConflictException;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessingJobTest {

    @Test
    void newJobStartsPendingExecution() {
        UploadedFile file = storedFile();

        ProcessingJob job = new ProcessingJob(file, "File uploaded");

        assertThat(job.getUploadedFile()).isSameAs(file);
        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PENDING_EXECUTION);
        assertThat(job.getMessage()).isEqualTo("File uploaded");
        assertThat(job.allowsFileModification()).isTrue();
    }

    @Test
    void startProcessingMovesPendingJobToProcessing() {
        ProcessingJob job = pendingJob();
        User admin = administrator();

        job.startProcessing(admin);

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PROCESSING);
        assertThat(job.getTriggeredBy()).isSameAs(admin);
        assertThat(job.getTriggeredAt()).isNotNull();
        assertThat(job.getMessage()).isEqualTo("Processing started");
        assertThat(job.allowsFileModification()).isFalse();
    }

    @Test
    void completeProcessingMovesJobToAwaitingApproval() {
        ProcessingJob job = processingJob();

        job.markProcessingCompleted();

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.AWAITING_APPROVAL);
        assertThat(job.getProcessingCompletedAt()).isNotNull();
        assertThat(job.getFailureReason()).isNull();
        assertThat(job.getMessage()).isEqualTo("Processing completed; awaiting administrator approval");
    }

    @Test
    void failProcessingStoresTrimmedReason() {
        ProcessingJob job = processingJob();

        job.markProcessingFailed("  Invalid file layout  ");

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.PROCESSING_FAILED);
        assertThat(job.getFailureReason()).isEqualTo("Invalid file layout");
        assertThat(job.getProcessingCompletedAt()).isNotNull();
        assertThat(job.getMessage()).isEqualTo("Processing failed");
    }

    @Test
    void approveMovesAwaitingApprovalJobToApproved() {
        ProcessingJob job = awaitingApprovalJob();
        User admin = administrator();

        job.approve(admin);

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.APPROVED);
        assertThat(job.getApprovedBy()).isSameAs(admin);
        assertThat(job.getApprovedAt()).isNotNull();
        assertThat(job.getMessage()).isEqualTo("Submission approved");
    }

    @Test
    void rejectMovesAwaitingApprovalJobToRejected() {
        ProcessingJob job = awaitingApprovalJob();
        User admin = administrator();

        job.reject(admin, "  Data does not match expected totals  ");

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.REJECTED);
        assertThat(job.getRejectedBy()).isSameAs(admin);
        assertThat(job.getRejectedAt()).isNotNull();
        assertThat(job.getRejectionReason()).isEqualTo("Data does not match expected totals");
        assertThat(job.getMessage()).isEqualTo("Submission rejected");
    }

    @Test
    void revokeMovesApprovedJobToRevoked() {
        ProcessingJob job = approvedJob();
        User admin = administrator();

        job.revoke(admin, "  Approval was granted by mistake  ");

        assertThat(job.getStatus()).isEqualTo(ProcessingJobStatus.REVOKED);
        assertThat(job.getRevokedBy()).isSameAs(admin);
        assertThat(job.getRevokedAt()).isNotNull();
        assertThat(job.getRevocationReason()).isEqualTo("Approval was granted by mistake");
        assertThat(job.getMessage()).isEqualTo("Previously approved submission revoked");
    }

    @Test
    void cannotApprovePendingJob() {
        ProcessingJob job = pendingJob();

        assertThatThrownBy(() -> job.approve(administrator()))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessageContaining("PENDING_EXECUTION");
    }

    @Test
    void failureReasonIsRequired() {
        ProcessingJob job = processingJob();

        assertThatThrownBy(() -> job.markProcessingFailed(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failure reason is required");
    }

    private ProcessingJob pendingJob() {
        return new ProcessingJob(storedFile(), "File uploaded");
    }

    private ProcessingJob processingJob() {
        ProcessingJob job = pendingJob();
        job.startProcessing(administrator());
        return job;
    }

    private ProcessingJob awaitingApprovalJob() {
        ProcessingJob job = processingJob();
        job.markProcessingCompleted();
        return job;
    }

    private ProcessingJob approvedJob() {
        ProcessingJob job = awaitingApprovalJob();
        job.approve(administrator());
        return job;
    }

    private UploadedFile storedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
        );
    }

    private User analyst() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

    private User administrator() {
        return new User(
                "admin01",
                "admin01@example.com",
                "Admin 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

}
