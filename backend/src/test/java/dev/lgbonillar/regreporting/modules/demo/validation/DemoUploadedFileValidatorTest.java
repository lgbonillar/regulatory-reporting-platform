package dev.lgbonillar.regreporting.modules.demo.validation;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidationResult;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoUploadedFileValidatorTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DemoSalesWorkbookValidator workbookValidator;

    @InjectMocks
    private DemoUploadedFileValidator validator;

    @TempDir
    private Path tempDirectory;

    @Test
    void codeReturnsDemoValidatorCode() {
        assertThat(validator.code()).isEqualTo(DemoUploadedFileValidator.VALIDATOR_CODE);
    }

    @Test
    void validateReturnsWorkbookValidatorFindings() throws IOException {
        UploadedFile uploadedFile = uploadedFile();
        Path workbookPath = writeWorkbook();
        ProcessingFindingCommand finding = finding();

        when(fileStorageService.resolvePath(uploadedFile.getStoragePath()))
                .thenReturn(workbookPath);
        when(workbookValidator.validate(any(Workbook.class)))
                .thenReturn(List.of(finding));

        UploadedFileValidationResult result = validator.validate(uploadedFile);

        assertThat(result.findings()).containsExactly(finding);
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void validateReturnsReadErrorWhenWorkbookCannotBeOpened() {
        UploadedFile uploadedFile = uploadedFile();
        Path missingWorkbookPath = tempDirectory.resolve("missing.xlsx");

        when(fileStorageService.resolvePath(uploadedFile.getStoragePath()))
                .thenReturn(missingWorkbookPath);

        UploadedFileValidationResult result = validator.validate(uploadedFile);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.severity()).isEqualTo(ProcessingFindingSeverity.ERROR);
                    assertThat(finding.scope()).isEqualTo(ProcessingFindingScope.SYSTEM);
                    assertThat(finding.code()).isEqualTo("EXCEL_READ_ERROR");
                    assertThat(finding.message()).isEqualTo("Could not read uploaded Excel file");
                });
        verifyNoInteractions(workbookValidator);
    }

    @Test
    void validateReturnsValidationErrorWhenWorkbookValidatorFails() throws IOException {
        UploadedFile uploadedFile = uploadedFile();
        Path workbookPath = writeWorkbook();

        when(fileStorageService.resolvePath(uploadedFile.getStoragePath()))
                .thenReturn(workbookPath);
        when(workbookValidator.validate(any(Workbook.class)))
                .thenThrow(new IllegalStateException("Unexpected validation failure"));

        UploadedFileValidationResult result = validator.validate(uploadedFile);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .singleElement()
                .satisfies(finding -> {
                    assertThat(finding.severity()).isEqualTo(ProcessingFindingSeverity.ERROR);
                    assertThat(finding.scope()).isEqualTo(ProcessingFindingScope.SYSTEM);
                    assertThat(finding.code()).isEqualTo("EXCEL_VALIDATION_ERROR");
                    assertThat(finding.message()).isEqualTo("Could not validate uploaded Excel file");
                });
    }

    private Path writeWorkbook() throws IOException {
        Path workbookPath = Files.createTempFile(tempDirectory, "uploaded-file-", ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Hoja1");

            try (var outputStream = Files.newOutputStream(workbookPath)) {
                workbook.write(outputStream);
            }
        }

        return workbookPath;
    }

    private ProcessingFindingCommand finding() {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.FILE_STRUCTURE,
                "INVALID_STRUCTURE",
                "Invalid workbook structure",
                "Hoja1",
                null,
                null,
                null,
                null,
                "Expected structure",
                "Invalid structure"
        );
    }

    private UploadedFile uploadedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "analyst01/stored-report.xlsx",
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
