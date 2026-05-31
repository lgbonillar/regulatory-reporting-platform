package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MarkFileMissingService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UploadedFileStatusHistoryService uploadedFileStatusHistoryService;

    public MarkFileMissingService(
            UploadedFileRepository uploadedFileRepository,
            UploadedFileStatusHistoryService uploadedFileStatusHistoryService
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.uploadedFileStatusHistoryService = uploadedFileStatusHistoryService;
    }

    @Transactional
    public void markFileAsMissing(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        UploadedFileStatus previousStatus = uploadedFile.getStatus();

        uploadedFile.markMissing();

        uploadedFileStatusHistoryService.recordTransition(
                uploadedFile,
                previousStatus,
                UploadedFileStatus.MISSING,
                UploadedFileTransitionSource.SYSTEM,
                null,
                "Stored file was not found"
        );
    }
}