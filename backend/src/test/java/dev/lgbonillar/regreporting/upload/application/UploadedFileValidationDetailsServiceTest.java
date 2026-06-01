package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileValidationDetailsServiceTest {

    @Mock
    private UploadedFileQueryService uploadedFileQueryService;

    @Mock
    private UploadedFileValidationRunService validationRunService;

    @Mock
    private UploadedFileFindingService findingService;

    @Mock
    private UploadedFileMapper uploadedFileMapper;

    @InjectMocks
    private UploadedFileValidationDetailsService service;

    @Test
    void listValidationRunsReturnsValidationRunResponses() {
        UUID fileId = UUID.randomUUID();
        User owner = analystUser();
        UploadedFile file = storedFile(fileId, owner);
        UploadedFileValidationRun run = validationRun(file);
        UploadedFileValidationRunResponse response = validationRunResponse(run);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);
        when(validationRunService.listValidationRuns(any())).thenReturn(List.of(run));
        when(uploadedFileMapper.toValidationRunResponse(run)).thenReturn(response);

        List<UploadedFileValidationRunResponse> result = service.listValidationRuns(fileId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    void listValidationRunsThrowsResourceNotFoundWhenFileNotFound() {
        UUID fileId = UUID.randomUUID();

        when(uploadedFileQueryService.getViewableUploadedFile(fileId))
                .thenThrow(new ResourceNotFoundException("Uploaded file not found"));

        assertThatThrownBy(() -> service.listValidationRuns(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listFindingsReturnsFindingResponses() {
        UUID fileId = UUID.randomUUID();
        User owner = analystUser();
        UploadedFile file = storedFile(fileId, owner);
        UploadedFileFinding finding = createFinding();
        UploadedFileFindingResponse response = findingResponse(finding);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);
        when(findingService.listFindings(any())).thenReturn(List.of(finding));
        when(uploadedFileMapper.toFindingResponse(finding)).thenReturn(response);

        List<UploadedFileFindingResponse> result = service.listFindings(fileId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    void listFindingsThrowsResourceNotFoundWhenFileNotFound() {
        UUID fileId = UUID.randomUUID();

        when(uploadedFileQueryService.getViewableUploadedFile(fileId))
                .thenThrow(new ResourceNotFoundException("Uploaded file not found"));

        assertThatThrownBy(() -> service.listFindings(fileId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listValidationRunFindingsReturnsFindingsForSpecificRun() {
        UUID fileId = UUID.randomUUID();
        UUID validationRunId = UUID.randomUUID();
        User owner = analystUser();
        UploadedFile file = storedFile(fileId, owner);
        UploadedFileValidationRun run = validationRun(file);
        UploadedFileFinding finding = createFinding();
        UploadedFileFindingResponse response = findingResponse(finding);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(file);
        when(validationRunService.getValidationRun(any(), any())).thenReturn(run);
        when(findingService.listFindingsByValidationRun(any())).thenReturn(List.of(finding));
        when(uploadedFileMapper.toFindingResponse(finding)).thenReturn(response);

        List<UploadedFileFindingResponse> result = service.listValidationRunFindings(fileId, validationRunId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    void listValidationRunFindingsThrowsResourceNotFoundWhenFileNotFound() {
        UUID fileId = UUID.randomUUID();
        UUID validationRunId = UUID.randomUUID();

        when(uploadedFileQueryService.getViewableUploadedFile(fileId))
                .thenThrow(new ResourceNotFoundException("Uploaded file not found"));

        assertThatThrownBy(() -> service.listValidationRunFindings(fileId, validationRunId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User analystUser() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

    private UploadedFile storedFile(UUID fileId, User owner) {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/analyst01/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                owner
        );
    }

    private UploadedFileValidationRun validationRun(UploadedFile file) {
        return new UploadedFileValidationRun(
                file,
                UploadedFileValidationRunStatus.PASSED,
                UploadedFileValidationRunSource.UPLOAD,
                "Validation passed",
                "analyst01"
        );
    }

    private UploadedFileFinding createFinding() {
        return new UploadedFileFinding(
                null,
                null,
                ProcessingFindingSeverity.WARNING,
                ProcessingFindingScope.SHEET_STRUCTURE,
                "SHEET_NOT_FOUND",
                "Sheet not found",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private UploadedFileValidationRunResponse validationRunResponse(UploadedFileValidationRun run) {
        return new UploadedFileValidationRunResponse(
                run.getId(),
                run.getUploadedFile().getId(),
                run.getStatus().name(),
                run.getSource().name(),
                run.getSummaryMessage(),
                run.getCreatedBy(),
                run.getCreatedAt()
        );
    }

    private UploadedFileFindingResponse findingResponse(UploadedFileFinding finding) {
        return new UploadedFileFindingResponse(
                finding.getId(),
                null,
                null,
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
                null
        );
    }
}