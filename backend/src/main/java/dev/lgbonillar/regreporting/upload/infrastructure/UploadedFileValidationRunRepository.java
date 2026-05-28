package dev.lgbonillar.regreporting.upload.infrastructure;

import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadedFileValidationRunRepository extends JpaRepository<UploadedFileValidationRun, UUID> {

    List<UploadedFileValidationRun> findAllByUploadedFile_IdOrderByCreatedAtDesc(UUID uploadedFileId);
}
