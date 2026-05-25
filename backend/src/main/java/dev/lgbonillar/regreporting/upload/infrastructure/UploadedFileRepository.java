package dev.lgbonillar.regreporting.upload.infrastructure;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    Optional<UploadedFile> findByUploadedByIdAndOriginalFilename(
            UUID uploadedById,
            String originalFilename
    );

    List<UploadedFile> findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
            UUID uploadedById,
            List<UploadedFileStatus> statuses
    );

    Optional<UploadedFile> findByIdAndUploadedByIdAndStatusIn(
            UUID id,
            UUID uploadedById,
            List<UploadedFileStatus> statuses
    );

}
