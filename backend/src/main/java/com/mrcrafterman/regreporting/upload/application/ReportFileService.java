package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.upload.domain.ProcessingJob;
import com.mrcrafterman.regreporting.upload.domain.ProcessingJobStatus;
import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.domain.UploadedFileStatus;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.StoredFile;
import com.mrcrafterman.regreporting.upload.infrastructure.ProcessingJobRepository;
import com.mrcrafterman.regreporting.upload.infrastructure.UploadedFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
                savedJob.getStatus().name(),
                savedJob.getMessage()
        );
    }

    @Transactional(readOnly = true)
    public UploadedFile getUploadedFile(UUID fileId) {
        return uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found"));
    }


}
