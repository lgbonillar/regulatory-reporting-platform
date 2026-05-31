package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.application.support.UploadedFileAccessRules;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.dto.DownloadedFile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DownloadFileService {

    private final UploadedFileQueryService uploadedFileQueryService;
    private final FileStorageService fileStorageService;
    private final MarkFileMissingService markFileMissingService;

    public DownloadFileService(
            UploadedFileQueryService uploadedFileQueryService,
            FileStorageService fileStorageService,
            MarkFileMissingService markFileMissingService
    ) {
        this.uploadedFileQueryService = uploadedFileQueryService;
        this.fileStorageService = fileStorageService;
        this.markFileMissingService = markFileMissingService;
    }

    @Transactional
    public DownloadedFile downloadFile(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        if (!UploadedFileAccessRules.isDownloadableStatus(uploadedFile.getStatus())) {
            throw new ResourceNotFoundException("Uploaded file not found");
        }

        Resource resource;
        try {
            resource = fileStorageService.loadAsResource(uploadedFile.getStoragePath());
        } catch (ResourceNotFoundException exception) {
            markFileMissingService.markFileAsMissing(fileId);
            throw exception;
        }

        return new DownloadedFile(uploadedFile, resource);
    }
}