package dev.lgbonillar.regreporting.processing.processor.demo;

import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingScope;
import dev.lgbonillar.regreporting.processing.domain.ProcessingFindingSeverity;
import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.processor.ProcessingFindingCommand;
import dev.lgbonillar.regreporting.processing.processor.ProcessingResult;
import dev.lgbonillar.regreporting.processing.processor.RegulatoryReportProcessor;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DemoRegulatoryReportProcessor implements RegulatoryReportProcessor {

    public static final String PROCESSOR_CODE = "DEMO_REGULATORY_REPORT";
    private static final String EXPECTED_SHEET_NAME = "Hoja1";

    private static final List<String> EXPECTED_HEADERS = List.of(
            "ID Cliente",
            "Zona",
            "País",
            "Tipo de producto",
            "Canal de venta",
            "Prioridad",
            "Fecha pedido",
            "ID Pedido",
            "Fecha envío",
            "Unidades",
            "Precio Unitario",
            "Coste unitario",
            "Importe venta total",
            "Importe Coste total"
    );

    private final FileStorageService fileStorageService;
    private final DataFormatter dataFormatter = new DataFormatter();

    public DemoRegulatoryReportProcessor(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public String code() {
        return PROCESSOR_CODE;
    }

    @Override
    public ProcessingResult process(ProcessingJob job) {
        Path filePath = fileStorageService.resolvePath(
                job.getUploadedFile().getStoragePath()
        );

        try (InputStream inputStream = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            List<ProcessingFindingCommand> findings = new ArrayList<>();

            validateWorkbook(workbook, findings);

            if (!findings.isEmpty()) {
                return ProcessingResult.withFindings(
                        PROCESSOR_CODE,
                        "Demo regulatory report contains validation findings",
                        findings
                );
            }

            return ProcessingResult.successful(
                    PROCESSOR_CODE,
                    "Demo regulatory report processed successfully"
            );
        } catch (IOException exception) {
            return ProcessingResult.withFindings(
                    PROCESSOR_CODE,
                    "Could not read uploaded Excel file",
                    List.of(systemError("EXCEL_READ_ERROR", exception.getMessage()))
            );
        }
    }

    private void validateWorkbook(Workbook workbook, List<ProcessingFindingCommand> findings) {
        if (workbook.getNumberOfSheets() == 0) {
            findings.add(workbookWithoutSheets());
            return;
        }

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            validateSheet(sheet, findings);
        }

        if (findings.stream().noneMatch(finding -> finding.severity() == ProcessingFindingSeverity.ERROR)) {
            validateDemoSalesLayout(workbook, findings);
        }
    }

    private void validateSheet(Sheet sheet, List<ProcessingFindingCommand> findings) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            findings.add(emptySheet(sheet.getSheetName()));
            return;
        }

        Row headerRow = sheet.getRow(sheet.getFirstRowNum());

        if (headerRow == null || isRowBlank(headerRow)) {
            findings.add(sheetWithoutHeader(sheet.getSheetName()));
            return;
        }

        validateDuplicatedHeaders(sheet.getSheetName(), headerRow, findings);
    }

    private void validateDuplicatedHeaders(
            String sheetName,
            Row headerRow,
            List<ProcessingFindingCommand> findings
    ) {
        Set<String> headers = new HashSet<>();

        for (int columnIndex = headerRow.getFirstCellNum(); columnIndex < headerRow.getLastCellNum(); columnIndex++) {
            String header = getCellText(headerRow, columnIndex);

            if (header.isBlank()) {
                continue;
            }

            String normalizedHeader = header.trim().toLowerCase();

            if (!headers.add(normalizedHeader)) {
                findings.add(duplicatedHeader(sheetName, columnIndex + 1, header));
            }
        }
    }

    private ProcessingFindingCommand workbookWithoutSheets() {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.FILE_STRUCTURE,
                "WORKBOOK_WITHOUT_SHEETS",
                "The workbook must contain at least one sheet",
                null,
                null,
                null,
                null,
                null,
                "At least one sheet",
                "No sheets found"
        );
    }

    private ProcessingFindingCommand emptySheet(String sheetName) {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.SHEET_STRUCTURE,
                "EMPTY_SHEET",
                "The sheet does not contain any rows",
                sheetName,
                null,
                null,
                null,
                null,
                "At least one row with headers",
                "No rows found"
        );
    }

    private ProcessingFindingCommand sheetWithoutHeader(String sheetName) {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.SHEET_STRUCTURE,
                "SHEET_WITHOUT_HEADER",
                "The sheet does not contain a readable header row",
                sheetName,
                1,
                null,
                null,
                null,
                "Header row",
                "Blank or missing header"
        );
    }

    private ProcessingFindingCommand duplicatedHeader(
            String sheetName,
            int columnNumber,
            String header
    ) {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.COLUMN_STRUCTURE,
                "DUPLICATED_HEADER",
                "The sheet contains a duplicated header",
                sheetName,
                1,
                String.valueOf(columnNumber),
                header,
                header,
                "Unique header name",
                header
        );
    }

    private ProcessingFindingCommand systemError(String code, String message) {
        return new ProcessingFindingCommand(
                ProcessingFindingSeverity.ERROR,
                ProcessingFindingScope.SYSTEM,
                code,
                message,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private boolean isRowBlank(Row row) {
        for (int columnIndex = row.getFirstCellNum(); columnIndex < row.getLastCellNum(); columnIndex++) {
            if (!getCellText(row, columnIndex).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String getCellText(Row row, int columnIndex) {
        if (columnIndex < 0 || row == null) {
            return "";
        }

        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        return dataFormatter.formatCellValue(cell).trim();
    }

    private void validateDemoSalesLayout(Workbook workbook, List<ProcessingFindingCommand> findings) {
        Sheet sheet = workbook.getSheet(EXPECTED_SHEET_NAME);

        if (sheet == null) {
            findings.add(new ProcessingFindingCommand(
                    ProcessingFindingSeverity.ERROR,
                    ProcessingFindingScope.SHEET_STRUCTURE,
                    "MISSING_REQUIRED_SHEET",
                    "The workbook does not contain the required demo sales sheet",
                    EXPECTED_SHEET_NAME,
                    null,
                    null,
                    null,
                    null,
                    EXPECTED_SHEET_NAME,
                    "Sheet not found"
            ));
            return;
        }

        Row headerRow = sheet.getRow(sheet.getFirstRowNum());

        validateExpectedHeaders(sheet.getSheetName(), headerRow, findings);
        validateDataRows(sheet, findings);
    }

    private void validateExpectedHeaders(
            String sheetName,
            Row headerRow,
            List<ProcessingFindingCommand> findings
    ) {
        for (int index = 0; index < EXPECTED_HEADERS.size(); index++) {
            String expectedHeader = EXPECTED_HEADERS.get(index);
            String actualHeader = getCellText(headerRow, index);

            if (actualHeader.isBlank()) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.COLUMN_STRUCTURE,
                        "MISSING_REQUIRED_COLUMN",
                        "A required column is missing",
                        sheetName,
                        1,
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
                        "The column does not match the expected demo sales layout",
                        sheetName,
                        1,
                        String.valueOf(index + 1),
                        expectedHeader,
                        actualHeader,
                        expectedHeader,
                        actualHeader
                ));
            }
        }
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

            if (row == null || isRowBlank(row)) {
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
        for (String header : EXPECTED_HEADERS) {
            String value = getCellText(row, columnIndex(header));

            if (value.isBlank()) {
                findings.add(new ProcessingFindingCommand(
                        ProcessingFindingSeverity.ERROR,
                        ProcessingFindingScope.ROW_DATA,
                        "REQUIRED_VALUE_MISSING",
                        "A required value is missing",
                        sheetName,
                        row.getRowNum() + 1,
                        String.valueOf(columnIndex(header) + 1),
                        header,
                        null,
                        "Non-empty value",
                        "Blank value"
                ));
            }
        }
    }

    private int columnIndex(String header) {
        return EXPECTED_HEADERS.indexOf(header);
    }

    private BigDecimal getNumericValue(Row row, String header) {
        Cell cell = row.getCell(columnIndex(header));

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
                    String.valueOf(columnIndex(header) + 1),
                    header,
                    getCellText(row, columnIndex(header)),
                    "Numeric value",
                    getCellText(row, columnIndex(header))
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
                String.valueOf(columnIndex(header) + 1),
                header,
                actual.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                expected.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                actual.setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
    }

    private LocalDate getDateValue(Row row, String header) {
        Cell cell = row.getCell(columnIndex(header));

        if (cell == null || !DateUtil.isCellDateFormatted(cell)) {
            throw new IllegalArgumentException("Cell is not a valid Excel date");
        }

        return cell.getDateCellValue()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
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
                        String.valueOf(columnIndex("Fecha envío") + 1),
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
        String orderId = getCellText(row, columnIndex("ID Pedido"));

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
                    String.valueOf(columnIndex("ID Pedido") + 1),
                    "ID Pedido",
                    orderId,
                    "Unique order id",
                    orderId
            ));
        }
    }
}
