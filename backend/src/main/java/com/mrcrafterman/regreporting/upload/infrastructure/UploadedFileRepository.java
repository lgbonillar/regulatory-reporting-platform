package com.mrcrafterman.regreporting.upload.infrastructure;

import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    Optional<UploadedFile> findByUploadedByAndOriginalFilename(
            String uploadedBy,
            String originalFilename
    );

    List<UploadedFile> findByUploadedByAndStatusOrderByUploadedAtDesc(
            String uploadedBy,
            UploadedFileStatus status
    );

    Optional<UploadedFile> findByIdAndUploadedByAndStatus(
            UUID id,
            String uploadedBy,
            UploadedFileStatus status
    );

}
