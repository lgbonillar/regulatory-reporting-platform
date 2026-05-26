package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelBusinessRulesTest {

    private final ExcelCellReader cellReader = new ExcelCellReader();
    private final ExcelCellRules cellRules = new ExcelCellRules();
    private final ExcelBusinessRules rules = new ExcelBusinessRules(cellReader, cellRules);

    @Test
    void validateDateGreaterThanOrEqualDoesNotAddFindingForValidRange() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            CellStyle dateStyle = dateStyle(workbook);
            writeDate(row, 0, LocalDate.of(2024, 1, 15), dateStyle);
            writeDate(row, 1, LocalDate.of(2024, 1, 20), dateStyle);
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateDateGreaterThanOrEqual(
                    "Sheet1",
                    row,
                    "Fecha pedido",
                    0,
                    "Fecha envío",
                    1,
                    "INVALID_SHIPPING_DATE_RANGE",
                    "The shipping date cannot be before the order date",
                    findings
            );

            assertThat(findings).isEmpty();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateDateGreaterThanOrEqualAddsFindingForInvalidRange() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            CellStyle dateStyle = dateStyle(workbook);
            writeDate(row, 0, LocalDate.of(2024, 1, 20), dateStyle);
            writeDate(row, 1, LocalDate.of(2024, 1, 15), dateStyle);
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateDateGreaterThanOrEqual(
                    "Sheet1",
                    row,
                    "Fecha pedido",
                    0,
                    "Fecha envío",
                    1,
                    "INVALID_SHIPPING_DATE_RANGE",
                    "The shipping date cannot be before the order date",
                    findings
            );

            assertThat(findings)
                    .extracting(ProcessingFindingCommand::code)
                    .containsExactly("INVALID_SHIPPING_DATE_RANGE");
            assertThat(findings.getFirst().scope().name()).isEqualTo("BUSINESS_RULE");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateMultiplicationDoesNotAddFindingWhenResultMatches() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            row.createCell(0).setCellValue(10);
            row.createCell(1).setCellValue(12.50);
            row.createCell(2).setCellValue(125.00);
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateMultiplication(
                    "Sheet1",
                    row,
                    "Unidades",
                    0,
                    "Precio Unitario",
                    1,
                    "Importe venta total",
                    2,
                    "AMOUNT_CALCULATION_MISMATCH",
                    "The calculated amount does not match the reported amount",
                    findings
            );

            assertThat(findings).isEmpty();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateMultiplicationAddsFindingWhenResultDoesNotMatch() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row row = workbook.createSheet("Sheet1").createRow(0);
            row.createCell(0).setCellValue(10);
            row.createCell(1).setCellValue(12.50);
            row.createCell(2).setCellValue(999.99);
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateMultiplication(
                    "Sheet1",
                    row,
                    "Unidades",
                    0,
                    "Precio Unitario",
                    1,
                    "Importe venta total",
                    2,
                    "AMOUNT_CALCULATION_MISMATCH",
                    "The calculated amount does not match the reported amount",
                    findings
            );

            assertThat(findings)
                    .extracting(ProcessingFindingCommand::code)
                    .containsExactly("AMOUNT_CALCULATION_MISMATCH");
            assertThat(findings.getFirst().expectedValue()).isEqualTo("125.00");
            assertThat(findings.getFirst().actualValue()).isEqualTo("999.99");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateUniqueValueAddsFindingForDuplicatedValue() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Row firstRow = workbook.createSheet("Sheet1").createRow(0);
            firstRow.createCell(0).setCellValue("100001");
            Row secondRow = firstRow.getSheet().createRow(1);
            secondRow.createCell(0).setCellValue("100001");
            List<ProcessingFindingCommand> findings = new ArrayList<>();
            HashSet<String> seenValues = new HashSet<>();

            rules.validateUniqueValue(
                    "Sheet1",
                    firstRow,
                    "ID Pedido",
                    0,
                    seenValues,
                    "DUPLICATED_ORDER_ID",
                    "The order id must be unique within the sales report",
                    findings
            );
            rules.validateUniqueValue(
                    "Sheet1",
                    secondRow,
                    "ID Pedido",
                    0,
                    seenValues,
                    "DUPLICATED_ORDER_ID",
                    "The order id must be unique within the sales report",
                    findings
            );

            assertThat(findings)
                    .extracting(ProcessingFindingCommand::code)
                    .containsExactly("DUPLICATED_ORDER_ID");
            assertThat(findings.getFirst().rejectedValue()).isEqualTo("100001");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private CellStyle dateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private void writeDate(Row row, int columnIndex, LocalDate value, CellStyle dateStyle) {
        Date date = Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
        row.createCell(columnIndex).setCellValue(date);
        row.getCell(columnIndex).setCellStyle(dateStyle);
    }
}
