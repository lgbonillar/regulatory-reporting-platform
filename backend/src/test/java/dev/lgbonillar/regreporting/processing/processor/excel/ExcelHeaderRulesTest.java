package dev.lgbonillar.regreporting.processing.processor.excel;

import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelHeaderRulesTest {

    private final ExcelHeaderRules rules = new ExcelHeaderRules(new ExcelCellReader());

    @Test
    void validateNoDuplicatedHeadersAddsFindingForDuplicatedHeader() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID Pedido");
            header.createCell(1).setCellValue("id pedido");
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateNoDuplicatedHeaders(sheet, 0, findings);

            assertThat(findings)
                    .extracting(ProcessingFindingCommand::code)
                    .containsExactly("DUPLICATED_HEADER");
            assertThat(findings.getFirst().scope().name()).isEqualTo("COLUMN_STRUCTURE");
            assertThat(findings.getFirst().fieldName()).isEqualTo("id pedido");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateExactHeadersAddsMissingRequiredColumnFinding() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID Cliente");
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateExactHeaders(
                    sheet,
                    0,
                    List.of("ID Cliente", "Zona"),
                    findings
            );

            assertThat(findings)
                    .extracting(ProcessingFindingCommand::code)
                    .containsExactly("MISSING_REQUIRED_COLUMN");
            assertThat(findings.getFirst().fieldName()).isEqualTo("Zona");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateExactHeadersAddsInvalidColumnOrderFinding() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Zona");
            header.createCell(1).setCellValue("ID Cliente");
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateExactHeaders(
                    sheet,
                    0,
                    List.of("ID Cliente", "Zona"),
                    findings
            );

            assertThat(findings)
                    .extracting(ProcessingFindingCommand::code)
                    .containsExactly("INVALID_COLUMN_ORDER", "INVALID_COLUMN_ORDER");
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void validateExactHeadersDoesNotAddFindingsForExpectedHeaders() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID Cliente");
            header.createCell(1).setCellValue("Zona");
            List<ProcessingFindingCommand> findings = new ArrayList<>();

            rules.validateExactHeaders(
                    sheet,
                    0,
                    List.of("ID Cliente", "Zona"),
                    findings
            );

            assertThat(findings).isEmpty();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
