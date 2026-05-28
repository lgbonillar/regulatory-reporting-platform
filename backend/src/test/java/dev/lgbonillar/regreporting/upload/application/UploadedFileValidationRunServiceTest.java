package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.shared.ResourceNotFoundException;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRun;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunSource;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileValidationRunStatus;
import dev.lgbonillar.regreporting.upload.infrastructure.UploadedFileValidationRunRepository;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadedFileValidationRunServiceTest {

    @Mock
    private UploadedFileValidationRunRepository validationRunRepository;

    @InjectMocks
    private UploadedFileValidationRunService validationRunService;

    @Test
    void createValidationRunPersistsRun() {
        UploadedFile uploadedFile = uploadedFile();

        when(validationRunRepository.save(org.mockito.ArgumentMatchers.any(UploadedFileValidationRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UploadedFileValidationRun result = validationRunService.createValidationRun(
                uploadedFile,
                UploadedFileValidationRunStatus.FAILED,
                UploadedFileValidationRunSource.UPLOAD,
                "Validation failed",
                "analyst01"
        );

        ArgumentCaptor<UploadedFileValidationRun> validationRunCaptor =
                ArgumentCaptor.forClass(UploadedFileValidationRun.class);

        verify(validationRunRepository).save(validationRunCaptor.capture());

        UploadedFileValidationRun savedValidationRun = validationRunCaptor.getValue();

        assertThat(result).isSameAs(savedValidationRun);
        assertThat(savedValidationRun.getUploadedFile()).isSameAs(uploadedFile);
        assertThat(savedValidationRun.getStatus()).isEqualTo(UploadedFileValidationRunStatus.FAILED);
        assertThat(savedValidationRun.getSource()).isEqualTo(UploadedFileValidationRunSource.UPLOAD);
        assertThat(savedValidationRun.getSummaryMessage()).isEqualTo("Validation failed");
        assertThat(savedValidationRun.getCreatedBy()).isEqualTo("analyst01");
        assertThat(savedValidationRun.getCreatedAt()).isNotNull();
    }

    @Test
    void listValidationRunsReturnsRepositoryResults() {
        UUID uploadedFileId = UUID.randomUUID();
        List<UploadedFileValidationRun> validationRuns = List.of(
                new UploadedFileValidationRun(
                        uploadedFile(),
                        UploadedFileValidationRunStatus.PASSED,
                        UploadedFileValidationRunSource.REPLACEMENT,
                        "Validation passed",
                        "analyst01"
                )
        );

        when(validationRunRepository.findAllByUploadedFile_IdOrderByCreatedAtDesc(uploadedFileId))
                .thenReturn(validationRuns);

        List<UploadedFileValidationRun> result = validationRunService.listValidationRuns(uploadedFileId);

        assertThat(result).isSameAs(validationRuns);
    }

    @Test
    void getValidationRunReturnsRunForUploadedFile() {
        UUID uploadedFileId = UUID.randomUUID();
        UUID validationRunId = UUID.randomUUID();
        UploadedFileValidationRun validationRun = new UploadedFileValidationRun(
                uploadedFile(),
                UploadedFileValidationRunStatus.PASSED,
                UploadedFileValidationRunSource.REPLACEMENT,
                "Validation passed",
                "analyst01"
        );

        when(validationRunRepository.findByIdAndUploadedFile_Id(validationRunId, uploadedFileId))
                .thenReturn(Optional.of(validationRun));

        UploadedFileValidationRun result = validationRunService.getValidationRun(
                uploadedFileId,
                validationRunId
        );

        assertThat(result).isSameAs(validationRun);
    }

    @Test
    void getValidationRunThrowsResourceNotFoundWhenRunDoesNotBelongToFile() {
        UUID uploadedFileId = UUID.randomUUID();
        UUID validationRunId = UUID.randomUUID();

        when(validationRunRepository.findByIdAndUploadedFile_Id(validationRunId, uploadedFileId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> validationRunService.getValidationRun(
                uploadedFileId,
                validationRunId
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Uploaded file validation run not found");
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
