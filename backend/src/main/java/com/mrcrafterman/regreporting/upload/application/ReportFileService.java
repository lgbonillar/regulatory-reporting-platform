package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.shared.ResourceNotFoundException;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.StoredFile;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
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

    public ReportFileService(
            FileStorageService fileStorageService,
            UploadedFileRepository uploadedFileRepository,
            ProcessingJobRepository processingJobRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.processingJobRepository = processingJobRepository;
    }

    @Transactional
    public ReportFileUploadResponse uploadReportFile(MultipartFile file) {
        String username = "analyst01";

        StoredFile storedFile = fileStorageService.store(file, username);
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());

        UploadedFile uploadedFile = uploadedFileRepository
                .findByUploadedByAndOriginalFilename(username, originalFilename)
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
                        username
                ));

        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);

        ProcessingJob processingJob = processingJobRepository
                .findByUploadedFileId(savedFile.getId())
                .map(existingJob -> {
                    existingJob.markPending("File uploaded successfully");
                    return existingJob;
                })
                .orElseGet(() -> new ProcessingJob(
                        savedFile,
                        ProcessingJobStatus.PENDING,
                        "File uploaded successfully"
                ));

        ProcessingJob savedJob = processingJobRepository.save(processingJob);

        return new ReportFileUploadResponse(
                savedFile.getId(),
                savedJob.getId(),
                savedFile.getOriginalFilename(),
                savedFile.getStatus().name(),
                savedJob.getStatus().name(),
                savedJob.getMessage()
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
        String username = "analyst01";

        UploadedFile uploadedFile = uploadedFileRepository
                .findByIdAndUploadedByAndStatusIn(
                        fileId,
                        username,
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
                    existingJob.markPending("File updated successfully");
                    return existingJob;
                })
                .orElseGet(() -> new ProcessingJob(
                        uploadedFile,
                        ProcessingJobStatus.PENDING,
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
        String username = "analyst01";

        UploadedFile uploadedFile = uploadedFileRepository
                .findByIdAndUploadedByAndStatusIn(
                        fileId,
                        username,
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
        return uploadedFileRepository
                .findByUploadedByAndStatusInOrderByUploadedAtDesc(
                        username,
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
                uploadedFile.getUploadedBy(),
                uploadedFile.getUploadedAt(),
                uploadedFile.getUpdatedAt()
        );
    }

}
