package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ReportFileService {

    private final UploadedFileCommandService uploadedFileCommandService;
    private final UploadedFileQueryService uploadedFileQueryService;
    private final UploadedFileValidationRunService validationRunService;
    private final UploadedFileFindingService findingService;

    public ReportFileService(
            UploadedFileCommandService uploadedFileCommandService,
            UploadedFileQueryService uploadedFileQueryService,
            UploadedFileValidationRunService validationRunService,
            UploadedFileFindingService findingService
    ) {
        this.uploadedFileCommandService = uploadedFileCommandService;
        this.uploadedFileQueryService = uploadedFileQueryService;
        this.validationRunService = validationRunService;
        this.findingService = findingService;
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

    public List<UploadedFileValidationRunResponse> listValidationRuns(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        return validationRunService.listValidationRuns(uploadedFile.getId())
                .stream()
                .map(this::toValidationRunResponse)
                .toList();
    }

    public List<UploadedFileFindingResponse> listFindings(UUID fileId) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);

        return findingService.listFindings(uploadedFile.getId())
                .stream()
                .map(this::toFindingResponse)
                .toList();
    }

    public List<UploadedFileFindingResponse> listFindingsByValidationRun(
            UUID fileId,
            UUID validationRunId
    ) {
        UploadedFile uploadedFile = uploadedFileQueryService.getViewableUploadedFile(fileId);
        UploadedFileValidationRun validationRun = validationRunService.getValidationRun(
                uploadedFile.getId(),
                validationRunId
        );

        return findingService.listFindingsByValidationRun(validationRun.getId())
                .stream()
                .map(this::toFindingResponse)
                .toList();
    }

    private UploadedFileValidationRunResponse toValidationRunResponse(
            UploadedFileValidationRun validationRun
    ) {
        return new UploadedFileValidationRunResponse(
                validationRun.getId(),
                validationRun.getUploadedFile().getId(),
                validationRun.getStatus().name(),
                validationRun.getSource().name(),
                validationRun.getSummaryMessage(),
                validationRun.getCreatedBy(),
                validationRun.getCreatedAt()
        );
    }

    private UploadedFileFindingResponse toFindingResponse(UploadedFileFinding finding) {
        return new UploadedFileFindingResponse(
                finding.getId(),
                finding.getValidationRun().getId(),
                finding.getUploadedFile().getId(),
                finding.getSeverity().name(),
                finding.getScope().name(),
                finding.getCode(),
                finding.getMessage(),
                finding.getSheetName(),
                finding.getRowNumber(),
                finding.getColumnName(),
                finding.getFieldName(),
                finding.getRejectedValue(),
                finding.getExpectedValue(),
                finding.getActualValue(),
                finding.getCreatedAt()
        );
    }

}
