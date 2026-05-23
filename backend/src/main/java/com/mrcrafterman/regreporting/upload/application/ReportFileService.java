package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.StoredFile;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
import com.mrcrafterman.regreporting.users.application.CurrentUserProvider;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReportFileService {

    private final CurrentUserProvider currentUserProvider;
    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final ProcessingJobCreationService processingJobCreationService;
    private final UserRepository userRepository;

    public ReportFileService(
            CurrentUserProvider currentUserProvider,
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobCreationService processingJobCreationService,
            UserRepository userRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobCreationService = processingJobCreationService;
        this.userRepository = userRepository;
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
        ProcessingJob processingJob;

        if (existingJob == null) {
            processingJob = processingJobCreationService.createInitialJob(savedFile, currentUser);
        } else {
            processingJob = processingJobCreationService.markFileReplaced(existingJob);
        }

        return new ReportFileUploadResponse(
                savedFile.getId(),
                processingJob.getId(),
                savedFile.getOriginalFilename(),
                savedFile.getStatus().name(),
                processingJob.getStatus().name(),
                processingJob.getMessage()
        );
    }

    @Transactional(readOnly = true)
    public UploadedFile getStoredUploadedFile(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        if (uploadedFile.getStatus() != UploadedFileStatus.STORED) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        return uploadedFile;
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

        return new ReportFileUploadResponse(
                uploadedFile.getId(),
                savedJob.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStatus().name(),
                savedJob.getStatus().name(),
                savedJob.getMessage()
        );
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

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> listUploadedFiles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return uploadedFileRepository
                .findByUploadedByIdAndStatusInOrderByUploadedAtDesc(
                        user.getId(),
                        List.of(
                                UploadedFileStatus.STORED,
                                UploadedFileStatus.MISSING,
                                UploadedFileStatus.FAILED
                        )
                )
                .stream()
                .map(this::toUploadedFileResponse)
                .toList();
    }

    @Transactional
    public void markUploadedFileAsMissing(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploaded file not found"));

        uploadedFile.markMissing();
    }

    private UploadedFileResponse toUploadedFileResponse(UploadedFile uploadedFile) {
        return new UploadedFileResponse(
                uploadedFile.getId(),
                uploadedFile.getOriginalFilename(),
                uploadedFile.getStoredFilename(),
                uploadedFile.getContentType(),
                uploadedFile.getFileSize(),
                uploadedFile.getChecksum(),
                uploadedFile.getStatus().name(),
                uploadedFile.getUploadedBy().getUsername(),
                uploadedFile.getUploadedAt(),
                uploadedFile.getUpdatedAt()
        );
    }

}
