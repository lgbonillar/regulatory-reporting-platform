package dev.lgbonillar.regreporting.modules.demo.validation;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidationResult;
import dev.lgbonillar.regreporting.upload.validation.UploadedFileValidator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class DemoUploadedFileValidator implements UploadedFileValidator {

    public static final String VALIDATOR_CODE = "DEMO_UPLOADED_FILE";

    private final FileStorageService fileStorageService;
    private final DemoSalesWorkbookValidator workbookValidator;

    public DemoUploadedFileValidator(
            FileStorageService fileStorageService,
            DemoSalesWorkbookValidator workbookValidator
    ) {
        this.fileStorageService = fileStorageService;
        this.workbookValidator = workbookValidator;
    }

    @Override
    public String code() {
        return VALIDATOR_CODE;
    }

    @Override
    public UploadedFileValidationResult validate(UploadedFile uploadedFile) {
        Path filePath = fileStorageService.resolvePath(uploadedFile.getStoragePath());

        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            return UploadedFileValidationResult.withFindings(
                    workbookValidator.validate(workbook)
            );
        } catch (IOException exception) {
            return UploadedFileValidationResult.withFindings(List.of(systemError(
                    "EXCEL_READ_ERROR",
                    "Could not read uploaded Excel file"
            )));
        } catch (RuntimeException exception) {
            return UploadedFileValidationResult.withFindings(List.of(systemError(
                    "EXCEL_VALIDATION_ERROR",
                    "Could not validate uploaded Excel file"
            )));
        }
    }

    private ProcessingFindingCommand systemError(String code, String message) {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.SYSTEM,
                code,
                message,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
