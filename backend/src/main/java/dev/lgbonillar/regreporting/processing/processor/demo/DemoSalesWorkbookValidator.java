package dev.lgbonillar.regreporting.processing.processor.demo;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.processing.processor.excel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DemoSalesWorkbookValidator {

    private final ExcelRowRules rowRules;
    private final ExcelWorkbookRules workbookRules;
    private final ExcelSheetRules sheetRules;
    private final ExcelHeaderRules headerRules;
    private final ExcelCellReader cellReader;
    private final ExcelCellRules cellRules;
    private final ExcelBusinessRules businessRules;

    public DemoSalesWorkbookValidator(
            ExcelRowRules rowRules,
            ExcelWorkbookRules workbookRules,
            ExcelSheetRules sheetRules,
            ExcelHeaderRules headerRules,
            ExcelCellReader cellReader,
            ExcelCellRules cellRules,
            ExcelBusinessRules businessRules
    ) {
        this.rowRules = rowRules;
        this.workbookRules = workbookRules;
        this.sheetRules = sheetRules;
        this.headerRules = headerRules;
        this.cellReader = cellReader;
        this.cellRules = cellRules;
        this.businessRules = businessRules;
    }

    public List<ProcessingFindingCommand> validate(Workbook workbook) {
        List<ProcessingFindingCommand> findings = new ArrayList<>();

        workbookRules.validateHasSheets(workbook, findings);

        if (hasErrors(findings)) {
            return findings;
        }

        workbookRules.validateRequiredSheet(
                workbook,
                DemoSalesReportLayout.EXPECTED_SHEET_NAME,
                findings
        );

        if (hasErrors(findings)) {
            return findings;
        }

        Sheet sheet = workbook.getSheet(DemoSalesReportLayout.EXPECTED_SHEET_NAME);

        sheetRules.validateNotEmpty(sheet, findings);
        sheetRules.validateHasHeader(sheet, DemoSalesReportLayout.HEADER_ROW_INDEX, findings);
        sheetRules.validateHasDataRows(sheet, DemoSalesReportLayout.HEADER_ROW_INDEX, findings);

        if (hasErrors(findings)) {
            return findings;
        }

        headerRules.validateNoDuplicatedHeaders(sheet, DemoSalesReportLayout.HEADER_ROW_INDEX, findings);
        headerRules.validateExactHeaders(
                sheet,
                DemoSalesReportLayout.HEADER_ROW_INDEX,
                DemoSalesReportLayout.EXPECTED_HEADERS,
                findings
        );

        if (hasErrors(findings)) {
            return findings;
        }

        validateDataRows(sheet, findings);

        return findings;
    }

    private void validateDataRows(Sheet sheet, List<ProcessingFindingCommand> findings) {
        int firstDataRowIndex = sheet.getFirstRowNum() + 1;
        int lastRowIndex = sheet.getLastRowNum();
        Set<String> seenOrderIds = new HashSet<>();

        if (lastRowIndex < firstDataRowIndex) {
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
            return;
        }

        for (int rowIndex = firstDataRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row == null || cellReader.isRowBlank(row)) {
                continue;
            }

            rowRules.validateRequiredValues(
                    sheet.getSheetName(),
                    row,
                    DemoSalesReportLayout.EXPECTED_HEADERS,
                    findings
            );

            validateNumericValues(sheet.getSheetName(), row, findings);
            validateBusinessCalculations(sheet.getSheetName(), row, findings);
            validateDateRange(sheet.getSheetName(), row, findings);
            validateDuplicatedOrderId(sheet.getSheetName(), row, seenOrderIds, findings);
        }
    }

    private void validateNumericValues(
            String sheetName,
            Row row,
            List<ProcessingFindingCommand> findings
    ) {
        validateNumericValue(sheetName, row, "Unidades", findings);
        validateNumericValue(sheetName, row, "Precio Unitario", findings);
        validateNumericValue(sheetName, row, "Coste unitario", findings);
        validateNumericValue(sheetName, row, "Importe venta total", findings);
        validateNumericValue(sheetName, row, "Importe Coste total", findings);
    }

    private void validateNumericValue(
            String sheetName,
            Row row,
            String header,
            List<ProcessingFindingCommand> findings
    ) {
        try {
            getNumericValue(row, header);
        } catch (IllegalArgumentException exception) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.ROW_DATA,
                    "INVALID_NUMERIC_VALUE",
                    "The value must be numeric",
                    sheetName,
                    row.getRowNum() + 1,
                    String.valueOf(DemoSalesReportLayout.columnIndex(header) + 1),
                    header,
                    cellReader.getCellText(row, DemoSalesReportLayout.columnIndex(header)),
                    "Numeric value",
                    cellReader.getCellText(row, DemoSalesReportLayout.columnIndex(header))
            ));
        }
    }

    private void validateBusinessCalculations(
            String sheetName,
            Row row,
            List<ProcessingFindingCommand> findings
    ) {
        businessRules.validateMultiplication(
                sheetName,
                row,
                "Unidades",
                DemoSalesReportLayout.columnIndex("Unidades"),
                "Precio Unitario",
                DemoSalesReportLayout.columnIndex("Precio Unitario"),
                "Importe venta total",
                DemoSalesReportLayout.columnIndex("Importe venta total"),
                "AMOUNT_CALCULATION_MISMATCH",
                "The calculated amount does not match the reported amount",
                findings
        );

        businessRules.validateMultiplication(
                sheetName,
                row,
                "Unidades",
                DemoSalesReportLayout.columnIndex("Unidades"),
                "Coste unitario",
                DemoSalesReportLayout.columnIndex("Coste unitario"),
                "Importe Coste total",
                DemoSalesReportLayout.columnIndex("Importe Coste total"),
                "AMOUNT_CALCULATION_MISMATCH",
                "The calculated amount does not match the reported amount",
                findings
        );
    }

    private void validateDateRange(
            String sheetName,
            Row row,
            List<ProcessingFindingCommand> findings
    ) {
        businessRules.validateDateGreaterThanOrEqual(
                sheetName,
                row,
                "Fecha pedido",
                DemoSalesReportLayout.columnIndex("Fecha pedido"),
                "Fecha envío",
                DemoSalesReportLayout.columnIndex("Fecha envío"),
                "INVALID_SHIPPING_DATE_RANGE",
                "The shipping date cannot be before the order date",
                findings
        );
    }

    private void validateDuplicatedOrderId(
            String sheetName,
            Row row,
            Set<String> seenOrderIds,
            List<ProcessingFindingCommand> findings
    ) {
        businessRules.validateUniqueValue(
                sheetName,
                row,
                "ID Pedido",
                DemoSalesReportLayout.columnIndex("ID Pedido"),
                seenOrderIds,
                "DUPLICATED_ORDER_ID",
                "The order id must be unique within the sales report",
                findings
        );
    }

    private boolean hasErrors(List<ProcessingFindingCommand> findings) {
        return findings.stream()
                .anyMatch(finding -> finding.severity() == ProcessingFindingSeverity.ERROR);
    }

    private BigDecimal getNumericValue(Row row, String header) {
        return cellRules.getNumericValue(
                row,
                DemoSalesReportLayout.columnIndex(header)
        );
    }

}
