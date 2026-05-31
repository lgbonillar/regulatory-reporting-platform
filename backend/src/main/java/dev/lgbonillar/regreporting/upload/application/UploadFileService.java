package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
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

import java.util.Objects;
import java.util.UUID;

@Service
public class UploadFileService {

    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final ProcessingJobCreationService processingJobCreationService;
    private final UploadedFileStatusHistoryService uploadedFileStatusHistoryService;
    private final UploadedFileValidationService uploadedFileValidationService;

    public UploadFileService(
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
    public ReportFileUploadResponse uploadFile(MultipartFile file) {
        User currentUser = currentUserProvider.getCurrentUser();

        Objects.requireNonNull(file.getOriginalFilename(), "File name is required");

        StoredFile storedFile = fileStorageService.store(file, currentUser);

        UploadedFile uploadedFile = new UploadedFile(
                storedFile.originalFilename(),
                storedFile.storedFilename(),
                storedFile.relativeStoragePath(),
                file.getContentType(),
                file.getSize(),
                storedFile.checksum(),
                UploadedFileStatus.PENDING_VALIDATION,
                currentUser
        );

        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);

        UploadedFileStatus validationStatus = uploadedFileValidationService.validate(
                savedFile,
                UploadedFileValidationRunSource.UPLOAD,
                currentUser.getUsername()
        );

        UploadedFileStatusApplier.applyStatus(savedFile, validationStatus);

        uploadedFileStatusHistoryService.recordTransition(
                savedFile,
                null,
                validationStatus,
                UploadedFileTransitionSource.USER,
                currentUser,
                StatusMessageHelper.historyMessage(validationStatus, "uploaded")
        );

        ProcessingJob processingJob = null;
        if (validationStatus == UploadedFileStatus.STORED) {
            processingJob = processingJobCreationService.createInitialJob(savedFile, currentUser);
        }

        return toReportFileUploadResponse(savedFile, processingJob, validationStatus);
    }

    private String historyMessage(UploadedFileStatus status) {
        return switch (status) {
            case STORED -> "File uploaded successfully";
            case PENDING_CORRECTION -> "File uploaded with validation issues";
            case FAILED -> "File upload validation failed";
            default -> "File uploaded";
        };
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
                        ? uploadMessage(status)
                        : processingJob.getMessage()
        );
    }

    private String uploadMessage(UploadedFileStatus status) {
        return StatusMessageHelper.statusMessage(status, "uploaded");
    }
}