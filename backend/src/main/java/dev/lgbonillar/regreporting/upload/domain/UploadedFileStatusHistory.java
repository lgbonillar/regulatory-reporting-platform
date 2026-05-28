package dev.lgbonillar.regreporting.upload.domain;

import dev.lgbonillar.regreporting.users.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploaded_file_status_history")
@Getter
@Setter
@NoArgsConstructor
public class UploadedFileStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_file_id", nullable = false)
    private UploadedFile uploadedFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private UploadedFileStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private UploadedFileStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_source", nullable = false)
    private UploadedFileTransitionSource transitionSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transitioned_by_user_id")
    private User transitionedBy;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UploadedFileStatusHistory(
            UploadedFile uploadedFile,
            UploadedFileStatus previousStatus,
            UploadedFileStatus newStatus,
            UploadedFileTransitionSource transitionSource,
            User transitionedBy,
            String reason
    ) {
        this.uploadedFile = uploadedFile;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.transitionSource = transitionSource;
        this.transitionedBy = transitionedBy;
        this.reason = reason;
    }
}
