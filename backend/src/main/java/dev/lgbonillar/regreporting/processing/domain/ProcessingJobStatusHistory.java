package dev.lgbonillar.regreporting.processing.domain;

import dev.lgbonillar.regreporting.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_job_status_history")
@Getter
@Setter
@NoArgsConstructor
public class ProcessingJobStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processing_job_id", nullable = false)
    private ProcessingJob processingJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private ProcessingJobStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ProcessingJobStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_source", nullable = false)
    private ProcessingJobTransitionSource transitionSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transitioned_by_user_id")
    private User transitionedBy;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProcessingJobStatusHistory(
            ProcessingJob processingJob,
            ProcessingJobStatus previousStatus,
            ProcessingJobStatus newStatus,
            ProcessingJobTransitionSource transitionSource,
            User transitionedBy,
            String reason
    ) {
        this.processingJob = processingJob;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.transitionSource = transitionSource;
        this.transitionedBy = transitionedBy;
        this.reason = reason;
    }
}
