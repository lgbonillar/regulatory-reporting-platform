package dev.lgbonillar.regreporting.processing.processor.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelCellRulesTest {

    private final ExcelCellRules rules = new ExcelCellRules();

    @Test
    void getNumericValueReturnsValueFromNumericCell() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            row.createCell(0).setCellValue(125.50);

            BigDecimal result = rules.getNumericValue(row, 0);

            assertThat(result).isEqualByComparingTo("125.50");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void getNumericValueReturnsValueFromStringCell() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            row.createCell(0).setCellValue("1,250.75");

            BigDecimal result = rules.getNumericValue(row, 0);

            assertThat(result).isEqualByComparingTo("1250.75");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void getNumericValueFailsForBlankCell() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);

            assertThatThrownBy(() -> rules.getNumericValue(row, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cell is blank");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void getDateValueReturnsLocalDateFromDateCell() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
            LocalDate expectedDate = LocalDate.of(2024, 1, 15);
            Date date = Date.from(expectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            row.createCell(0).setCellValue(date);
            row.getCell(0).setCellStyle(dateStyle);

            LocalDate result = rules.getDateValue(row, 0);

            assertThat(result).isEqualTo(expectedDate);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void getDateValueFailsForTextCell() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            row.createCell(0).setCellValue("not-a-date");

            assertThatThrownBy(() -> rules.getDateValue(row, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cell is not a valid Excel date");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
