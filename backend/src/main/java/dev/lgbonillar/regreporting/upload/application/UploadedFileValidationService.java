package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidationResult;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidatorRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UploadedFileValidationService {

    private final UploadedFileValidatorRegistry validatorRegistry;
    private final UploadedFileValidationRunService validationRunService;
    private final UploadedFileFindingService findingService;

    public UploadedFileValidationService(
            UploadedFileValidatorRegistry validatorRegistry,
            UploadedFileValidationRunService validationRunService,
            UploadedFileFindingService findingService
    ) {
        this.validatorRegistry = validatorRegistry;
        this.validationRunService = validationRunService;
        this.findingService = findingService;
    }

    @Transactional
    public UploadedFileStatus validate(UploadedFile uploadedFile, UploadedFileValidationRunSource source, String username) {
        UploadedFileValidationResult result = validatorRegistry
                .getDefaultValidator()
                .validate(uploadedFile);

        UploadedFileValidationRunStatus runStatus = toRunStatus(result);

        UploadedFileValidationRun validationRun = validationRunService.createValidationRun(
                uploadedFile,
                runStatus,
                source,
                validationSummary(runStatus),
                username
        );

        if (!result.findings().isEmpty()) {
            findingService.saveFindings(validationRun, uploadedFile, result.findings());
        }

        return toFileStatus(runStatus);
    }

    private UploadedFileValidationRunStatus toRunStatus(UploadedFileValidationResult result) {
        if (!result.hasErrors()) {
            return UploadedFileValidationRunStatus.PASSED;
        }

        boolean hasSystemError = result.findings().stream()
                .anyMatch(finding -> finding.severity() == ProcessingFindingSeverity.ERROR
                        && finding.scope() == ProcessingFindingScope.SYSTEM);

        return hasSystemError
                ? UploadedFileValidationRunStatus.SYSTEM_FAILED
                : UploadedFileValidationRunStatus.FAILED;
    }

    private UploadedFileStatus toFileStatus(UploadedFileValidationRunStatus runStatus) {
        return switch (runStatus) {
            case PASSED -> UploadedFileStatus.STORED;
            case FAILED -> UploadedFileStatus.PENDING_CORRECTION;
            case SYSTEM_FAILED -> UploadedFileStatus.FAILED;
        };
    }

    private String validationSummary(UploadedFileValidationRunStatus status) {
        return switch (status) {
            case PASSED -> "Uploaded file validation passed";
            case FAILED -> "Uploaded file validation found issues";
            case SYSTEM_FAILED -> "Uploaded file validation failed due to a system error";
        };
    }
}