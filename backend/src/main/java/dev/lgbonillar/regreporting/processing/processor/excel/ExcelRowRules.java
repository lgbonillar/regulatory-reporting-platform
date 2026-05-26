package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ExcelRowRules {

    private final ExcelCellReader cellReader;

    public ExcelRowRules(ExcelCellReader cellReader) {
        this.cellReader = cellReader;
    }

    public void validateRequiredValues(
            String sheetName,
            Row row,
            List<String> headers,
            List<ProcessingFindingCommand> findings
    ) {
        for (String header : headers) {
            int columnIndex = headers.indexOf(header);
            String value = cellReader.getCellText(row, columnIndex);

            if (value.isBlank()) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.ROW_DATA,
                        "REQUIRED_VALUE_MISSING",
                        "A required value is missing",
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(columnIndex + 1),
                        header,
                        null,
                        "Non-empty value",
                        "Blank value"
                ));
            }
        }
    }

    public void validateUniqueValue(
            String sheetName,
            Row row,
            String header,
            int columnIndex,
            Set<String> seenValues,
            List<ProcessingFindingCommand> findings
    ) {
        String value = cellReader.getCellText(row, columnIndex);

        if (value.isBlank()) {
            return;
        }

        if (!seenValues.add(value)) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.BUSINESS_RULE,
                    "DUPLICATED_VALUE",
                    "The value must be unique within the sheet",
                    sheetName,
                    row.getRowNum() + 1,
                    String.valueOf(columnIndex + 1),
                    header,
                    value,
                    "Unique value",
                    value
            ));
        }
    }
}
