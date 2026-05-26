package dev.lgbonillar.regreporting.processing.processor.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class ExcelCellRules {

    public BigDecimal getNumericValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            throw new IllegalArgumentException("Cell is blank");
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> parseBigDecimal(cell.getStringCellValue());
            case FORMULA -> BigDecimal.valueOf(cell.getNumericCellValue());
            default -> throw new IllegalArgumentException("Cell is not numeric");
        };
    }

    public LocalDate getDateValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null || !DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Cell is not a valid Excel date");
        }

        return cell.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value is blank");
        }

        return new BigDecimal(value.trim().replace(",", ""));
    }
}
