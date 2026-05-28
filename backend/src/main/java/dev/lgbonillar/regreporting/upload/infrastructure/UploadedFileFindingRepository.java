package dev.lgbonillar.regreporting.upload.infrastructure;

import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadedFileFindingRepository extends JpaRepository<UploadedFileFinding, UUID> {

    List<UploadedFileFinding> findAllByUploadedFile_IdOrderByCreatedAtAsc(UUID uploadedFileId);

    List<UploadedFileFinding> findAllByValidationRun_IdOrderByCreatedAtAsc(UUID validationRunId);

}