package dev.lgbonillar.regreporting.upload.infrastructure;

import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadedFileStatusHistoryRepository extends
        JpaRepository<UploadedFileStatusHistory, UUID> {

    List<UploadedFileStatusHistory> findAllByUploadedFile_IdOrderByCreatedAtAsc(UUID uploadedFileId);
}