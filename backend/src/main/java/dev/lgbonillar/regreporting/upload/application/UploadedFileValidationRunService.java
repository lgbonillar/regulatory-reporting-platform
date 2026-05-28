package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileValidationRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UploadedFileValidationRunService {

    private final UploadedFileValidationRunRepository validationRunRepository;

    public UploadedFileValidationRunService(
            UploadedFileValidationRunRepository validationRunRepository
    ) {
        this.validationRunRepository = validationRunRepository;
    }

    @Transactional
    public UploadedFileValidationRun createValidationRun(
            UploadedFile uploadedFile,
            UploadedFileValidationRunStatus status,
            UploadedFileValidationRunSource source,
            String summaryMessage,
            String createdBy
    ) {
        return validationRunRepository.save(new UploadedFileValidationRun(
                uploadedFile,
                status,
                source,
                summaryMessage,
                createdBy
        ));
    }

    @Transactional(readOnly = true)
    public List<UploadedFileValidationRun> listValidationRuns(UUID uploadedFileId) {
        return
                validationRunRepository.findAllByUploadedFile_IdOrderByCreatedAtDesc(uploadedFileId);
    }
}
