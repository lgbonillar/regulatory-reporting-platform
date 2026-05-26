package dev.lgbonillar.regreporting.processing.processor.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

@Component
public class ExcelCellReader {

    private final DataFormatter dataFormatter = new DataFormatter();

    public String getCellText(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            return "";
        }

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        return dataFormatter.formatCellValue(cell).trim();
    }

    public boolean isRowBlank(Row row) {
        if (row == null || row.getFirstCellNum() < 0 || row.getLastCellNum() < 0) {
            return true;
        }

        for (int columnIndex = row.getFirstCellNum(); columnIndex < row.getLastCellNum(); columnIndex++) {
            if (!getCellText(row, columnIndex).isBlank()) {
                return false;
            }
        }

        return true;
    }
}
