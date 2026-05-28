package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileFinding;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileFindingRepository;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileFindingServiceTest {

    @Mock
    private UploadedFileFindingRepository findingRepository;

    @InjectMocks
    private UploadedFileFindingService findingService;

    @Test
    void saveFindingsPersistsMappedFindings() {
        UploadedFile uploadedFile = uploadedFile();
        UploadedFileValidationRun validationRun = validationRun(uploadedFile);
        ProcessingFindingCommand findingCommand = new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.ROW_DATA,
                "INVALID_AMOUNT",
                "Amount must be positive",
                "Sales",
                10,
                "Amount",
                "amount",
                "-1",
                "positive number",
                "-1"
        );

        when(findingRepository.saveAll(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<UploadedFileFinding> result = findingService.saveFindings(
                validationRun,
                uploadedFile,
                List.of(findingCommand)
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UploadedFileFinding>> findingsCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(findingRepository).saveAll(findingsCaptor.capture());

        List<UploadedFileFinding> savedFindings = findingsCaptor.getValue();

        assertThat(result).isSameAs(savedFindings);
        assertThat(savedFindings).hasSize(1);

        UploadedFileFinding savedFinding = savedFindings.getFirst();

        assertThat(savedFinding.getValidationRun()).isSameAs(validationRun);
        assertThat(savedFinding.getUploadedFile()).isSameAs(uploadedFile);
        assertThat(savedFinding.getSeverity()).isEqualTo(ProcessingFindingSeverity.ERROR);
        assertThat(savedFinding.getScope()).isEqualTo(ProcessingFindingScope.ROW_DATA);
        assertThat(savedFinding.getCode()).isEqualTo("INVALID_AMOUNT");
        assertThat(savedFinding.getMessage()).isEqualTo("Amount must be positive");
        assertThat(savedFinding.getSheetName()).isEqualTo("Sales");
        assertThat(savedFinding.getRowNumber()).isEqualTo(10);
        assertThat(savedFinding.getColumnName()).isEqualTo("Amount");
        assertThat(savedFinding.getFieldName()).isEqualTo("amount");
        assertThat(savedFinding.getRejectedValue()).isEqualTo("-1");
        assertThat(savedFinding.getExpectedValue()).isEqualTo("positive number");
        assertThat(savedFinding.getActualValue()).isEqualTo("-1");
        assertThat(savedFinding.getCreatedAt()).isNotNull();
    }

    @Test
    void listFindingsReturnsRepositoryResults() {
        UUID uploadedFileId = UUID.randomUUID();
        List<UploadedFileFinding> findings = List.of(uploadedFileFinding());

        when(findingRepository.findAllByUploadedFile_IdOrderByCreatedAtAsc(uploadedFileId))
                .thenReturn(findings);

        List<UploadedFileFinding> result = findingService.listFindings(uploadedFileId);

        assertThat(result).isSameAs(findings);
    }

    @Test
    void listFindingsByValidationRunReturnsRepositoryResults() {
        UUID validationRunId = UUID.randomUUID();
        List<UploadedFileFinding> findings = List.of(uploadedFileFinding());

        when(findingRepository.findAllByValidationRun_IdOrderByCreatedAtAsc(validationRunId))
                .thenReturn(findings);

        List<UploadedFileFinding> result = findingService.listFindingsByValidationRun(validationRunId);

        assertThat(result).isSameAs(findings);
    }

    private UploadedFileFinding uploadedFileFinding() {
        UploadedFile uploadedFile = uploadedFile();

        return new UploadedFileFinding(
                validationRun(uploadedFile),
                uploadedFile,
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.FILE_STRUCTURE,
                "INVALID_FILE",
                "Invalid file",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private UploadedFileValidationRun validationRun(UploadedFile uploadedFile) {
        return new UploadedFileValidationRun(
                uploadedFile,
                UploadedFileValidationRunStatus.FAILED,
                UploadedFileValidationRunSource.UPLOAD,
                "Validation failed",
                "analyst01"
        );
    }

    private UploadedFile uploadedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "/uploads/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
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
