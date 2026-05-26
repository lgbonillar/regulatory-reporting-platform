package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class ExcelBusinessRules {

    private final ExcelCellReader cellReader;
    private final ExcelCellRules cellRules;

    public ExcelBusinessRules(
            ExcelCellReader cellReader,
            ExcelCellRules cellRules
    ) {
        this.cellReader = cellReader;
        this.cellRules = cellRules;
    }

    public void validateDateGreaterThanOrEqual(
            String sheetName,
            Row row,
            String startHeader,
            int startColumnIndex,
            String endHeader,
            int endColumnIndex,
            String code,
            String message,
            List<ProcessingFindingCommand> findings
    ) {
        try {
            LocalDate startDate = cellRules.getDateValue(row, startColumnIndex);
            LocalDate endDate = cellRules.getDateValue(row, endColumnIndex);

            if (endDate.isBefore(startDate)) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.BUSINESS_RULE,
                        code,
                        message,
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(endColumnIndex + 1),
                        endHeader,
                        endDate.toString(),
                        endHeader + " greater than or equal to " + startHeader,
                        startDate.toString()
                ));
            }
        } catch (IllegalArgumentException exception) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.ROW_DATA,
                    "INVALID_DATE_VALUE",
                    "The date values must be valid Excel dates",
                    sheetName,
                    row.getRowNum() + 1,
                    null,
                    startHeader + " / " + endHeader,
                    null,
                    "Valid Excel date",
                    "Invalid or blank date"
            ));
        }
    }

    public void validateMultiplication(
            String sheetName,
            Row row,
            String leftHeader,
            int leftColumnIndex,
            String rightHeader,
            int rightColumnIndex,
            String resultHeader,
            int resultColumnIndex,
            String code,
            String message,
            List<ProcessingFindingCommand> findings
    ) {
        try {
            BigDecimal left = cellRules.getNumericValue(row, leftColumnIndex);
            BigDecimal right = cellRules.getNumericValue(row, rightColumnIndex);
            BigDecimal actual = cellRules.getNumericValue(row, resultColumnIndex);
            BigDecimal expected = left.multiply(right);

            if (!sameAmount(expected, actual)) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.BUSINESS_RULE,
                        code,
                        message,
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(resultColumnIndex + 1),
                        resultHeader,
                        actual.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        expected.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        actual.setScale(2, RoundingMode.HALF_UP).toPlainString()
                ));
            }
        } catch (IllegalArgumentException exception) {
            // Numeric validation should report invalid cells separately.
        }
    }

    public void validateUniqueValue(
            String sheetName,
            Row row,
            String header,
            int columnIndex,
            Set<String> seenValues,
            String code,
            String message,
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
                    code,
                    message,
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

    private boolean sameAmount(BigDecimal expected, BigDecimal actual) {
        return expected.setScale(2, RoundingMode.HALF_UP)
                .compareTo(actual.setScale(2, RoundingMode.HALF_UP)) == 0;
    }
}
