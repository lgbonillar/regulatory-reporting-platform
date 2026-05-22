package com.mrcrafterman.regreporting.upload.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploaded_files")
@Getter
@Setter
@NoArgsConstructor
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UploadedFileStatus status;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UploadedFile(
            String originalFilename,
            String storedFilename,
            String storagePath,
            String contentType,
            long fileSize,
            String checksum,
            UploadedFileStatus status,
            String uploadedBy
    ) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.status = status;
        this.uploadedBy = uploadedBy;
    }

    public void replaceWith(
            String storedFilename,
            String storagePath,
            String contentType,
            long fileSize,
            String checksum
    ) {
        this.storedFilename = storedFilename;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.status = UploadedFileStatus.STORED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.status = UploadedFileStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markMissing() {
        this.status = UploadedFileStatus.MISSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = UploadedFileStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

}
