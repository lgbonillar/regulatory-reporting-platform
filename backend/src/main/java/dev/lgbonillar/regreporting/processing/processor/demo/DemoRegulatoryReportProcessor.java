package dev.lgbonillar.regreporting.processing.processor.demo;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.processing.processor.ProcessingResult;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessor;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoRegulatoryReportProcessor implements RegulatoryReportProcessor {

    public static final String PROCESSOR_CODE = "DEMO_REGULATORY_REPORT";

    private final FileStorageService fileStorageService;

    public DemoRegulatoryReportProcessor(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public String code() {
        return PROCESSOR_CODE;
    }

    @Override
    public ProcessingResult process(ProcessingJob job) {
        Path filePath = fileStorageService.resolvePath(
                job.getUploadedFile().getStoragePath()
        );

        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            List<ProcessingFindingCommand> findings = new ArrayList<>();

            validateWorkbook(workbook, findings);

            if (!findings.isEmpty()) {
                return ProcessingResult.withFindings(
                        PROCESSOR_CODE,
                        "Demo regulatory report contains validation findings",
                        findings
                );
            }

            return ProcessingResult.successful(
                    PROCESSOR_CODE,
                    "Demo regulatory report processed successfully"
            );
        } catch (IOException exception) {
            return ProcessingResult.withFindings(
                    PROCESSOR_CODE,
                    "Could not read uploaded Excel file",
                    List.of(systemError("EXCEL_READ_ERROR", exception.getMessage()))
            );
        }
    }

    private void validateWorkbook(Workbook workbook, List<ProcessingFindingCommand> findings) {
        if (workbook.getNumberOfSheets() == 0) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.FILE_STRUCTURE,
                    "WORKBOOK_WITHOUT_SHEETS",
                    "The workbook must contain at least one sheet",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "At least one sheet",
                    "No sheets found"
            ));
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
