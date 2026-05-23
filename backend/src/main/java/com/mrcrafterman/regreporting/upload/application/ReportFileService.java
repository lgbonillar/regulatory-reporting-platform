package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatusHistory;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobTransitionSource;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.StoredFile;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobStatusHistoryRepository;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
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

    private final FileStorageService fileStorageService;
    private final UploadedFileRepository uploadedFileRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final UserRepository userRepository;
    private final ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository;

    public ReportFileService(
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobRepository processingJobRepository,
            UserRepository userRepository,
            ProcessingJobStatusHistoryRepository processingJobStatusHistoryRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobRepository = processingJobRepository;
        this.userRepository = userRepository;
        this.processingJobStatusHistoryRepository = processingJobStatusHistoryRepository;
    }

    @Transactional
    public ReportFileUploadResponse uploadReportFile(MultipartFile file) {
        User currentUser = getCurrentUser();
        String username = currentUser.getUsername();

        StoredFile storedFile = fileStorageService.store(file, username);
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());

        UploadedFile uploadedFile = uploadedFileRepository
                .findByUploadedByIdAndOriginalFilename(currentUser.getId(), originalFilename)
                .map(existingFile -> {
                    existingFile.replaceWith(
                            storedFile.storedFilename(),
                            storedFile.relativeStoragePath(),
                            file.getContentType(),
                            file.getSize(),
                            storedFile.checksum()
                    );

                    return existingFile;
                })
                .orElseGet(() -> new UploadedFile(
                        originalFilename,
                        storedFile.storedFilename(),
                        storedFile.relativeStoragePath(),
                        file.getContentType(),
                        file.getSize(),
                        storedFile.checksum(),
                        UploadedFileStatus.STORED,
                        currentUser
                ));

        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);

        ProcessingJob processingJob = processingJobRepository
                .findByUploadedFileId(savedFile.getId())
                .orElse(null);

        if (processingJob == null) {
            processingJob = processingJobRepository.save(
                    new ProcessingJob(savedFile, "File uploaded successfully")
            );

            processingJobStatusHistoryRepository.save(
                    new ProcessingJobStatusHistory(
                            processingJob,
                            null,
                            ProcessingJobStatus.PENDING_EXECUTION,
                            ProcessingJobTransitionSource.USER,
                            currentUser,
                            "File uploaded and queued for execution"
                    )
            );
        } else {
            processingJob.markPendingExecution("File uploaded successfully");
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
        User currentUser = getCurrentUser();
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
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found"));

        fileStorageService.delete(uploadedFile.getStoragePath());

        StoredFile storedFile = fileStorageService.store(file, username);

        uploadedFile.replaceWith(
                storedFile.storedFilename(),
                storedFile.relativeStoragePath(),
                file.getContentType(),
                file.getSize(),
                storedFile.checksum()
        );

        ProcessingJob processingJob = processingJobRepository
                .findByUploadedFileId(uploadedFile.getId())
                .map(existingJob -> {
                    existingJob.markPendingExecution("File updated successfully");
                    return existingJob;
                })
                .orElseGet(() -> new ProcessingJob(
                        uploadedFile,
                        "File updated successfully"
                ));

        ProcessingJob savedJob = processingJobRepository.save(processingJob);

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
        User currentUser = getCurrentUser();

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

    private User getCurrentUser() {
        return userRepository.findByUsername("analyst01")
                .orElseThrow(() -> new IllegalStateException("Current development user not found"));
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
