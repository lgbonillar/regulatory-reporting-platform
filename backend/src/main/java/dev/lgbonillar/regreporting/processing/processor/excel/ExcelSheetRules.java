package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExcelSheetRules {

    private final ExcelCellReader cellReader;

    public ExcelSheetRules(ExcelCellReader cellReader) {
        this.cellReader = cellReader;
    }

    public void validateNotEmpty(
            Sheet sheet,
            List<ProcessingFindingCommand> findings
    ) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.SHEET_STRUCTURE,
                    "EMPTY_SHEET",
                    "The sheet does not contain any rows",
                    sheet.getSheetName(),
                    null,
                    null,
                    null,
                    null,
                    "At least one row with headers",
                    "No rows found"
            ));
        }
    }

    public void validateHasHeader(
            Sheet sheet,
            int headerRowIndex,
            List<ProcessingFindingCommand> findings
    ) {
        Row headerRow = sheet.getRow(headerRowIndex);

        if (headerRow == null || cellReader.isRowBlank(headerRow)) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.SHEET_STRUCTURE,
                    "SHEET_WITHOUT_HEADER",
                    "The sheet does not contain a readable header row",
                    sheet.getSheetName(),
                    headerRowIndex + 1,
                    null,
                    null,
                    null,
                    "Header row",
                    "Blank or missing header"
            ));
        }
    }

    public void validateHasDataRows(
            Sheet sheet,
            int headerRowIndex,
            List<ProcessingFindingCommand> findings
    ) {
        int firstDataRowIndex = headerRowIndex + 1;

        if (sheet.getLastRowNum() < firstDataRowIndex) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.ROW_DATA,
                    "SHEET_WITHOUT_DATA_ROWS",
                    "The sheet contains headers but no data rows",
                    sheet.getSheetName(),
                    firstDataRowIndex + 1,
                    null,
                    null,
                    null,
                    "At least one data row",
                    "No data rows found"
            ));
        }
    }
}
