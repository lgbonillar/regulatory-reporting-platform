package com.mrcrafterman.regreporting.upload.domain;

import com.mrcrafterman.regreporting.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_jobs")
@Getter
@Setter
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
        this.status = ProcessingJobStatus.PENDING_EXECUTION;
        this.message = message;
        this.updatedAt = LocalDateTime.now();
    }

}
