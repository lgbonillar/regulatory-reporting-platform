package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.StoredFile;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidationResult;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidatorRegistry;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UploadedFileCommandService {

    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final ProcessingJobCreationService processingJobCreationService;
    private final UploadedFileStatusHistoryService uploadedFileStatusHistoryService;
    private final UploadedFileValidatorRegistry uploadedFileValidatorRegistry;
    private final UploadedFileValidationRunService validationRunService;
    private final UploadedFileFindingService findingService;

    public UploadedFileCommandService(
            CurrentUserProvider currentUserProvider,
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobCreationService processingJobCreationService,
            UploadedFileStatusHistoryService uploadedFileStatusHistoryService,
            UploadedFileValidatorRegistry uploadedFileValidatorRegistry,
            UploadedFileValidationRunService validationRunService,
            UploadedFileFindingService findingService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobCreationService = processingJobCreationService;
        this.uploadedFileStatusHistoryService = uploadedFileStatusHistoryService;
        this.uploadedFileValidatorRegistry = uploadedFileValidatorRegistry;
        this.validationRunService = validationRunService;
        this.findingService = findingService;
    }

    @Transactional
    public ReportFileUploadResponse uploadReportFile(MultipartFile file) {
        User currentUser = currentUserProvider.getCurrentUser();
        String username = currentUser.getUsername();
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());

        UploadedFile existingFile = uploadedFileRepository
                .findByUploadedByIdAndOriginalFilename(currentUser.getId(), originalFilename)
                .orElse(null);

        UploadedFileStatus previousStatus = existingFile == null
                ? null
                : existingFile.getStatus();

        ProcessingJob existingJob = null;

        if (existingFile != null) {
            existingJob = processingJobCreationService
                    .findByUploadedFile(existingFile)
                    .orElse(null);

            if (existingJob != null) {
                processingJobCreationService.ensureFileCanBeReplaced(existingJob);
            }
        }

        StoredFile storedFile = fileStorageService.store(file, username);

        UploadedFile uploadedFile;

        if (existingFile != null) {
            existingFile.replaceWith(
                    storedFile.storedFilename(),
                    storedFile.relativeStoragePath(),
                    file.getContentType(),
                    file.getSize(),
                    storedFile.checksum()
            );

            uploadedFile = existingFile;
        } else {
            uploadedFile = new UploadedFile(
                    originalFilename,
                    storedFile.storedFilename(),
                    storedFile.relativeStoragePath(),
                    file.getContentType(),
                    file.getSize(),
                    storedFile.checksum(),
                    UploadedFileStatus.STORED,
                    currentUser
            );
        }

        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
        UploadedFileValidationOutcome validationOutcome = validateUploadedFile(
                savedFile,
                existingFile == null
                        ? UploadedFileValidationRunSource.UPLOAD
                        : UploadedFileValidationRunSource.REPLACEMENT,
                currentUser
        );
        UploadedFileStatus targetStatus = validationOutcome.fileStatus();

        applyValidationStatus(savedFile, targetStatus);

        if (existingFile == null) {
            uploadedFileStatusHistoryService.recordTransition(
                    savedFile,
                    null,
                    targetStatus,
                    UploadedFileTransitionSource.USER,
                    currentUser,
                    uploadHistoryMessage(targetStatus)
            );
        } else {
            uploadedFileStatusHistoryService.recordTransition(
                    savedFile,
                    previousStatus,
                    targetStatus,
                    UploadedFileTransitionSource.USER,
                    currentUser,
                    replacementHistoryMessage(targetStatus)
            );
        }

        ProcessingJob processingJob = updateProcessingJobAfterValidation(
                savedFile,
                existingJob,
                currentUser,
                existingFile == null
                        ? ProcessingJobUpdateAction.CREATE
                        : ProcessingJobUpdateAction.REPLACE
        );

        return toReportFileUploadResponse(savedFile, processingJob);
    }

    @Transactional
    public ReportFileUploadResponse updateReportFile(UUID fileId, MultipartFile file) {
        User currentUser = currentUserProvider.getCurrentUser();
        String username = currentUser.getUsername();

        UploadedFile uploadedFile = uploadedFileRepository
                .findByIdAndUploadedByIdAndStatusIn(
                        fileId,
                        currentUser.getId(),
                        List.of(
                                UploadedFileStatus.STORED,
                                UploadedFileStatus.PENDING_CORRECTION,
                                UploadedFileStatus.MISSING,
                                UploadedFileStatus.FAILED
                        )
                )
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        ProcessingJob processingJob = processingJobCreationService
                .findByUploadedFile(uploadedFile)
                .orElse(null);

        if (processingJob != null) {
            processingJobCreationService.ensureFileCanBeUpdated(processingJob);
        }

        String previousStoragePath = uploadedFile.getStoragePath();

        StoredFile storedFile = fileStorageService.store(file, username);

        if (!previousStoragePath.equals(storedFile.relativeStoragePath())) {
            fileStorageService.delete(previousStoragePath);
        }

        UploadedFileStatus previousStatus = uploadedFile.getStatus();

        uploadedFile.replaceWith(
                storedFile.storedFilename(),
                storedFile.relativeStoragePath(),
                file.getContentType(),
                file.getSize(),
                storedFile.checksum()
        );

        UploadedFileValidationOutcome validationOutcome = validateUploadedFile(
                uploadedFile,
                UploadedFileValidationRunSource.REPLACEMENT,
                currentUser
        );
        UploadedFileStatus targetStatus = validationOutcome.fileStatus();

        applyValidationStatus(uploadedFile, targetStatus);

        uploadedFileStatusHistoryService.recordTransition(
                uploadedFile,
                previousStatus,
                targetStatus,
                UploadedFileTransitionSource.USER,
                currentUser,
                updateHistoryMessage(targetStatus)
        );

        ProcessingJob savedJob = updateProcessingJobAfterValidation(
                uploadedFile,
                processingJob,
                currentUser,
                ProcessingJobUpdateAction.UPDATE
        );

        return toReportFileUploadResponse(uploadedFile, savedJob);
    }

    @Transactional
    public void deleteUploadedFile(UUID fileId) {
        User currentUser = currentUserProvider.getCurrentUser();

        UploadedFile uploadedFile = uploadedFileRepository
                .findByIdAndUploadedByIdAndStatusIn(
                        fileId,
                        currentUser.getId(),
                        List.of(
                                UploadedFileStatus.STORED,
                                UploadedFileStatus.PENDING_CORRECTION,
                                UploadedFileStatus.MISSING,
                                UploadedFileStatus.FAILED
                        )
                )
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        ProcessingJob processingJob = processingJobCreationService
                .findByUploadedFile(uploadedFile)
                .orElse(null);

        if (processingJob != null) {
            processingJobCreationService.ensureFileCanBeDeleted(processingJob);
        }

        UploadedFileStatus previousStatus = uploadedFile.getStatus();

        fileStorageService.delete(uploadedFile.getStoragePath());
        uploadedFile.markDeleted();

        uploadedFileStatusHistoryService.recordTransition(
                uploadedFile,
                previousStatus,
                UploadedFileStatus.DELETED,
                UploadedFileTransitionSource.USER,
                currentUser,
                "File deleted"
        );
    }

    @Transactional
    public void markUploadedFileAsMissing(UUID fileId) {
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

    private ReportFileUploadResponse toReportFileUploadResponse(
            UploadedFile uploadedFile,
            ProcessingJob processingJob
    ) {
        return new ReportFileUploadResponse(
                uploadedFile.getId(),
                processingJob == null ? null : processingJob.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStatus().name(),
                processingJob == null ? null : processingJob.getStatus().name(),
                processingJob == null
                        ? uploadMessage(uploadedFile.getStatus())
                        : processingJob.getMessage()
        );
    }

    private UploadedFileValidationOutcome validateUploadedFile(
            UploadedFile uploadedFile,
            UploadedFileValidationRunSource source,
            User currentUser
    ) {
        UploadedFileValidationResult result = uploadedFileValidatorRegistry
                .getDefaultValidator()
                .validate(uploadedFile);
        UploadedFileValidationRunStatus runStatus = validationRunStatus(result);
        UploadedFileValidationRun validationRun = validationRunService.createValidationRun(
                uploadedFile,
                runStatus,
                source,
                validationSummary(runStatus),
                currentUser.getUsername()
        );

        if (!result.findings().isEmpty()) {
            findingService.saveFindings(validationRun, uploadedFile, result.findings());
        }

        return new UploadedFileValidationOutcome(fileStatus(runStatus));
    }

    private UploadedFileValidationRunStatus validationRunStatus(
            UploadedFileValidationResult result
    ) {
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

    private UploadedFileStatus fileStatus(UploadedFileValidationRunStatus runStatus) {
        return switch (runStatus) {
            case PASSED -> UploadedFileStatus.STORED;
            case FAILED -> UploadedFileStatus.PENDING_CORRECTION;
            case SYSTEM_FAILED -> UploadedFileStatus.FAILED;
        };
    }

    private void applyValidationStatus(
            UploadedFile uploadedFile,
            UploadedFileStatus status
    ) {
        switch (status) {
            case STORED -> uploadedFile.markStored();
            case PENDING_CORRECTION -> uploadedFile.markPendingCorrection();
            case FAILED -> uploadedFile.markFailed();
            default -> throw new IllegalArgumentException("Unsupported validation status: " + status);
        }
    }

    private ProcessingJob updateProcessingJobAfterValidation(
            UploadedFile uploadedFile,
            ProcessingJob processingJob,
            User currentUser,
            ProcessingJobUpdateAction action
    ) {
        if (uploadedFile.getStatus() != UploadedFileStatus.STORED) {
            return processingJob;
        }

        if (processingJob == null) {
            return processingJobCreationService.createInitialJob(uploadedFile, currentUser);
        }

        return switch (action) {
            case CREATE -> processingJob;
            case REPLACE -> processingJobCreationService.markFileReplaced(processingJob);
            case UPDATE -> processingJobCreationService.markFileUpdated(processingJob);
        };
    }

    private String validationSummary(UploadedFileValidationRunStatus status) {
        return switch (status) {
            case PASSED -> "Uploaded file validation passed";
            case FAILED -> "Uploaded file validation found issues";
            case SYSTEM_FAILED -> "Uploaded file validation failed due to a system error";
        };
    }

    private String uploadHistoryMessage(UploadedFileStatus status) {
        return switch (status) {
            case STORED -> "File uploaded successfully";
            case PENDING_CORRECTION -> "File uploaded with validation issues";
            case FAILED -> "File upload validation failed";
            default -> "File uploaded";
        };
    }

    private String replacementHistoryMessage(UploadedFileStatus status) {
        return switch (status) {
            case STORED -> "File replaced successfully";
            case PENDING_CORRECTION -> "File replaced with validation issues";
            case FAILED -> "File replacement validation failed";
            default -> "File replaced";
        };
    }

    private String updateHistoryMessage(UploadedFileStatus status) {
        return switch (status) {
            case STORED -> "File updated successfully";
            case PENDING_CORRECTION -> "File updated with validation issues";
            case FAILED -> "File update validation failed";
            default -> "File updated";
        };
    }

    private String uploadMessage(UploadedFileStatus status) {
        return switch (status) {
            case PENDING_CORRECTION -> "File uploaded with validation issues";
            case FAILED -> "File upload validation failed";
            default -> "File uploaded";
        };
    }

    private record UploadedFileValidationOutcome(
            UploadedFileStatus fileStatus
    ) {
    }

    private enum ProcessingJobUpdateAction {
        CREATE,
        REPLACE,
        UPDATE
    }

}
