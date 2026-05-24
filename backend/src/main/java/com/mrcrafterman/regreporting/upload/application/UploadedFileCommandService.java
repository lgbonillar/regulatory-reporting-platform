package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.processing.application.ProcessingJobCreationService;
import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.processing.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.StoredFile;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
import com.mrcrafterman.regreporting.users.application.CurrentUserProvider;
import com.mrcrafterman.regreporting.users.domain.User;
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

    public UploadedFileCommandService(
            CurrentUserProvider currentUserProvider,
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobCreationService processingJobCreationService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobCreationService = processingJobCreationService;
    }

    @Transactional
    public ReportFileUploadResponse uploadReportFile(MultipartFile file) {
        User currentUser = currentUserProvider.getCurrentUser();
        String username = currentUser.getUsername();
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());

        UploadedFile existingFile = uploadedFileRepository
                .findByUploadedByIdAndOriginalFilename(currentUser.getId(), originalFilename)
                .orElse(null);

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

        uploadedFile.replaceWith(
                storedFile.storedFilename(),
                storedFile.relativeStoragePath(),
                file.getContentType(),
                file.getSize(),
                storedFile.checksum()
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

        fileStorageService.delete(uploadedFile.getStoragePath());
        uploadedFile.markDeleted();
    }

    @Transactional
    public void markUploadedFileAsMissing(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        uploadedFile.markMissing();
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
