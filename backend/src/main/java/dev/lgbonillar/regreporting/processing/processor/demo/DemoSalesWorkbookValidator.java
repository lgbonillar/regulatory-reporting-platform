package dev.lgbonillar.regreporting.processing.processor.demo;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.processing.processor.excel.ExcelCellReader;
import dev.lgbonillar.regreporting.processing.processor.excel.ExcelHeaderRules;
import dev.lgbonillar.regreporting.processing.processor.excel.ExcelSheetRules;
import dev.lgbonillar.regreporting.processing.processor.excel.ExcelWorkbookRules;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DemoSalesWorkbookValidator {

    private final ExcelWorkbookRules workbookRules;
    private final ExcelSheetRules sheetRules;
    private final ExcelHeaderRules headerRules;
    private final ExcelCellReader cellReader;

    public DemoSalesWorkbookValidator(
            ExcelWorkbookRules workbookRules,
            ExcelSheetRules sheetRules,
            ExcelHeaderRules headerRules,
            ExcelCellReader cellReader
    ) {
        this.workbookRules = workbookRules;
        this.sheetRules = sheetRules;
        this.headerRules = headerRules;
        this.cellReader = cellReader;
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

            validateRequiredRowValues(sheet.getSheetName(), row, findings);
            validateNumericValues(sheet.getSheetName(), row, findings);
            validateBusinessCalculations(sheet.getSheetName(), row, findings);
            validateDateRange(sheet.getSheetName(), row, findings);
            validateDuplicatedOrderId(sheet.getSheetName(), row, seenOrderIds, findings);
        }
    }

    private void validateRequiredRowValues(
            String sheetName,
            Row row,
            List<ProcessingFindingCommand> findings
    ) {
        for (String header : DemoSalesReportLayout.EXPECTED_HEADERS) {
            String value = cellReader.getCellText(row, DemoSalesReportLayout.columnIndex(header));

            if (value.isBlank()) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.ROW_DATA,
                        "REQUIRED_VALUE_MISSING",
                        "A required value is missing",
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(DemoSalesReportLayout.columnIndex(header) + 1),
                        header,
                        null,
                        "Non-empty value",
                        "Blank value"
                ));
            }
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
        try {
            BigDecimal units = getNumericValue(row, "Unidades");
            BigDecimal unitPrice = getNumericValue(row, "Precio Unitario");
            BigDecimal unitCost = getNumericValue(row, "Coste unitario");
            BigDecimal totalSaleAmount = getNumericValue(row, "Importe venta total");
            BigDecimal totalCostAmount = getNumericValue(row, "Importe Coste total");

            BigDecimal expectedSaleAmount = units.multiply(unitPrice);
            BigDecimal expectedCostAmount = units.multiply(unitCost);

            if (!sameAmount(expectedSaleAmount, totalSaleAmount)) {
                findings.add(calculationMismatch(
                        sheetName,
                        row,
                        "Importe venta total",
                        expectedSaleAmount,
                        totalSaleAmount
                ));
            }

            if (!sameAmount(expectedCostAmount, totalCostAmount)) {
                findings.add(calculationMismatch(
                        sheetName,
                        row,
                        "Importe Coste total",
                        expectedCostAmount,
                        totalCostAmount
                ));
            }
        } catch (IllegalArgumentException exception) {
            // Numeric validation already reports the specific invalid cells.
        }
    }

    private void validateDateRange(
            String sheetName,
            Row row,
            List<ProcessingFindingCommand> findings
    ) {
        try {
            LocalDate orderDate = getDateValue(row, "Fecha pedido");
            LocalDate shippingDate = getDateValue(row, "Fecha envío");

            if (shippingDate.isBefore(orderDate)) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.BUSINESS_RULE,
                        "INVALID_SHIPPING_DATE_RANGE",
                        "The shipping date cannot be before the order date",
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(DemoSalesReportLayout.columnIndex("Fecha envío") + 1),
                        "Fecha envío",
                        shippingDate.toString(),
                        "Shipping date greater than or equal to order date",
                        orderDate.toString()
                ));
            }
        } catch (IllegalArgumentException exception) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.ROW_DATA,
                    "INVALID_DATE_VALUE",
                    "The order date and shipping date must be valid Excel dates",
                    sheetName,
                    row.getRowNum() + 1,
                    null,
                    "Fecha pedido / Fecha envío",
                    null,
                    "Valid Excel date",
                    "Invalid or blank date"
            ));
        }
    }

    private void validateDuplicatedOrderId(
            String sheetName,
            Row row,
            Set<String> seenOrderIds,
            List<ProcessingFindingCommand> findings
    ) {
        String orderId = cellReader.getCellText(row, DemoSalesReportLayout.columnIndex("ID Pedido"));

        if (orderId.isBlank()) {
            return;
        }

        if (!seenOrderIds.add(orderId)) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.BUSINESS_RULE,
                    "DUPLICATED_ORDER_ID",
                    "The order id must be unique within the sales report",
                    sheetName,
                    row.getRowNum() + 1,
                    String.valueOf(DemoSalesReportLayout.columnIndex("ID Pedido") + 1),
                    "ID Pedido",
                    orderId,
                    "Unique order id",
                    orderId
            ));
        }
    }

    private ProcessingFindingCommand calculationMismatch(
            String sheetName,
            Row row,
            String header,
            BigDecimal expected,
            BigDecimal actual
    ) {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.BUSINESS_RULE,
                "AMOUNT_CALCULATION_MISMATCH",
                "The calculated amount does not match the reported amount",
                sheetName,
                row.getRowNum() + 1,
                String.valueOf(DemoSalesReportLayout.columnIndex(header) + 1),
                header,
                actual.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                expected.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                actual.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
    }

    private boolean hasErrors(List<ProcessingFindingCommand> findings) {
        return findings.stream()
                .anyMatch(finding -> finding.severity() == ProcessingFindingSeverity.ERROR);
    }

    private BigDecimal getNumericValue(Row row, String header) {
        Cell cell = row.getCell(DemoSalesReportLayout.columnIndex(header));

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

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value is blank");
        }

        return new BigDecimal(value.trim().replace(",", ""));
    }

    private boolean sameAmount(BigDecimal expected, BigDecimal actual) {
        return expected.setScale(2, RoundingMode.HALF_UP)
                .compareTo(actual.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    private LocalDate getDateValue(Row row, String header) {
        Cell cell = row.getCell(DemoSalesReportLayout.columnIndex(header));

        if (!DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Cell is not a valid Excel date");
        }

        return cell.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
