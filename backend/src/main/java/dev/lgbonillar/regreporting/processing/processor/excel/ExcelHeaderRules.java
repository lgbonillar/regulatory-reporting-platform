package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ExcelHeaderRules {

    private final ExcelCellReader cellReader;

    public ExcelHeaderRules(ExcelCellReader cellReader) {
        this.cellReader = cellReader;
    }

    public void validateNoDuplicatedHeaders(
            Sheet sheet,
            int headerRowIndex,
            List<ProcessingFindingCommand> findings
    ) {
        Row headerRow = sheet.getRow(headerRowIndex);
        Set<String> headers = new HashSet<>();

        if (headerRow == null) {
            return;
        }

        for (int columnIndex = headerRow.getFirstCellNum(); columnIndex < headerRow.getLastCellNum(); columnIndex++) {
            String header = cellReader.getCellText(headerRow, columnIndex);

            if (header.isBlank()) {
                continue;
            }

            String normalizedHeader = header.trim().toLowerCase();

            if (!headers.add(normalizedHeader)) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.COLUMN_STRUCTURE,
                        "DUPLICATED_HEADER",
                        "The sheet contains a duplicated header",
                        sheet.getSheetName(),
                        headerRowIndex + 1,
                        String.valueOf(columnIndex + 1),
                        header,
                        header,
                        "Unique header name",
                        header
                ));
            }
        }
    }

    public void validateExactHeaders(
            Sheet sheet,
            int headerRowIndex,
            List<String> expectedHeaders,
            List<ProcessingFindingCommand> findings
    ) {
        Row headerRow = sheet.getRow(headerRowIndex);

        for (int index = 0; index < expectedHeaders.size(); index++) {
            String expectedHeader = expectedHeaders.get(index);
            String actualHeader = cellReader.getCellText(headerRow, index);

            if (actualHeader.isBlank()) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.COLUMN_STRUCTURE,
                        "MISSING_REQUIRED_COLUMN",
                        "A required column is missing",
                        sheet.getSheetName(),
                        headerRowIndex + 1,
                        String.valueOf(index + 1),
                        expectedHeader,
                        null,
                        expectedHeader,
                        "Blank column"
                ));
                continue;
            }

            if (!actualHeader.equals(expectedHeader)) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.COLUMN_STRUCTURE,
                        "INVALID_COLUMN_ORDER",
                        "The column does not match the expected layout",
                        sheet.getSheetName(),
                        headerRowIndex + 1,
                        String.valueOf(index + 1),
                        expectedHeader,
                        actualHeader,
                        expectedHeader,
                        actualHeader
                ));
            }
        }
    }
}
