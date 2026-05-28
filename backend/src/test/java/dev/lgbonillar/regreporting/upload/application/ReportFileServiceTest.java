package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.dto.ReportFileUploadResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileFindingResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileResponse;
import dev.lgbonillar.regreporting.upload.dto.UploadedFileValidationRunResponse;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportFileServiceTest {

    @Mock
    private UploadedFileCommandService uploadedFileCommandService;

    @Mock
    private UploadedFileQueryService uploadedFileQueryService;

    @Mock
    private UploadedFileValidationRunService validationRunService;

    @Mock
    private UploadedFileFindingService findingService;

    @InjectMocks
    private ReportFileService reportFileService;

    @Test
    void uploadReportFileDelegatesToCommandService() {
        MockMultipartFile file = multipartFile();
        ReportFileUploadResponse expected = uploadResponse();

        when(uploadedFileCommandService.uploadReportFile(file)).thenReturn(expected);

        ReportFileUploadResponse result = reportFileService.uploadReportFile(file);

        assertThat(result).isSameAs(expected);
        verify(uploadedFileCommandService).uploadReportFile(file);
    }

    @Test
    void getStoredUploadedFileDelegatesToQueryService() {
        UUID fileId = UUID.randomUUID();
        UploadedFile expected = uploadedFile();

        when(uploadedFileQueryService.getStoredUploadedFile(fileId)).thenReturn(expected);

        UploadedFile result = reportFileService.getStoredUploadedFile(fileId);

        assertThat(result).isSameAs(expected);
        verify(uploadedFileQueryService).getStoredUploadedFile(fileId);
    }

    @Test
    void updateReportFileDelegatesToCommandService() {
        UUID fileId = UUID.randomUUID();
        MockMultipartFile file = multipartFile();
        ReportFileUploadResponse expected = uploadResponse();

        when(uploadedFileCommandService.updateReportFile(fileId, file)).thenReturn(expected);

        ReportFileUploadResponse result = reportFileService.updateReportFile(fileId, file);

        assertThat(result).isSameAs(expected);
        verify(uploadedFileCommandService).updateReportFile(fileId, file);
    }

    @Test
    void deleteUploadedFileDelegatesToCommandService() {
        UUID fileId = UUID.randomUUID();

        reportFileService.deleteUploadedFile(fileId);

        verify(uploadedFileCommandService).deleteUploadedFile(fileId);
    }

    @Test
    void listUploadedFilesDelegatesToQueryService() {
        List<UploadedFileResponse> expected = List.of();

        when(uploadedFileQueryService.listUploadedFiles("analyst01")).thenReturn(expected);

        List<UploadedFileResponse> result = reportFileService.listUploadedFiles("analyst01");

        assertThat(result).isSameAs(expected);
        verify(uploadedFileQueryService).listUploadedFiles("analyst01");
    }

    @Test
    void markUploadedFileAsMissingDelegatesToCommandService() {
        UUID fileId = UUID.randomUUID();

        reportFileService.markUploadedFileAsMissing(fileId);

        verify(uploadedFileCommandService).markUploadedFileAsMissing(fileId);
    }

    @Test
    void listValidationRunsReturnsMappedValidationRunsForViewableFile() {
        UUID fileId = UUID.randomUUID();
        UploadedFile uploadedFile = uploadedFile();
        UploadedFileValidationRun validationRun = validationRun(uploadedFile);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(uploadedFile);
        when(validationRunService.listValidationRuns(uploadedFile.getId()))
                .thenReturn(List.of(validationRun));

        List<UploadedFileValidationRunResponse> result =
                reportFileService.listValidationRuns(fileId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("FAILED");
        assertThat(result.get(0).source()).isEqualTo("UPLOAD");
        verify(uploadedFileQueryService).getViewableUploadedFile(fileId);
        verify(validationRunService).listValidationRuns(uploadedFile.getId());
    }

    @Test
    void listFindingsReturnsMappedFindingsForViewableFile() {
        UUID fileId = UUID.randomUUID();
        UploadedFile uploadedFile = uploadedFile();
        UploadedFileValidationRun validationRun = validationRun(uploadedFile);
        UploadedFileFinding finding = finding(validationRun, uploadedFile);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(uploadedFile);
        when(findingService.listFindings(uploadedFile.getId()))
                .thenReturn(List.of(finding));

        List<UploadedFileFindingResponse> result = reportFileService.listFindings(fileId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).severity()).isEqualTo("ERROR");
        assertThat(result.get(0).code()).isEqualTo("INVALID_STRUCTURE");
        verify(uploadedFileQueryService).getViewableUploadedFile(fileId);
        verify(findingService).listFindings(uploadedFile.getId());
    }

    @Test
    void listFindingsByValidationRunChecksFileAndRunBeforeReturningFindings() {
        UUID fileId = UUID.randomUUID();
        UUID validationRunId = UUID.randomUUID();
        UploadedFile uploadedFile = uploadedFile();
        UploadedFileValidationRun validationRun = validationRun(uploadedFile);
        UploadedFileFinding finding = finding(validationRun, uploadedFile);

        when(uploadedFileQueryService.getViewableUploadedFile(fileId)).thenReturn(uploadedFile);
        when(validationRunService.getValidationRun(uploadedFile.getId(), validationRunId))
                .thenReturn(validationRun);
        when(findingService.listFindingsByValidationRun(validationRun.getId()))
                .thenReturn(List.of(finding));

        List<UploadedFileFindingResponse> result =
                reportFileService.listFindingsByValidationRun(fileId, validationRunId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("INVALID_STRUCTURE");
        verify(validationRunService).getValidationRun(uploadedFile.getId(), validationRunId);
        verify(findingService).listFindingsByValidationRun(validationRun.getId());
    }

    private MockMultipartFile multipartFile() {
        return new MockMultipartFile(
                "file",
                "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "content".getBytes()
        );
    }

    private ReportFileUploadResponse uploadResponse() {
        return new ReportFileUploadResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "report.xlsx",
                "STORED",
                "PENDING_EXECUTION",
                "File uploaded"
        );
    }

    private UploadedFile uploadedFile() {
        UploadedFile uploadedFile = new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
        );
        uploadedFile.setId(UUID.randomUUID());
        return uploadedFile;
    }

    private UploadedFileValidationRun validationRun(UploadedFile uploadedFile) {
        return new UploadedFileValidationRun(
                uploadedFile,
                UploadedFileValidationRunStatus.FAILED,
                UploadedFileValidationRunSource.UPLOAD,
                "Uploaded file validation found issues",
                "analyst01"
        );
    }

    private UploadedFileFinding finding(
            UploadedFileValidationRun validationRun,
            UploadedFile uploadedFile
    ) {
        return new UploadedFileFinding(
                validationRun,
                uploadedFile,
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.FILE_STRUCTURE,
                "INVALID_STRUCTURE",
                "Invalid file structure",
                "Hoja1",
                null,
                null,
                null,
                null,
                "Expected structure",
                "Invalid structure"
        );
    }

    private User analyst() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

}
