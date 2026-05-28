package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.application.ProcessingJobCreationService;
import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileTransitionSource;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.StoredFile;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileRepository;
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

    public UploadedFileCommandService(
            CurrentUserProvider currentUserProvider,
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobCreationService processingJobCreationService,
            UploadedFileStatusHistoryService uploadedFileStatusHistoryService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobCreationService = processingJobCreationService;
        this.uploadedFileStatusHistoryService = uploadedFileStatusHistoryService;
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
                    .orElseThrow(() -> new IllegalStateException(
                            "Processing job not found for uploaded file"
                    ));

            processingJobCreationService.ensureFileCanBeReplaced(existingJob);
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

            existingFile.markStored();

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

        if (existingFile == null) {
            uploadedFileStatusHistoryService.recordTransition(
                    savedFile,
                    null,
                    UploadedFileStatus.STORED,
                    UploadedFileTransitionSource.USER,
                    currentUser,
                    "File uploaded successfully"
            );
        } else {
            uploadedFileStatusHistoryService.recordTransition(
                    savedFile,
                    previousStatus,
                    UploadedFileStatus.STORED,
                    UploadedFileTransitionSource.USER,
                    currentUser,
                    "File replaced successfully"
            );
        }

        ProcessingJob processingJob = existingJob == null
                ? processingJobCreationService.createInitialJob(savedFile, currentUser)
                : processingJobCreationService.markFileReplaced(existingJob);

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
                .orElseThrow(() -> new IllegalStateException(
                        "Processing job not found for uploaded file"
                ));

        processingJobCreationService.ensureFileCanBeUpdated(processingJob);

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

        uploadedFile.markStored();

        uploadedFileStatusHistoryService.recordTransition(
                uploadedFile,
                previousStatus,
                UploadedFileStatus.STORED,
                UploadedFileTransitionSource.USER,
                currentUser,
                "File updated successfully"
        );

        ProcessingJob savedJob = processingJobCreationService.markFileUpdated(processingJob);

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
                .orElseThrow(() -> new IllegalStateException(
                        "Processing job not found for uploaded file"
                ));

        processingJobCreationService.ensureFileCanBeDeleted(processingJob);

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
                processingJob.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStatus().name(),
                processingJob.getStatus().name(),
                processingJob.getMessage()
        );
    }

}
