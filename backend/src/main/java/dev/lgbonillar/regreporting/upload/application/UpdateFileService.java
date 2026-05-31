package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.application.support.UploadedFileStatusMessages;
import dev.lgbonillar.regreporting.upload.application.support.UploadedFileStatusTransitions;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.StoredFile;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class UpdateFileService {

    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final ProcessingJobCreationService processingJobCreationService;
    private final UploadedFileStatusHistoryService uploadedFileStatusHistoryService;
    private final UploadedFileValidationService uploadedFileValidationService;

    public UpdateFileService(
            CurrentUserProvider currentUserProvider,
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobCreationService processingJobCreationService,
            UploadedFileStatusHistoryService uploadedFileStatusHistoryService,
            UploadedFileValidationService uploadedFileValidationService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobCreationService = processingJobCreationService;
        this.uploadedFileStatusHistoryService = uploadedFileStatusHistoryService;
        this.uploadedFileValidationService = uploadedFileValidationService;
    }

    @Transactional
    public ReportFileUploadResponse updateFile(UUID fileId, MultipartFile file) {
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
            processingJobCreationService.ensureFileCanBeUpdated(processingJob);
        }

        StoredFile storedFile = fileStorageService.replace(file, currentUser, uploadedFile);

        uploadedFile.replaceWith(
                storedFile.storedFilename(),
                storedFile.relativeStoragePath(),
                file.getContentType(),
                file.getSize(),
                storedFile.checksum()
        );

        UploadedFileStatus previousStatus = uploadedFile.getStatus();

        UploadedFileStatus validationStatus = uploadedFileValidationService.validate(
                uploadedFile,
                UploadedFileValidationRunSource.REPLACEMENT,
                currentUser.getUsername()
        );

        UploadedFileStatusTransitions.applyValidationStatus(uploadedFile, validationStatus);

        uploadedFileStatusHistoryService.recordTransition(
                uploadedFile,
                previousStatus,
                validationStatus,
                UploadedFileTransitionSource.USER,
                currentUser,
                UploadedFileStatusMessages.updateHistoryMessage(validationStatus)
        );

        ProcessingJob savedJob = updateProcessingJobAfterValidation(
                uploadedFile,
                processingJob,
                currentUser
        );

        return toReportFileUploadResponse(uploadedFile, savedJob, validationStatus);
    }

    private ProcessingJob updateProcessingJobAfterValidation(
            UploadedFile uploadedFile,
            ProcessingJob processingJob,
            User currentUser
    ) {
        if (uploadedFile.getStatus() != UploadedFileStatus.STORED) {
            return processingJob;
        }

        if (processingJob == null) {
            return processingJobCreationService.createInitialJob(uploadedFile, currentUser);
        }

        return processingJobCreationService.markFileUpdated(processingJob);
    }

    private ReportFileUploadResponse toReportFileUploadResponse(
            UploadedFile uploadedFile,
            ProcessingJob processingJob,
            UploadedFileStatus status
    ) {
        return new ReportFileUploadResponse(
                uploadedFile.getId(),
                processingJob == null ? null : processingJob.getId(),
                uploadedFile.getOriginalFilename(),
                status.name(),
                processingJob == null ? null : processingJob.getStatus().name(),
                processingJob == null
                        ? UploadedFileStatusMessages.updateResponseMessage(status)
                        : processingJob.getMessage()
        );
    }
}