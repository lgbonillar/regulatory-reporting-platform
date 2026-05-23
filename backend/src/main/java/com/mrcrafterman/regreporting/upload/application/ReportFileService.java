package com.mrcrafterman.regreporting.upload.application;

import com.mrcrafterman.regreporting.upload.domain.UploadedFile;
import com.mrcrafterman.regreporting.upload.dto.ReportFileUploadResponse;
import com.mrcrafterman.regreporting.upload.dto.UploadedFileResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ReportFileService {

    private final UploadedFileCommandService uploadedFileCommandService;
    private final UploadedFileQueryService uploadedFileQueryService;

    public ReportFileService(
            UploadedFileCommandService uploadedFileCommandService,
            UploadedFileQueryService uploadedFileQueryService
    ) {
        this.uploadedFileCommandService = uploadedFileCommandService;
        this.uploadedFileQueryService = uploadedFileQueryService;
    }

    public ReportFileUploadResponse uploadReportFile(MultipartFile file) {
        return uploadedFileCommandService.uploadReportFile(file);
    }

    public UploadedFile getStoredUploadedFile(UUID fileId) {
        return uploadedFileQueryService.getStoredUploadedFile(fileId);
    }

    public ReportFileUploadResponse updateReportFile(UUID fileId, MultipartFile file) {
        return uploadedFileCommandService.updateReportFile(fileId, file);
    }

    public void deleteUploadedFile(UUID fileId) {
        uploadedFileCommandService.deleteUploadedFile(fileId);
    }

    public List<UploadedFileResponse> listUploadedFiles(String username) {
        return uploadedFileQueryService.listUploadedFiles(username);
    }

    public void markUploadedFileAsMissing(UUID fileId) {
        uploadedFileCommandService.markUploadedFileAsMissing(fileId);
    }

}
