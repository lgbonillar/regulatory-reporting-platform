package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExcelWorkbookRules {

    public void validateHasSheets(
            Workbook workbook,
            List<ProcessingFindingCommand> findings
    ) {
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

    public void validateRequiredSheet(
            Workbook workbook,
            String expectedSheetName,
            List<ProcessingFindingCommand> findings
    ) {
        if (workbook.getSheet(expectedSheetName) == null) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.SHEET_STRUCTURE,
                    "MISSING_REQUIRED_SHEET",
                    "The workbook does not contain a required sheet",
                    expectedSheetName,
                    null,
                    null,
                    null,
                    null,
                    expectedSheetName,
                    "Sheet not found"
            ));
        }
    }

    public void validateRequiredSheets(
            Workbook workbook,
            List<String> expectedSheetNames,
            List<ProcessingFindingCommand> findings
    ) {
        for (String expectedSheetName : expectedSheetNames) {
            validateRequiredSheet(workbook, expectedSheetName, findings);
        }
    }
}
