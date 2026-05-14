package com.mrcrafterman.regreporting.upload.domain;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProcessingJob(UploadedFile uploadedFile, ProcessingJobStatus status, String message) {
        this.uploadedFile = uploadedFile;
        this.status = status;
        this.message = message;
    }

    public void markPending(String message) {
        this.status = ProcessingJobStatus.PENDING;
        this.message = message;
        this.updatedAt = LocalDateTime.now();
    }


}
