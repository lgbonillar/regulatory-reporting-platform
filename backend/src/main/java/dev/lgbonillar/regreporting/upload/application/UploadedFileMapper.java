package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
import org.springframework.stereotype.Component;

@Component
public class UploadedFileMapper {

    public UploadedFileResponse toUploadedFileResponse(UploadedFile uploadedFile) {
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

    public UploadedFileValidationRunResponse toValidationRunResponse(
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

    public UploadedFileFindingResponse toFindingResponse(UploadedFileFinding finding) {
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