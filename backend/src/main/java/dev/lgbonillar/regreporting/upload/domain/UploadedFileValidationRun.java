package dev.lgbonillar.regreporting.upload.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploaded_file_validation_runs")
@Getter
@NoArgsConstructor
public class UploadedFileValidationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_file_id", nullable = false)
    private UploadedFile uploadedFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadedFileValidationRunStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadedFileValidationRunSource source;

    @Column(name = "summary_message")
    private String summaryMessage;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UploadedFileValidationRun(
            UploadedFile uploadedFile,
            UploadedFileValidationRunStatus status,
            UploadedFileValidationRunSource source,
            String summaryMessage,
            String createdBy
    ) {
        this.uploadedFile = uploadedFile;
        this.status = status;
        this.source = source;
        this.summaryMessage = summaryMessage;
        this.createdBy = createdBy;
    }
}
