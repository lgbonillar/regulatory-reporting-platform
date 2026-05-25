package com.mrcrafterman.regreporting.processing.domain;

import com.mrcrafterman.regreporting.shared.BusinessConflictException;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_jobs")
@Getter
@NoArgsConstructor
public class ProcessingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_file_id", nullable = false)
    private UploadedFile uploadedFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessingJobStatus status;

    @Column(name = "message")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    private User triggeredBy;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by_user_id")
    private User rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_user_id")
    private User revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revocation_reason", length = 1000)
    private String revocationReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProcessingJob(UploadedFile uploadedFile, String message) {
        this.uploadedFile = uploadedFile;
        this.status = ProcessingJobStatus.PENDING_EXECUTION;
        this.message = message;
    }

    public void markPendingExecution(String message) {
        requireStatus(ProcessingJobStatus.PENDING_EXECUTION, "update the pending file");

        this.message = message;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean allowsFileModification() {
        return this.status == ProcessingJobStatus.PENDING_EXECUTION;
    }

    public void startProcessing(User triggeredBy) {
        requireUser(triggeredBy, "User is required to start processing");
        requireStatus(ProcessingJobStatus.PENDING_EXECUTION, "start processing");

        this.status = ProcessingJobStatus.PROCESSING;
        this.triggeredBy = triggeredBy;
        this.triggeredAt = LocalDateTime.now();
        this.message = "Processing started";
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessingCompleted() {
        requireStatus(ProcessingJobStatus.PROCESSING, "complete processing");

        this.status = ProcessingJobStatus.AWAITING_APPROVAL;
        this.processingCompletedAt = LocalDateTime.now();
        this.failureReason = null;
        this.message = "Processing completed; awaiting administrator approval";
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessingFailed(String reason) {
        requireStatus(ProcessingJobStatus.PROCESSING, "fail processing");
        requireReason(reason, "Failure reason is required");

        this.status = ProcessingJobStatus.PROCESSING_FAILED;
        this.processingCompletedAt = LocalDateTime.now();
        this.failureReason = reason.trim();
        this.message = "Processing failed";
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(User administrator) {
        requireUser(administrator, "Administrator is required");
        requireStatus(ProcessingJobStatus.AWAITING_APPROVAL, "approve");

        this.status = ProcessingJobStatus.APPROVED;
        this.approvedBy = administrator;
        this.approvedAt = LocalDateTime.now();
        this.message = "Submission approved";
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(User administrator, String reason) {
        requireUser(administrator, "Administrator is required");
        requireStatus(ProcessingJobStatus.AWAITING_APPROVAL, "reject");
        requireReason(reason, "Rejection reason is required");

        this.status = ProcessingJobStatus.REJECTED;
        this.rejectedBy = administrator;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason.trim();
        this.message = "Submission rejected";
        this.updatedAt = LocalDateTime.now();
    }

    public void revoke(User administrator, String reason) {
        requireUser(administrator, "Administrator is required");
        requireStatus(ProcessingJobStatus.APPROVED, "revoke");
        requireReason(reason, "Revocation reason is required");

        this.status = ProcessingJobStatus.REVOKED;
        this.revokedBy = administrator;
        this.revokedAt = LocalDateTime.now();
        this.revocationReason = reason.trim();
        this.message = "Previously approved submission revoked";
        this.updatedAt = LocalDateTime.now();
    }

    private void requireStatus(ProcessingJobStatus requiredStatus, String action) {
        if (this.status != requiredStatus) {
            throw new BusinessConflictException(
                    "Cannot " + action + " processing job in state " + this.status
            );
        }
    }

    private void requireUser(User user, String message) {
        if (user == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireReason(String reason, String message) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

}
