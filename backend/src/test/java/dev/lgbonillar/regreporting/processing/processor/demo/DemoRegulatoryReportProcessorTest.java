package dev.lgbonillar.regreporting.processing.processor.demo;

import dev.lgbonillar.regreporting.processing.domain.ProcessingJob;
import dev.lgbonillar.regreporting.processing.processor.ProcessingResult;
import dev.lgbonillar.regreporting.upload.application.FileStorageService;
import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DemoRegulatoryReportProcessorTest {

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

    @TempDir
    private Path tempDirectory;

    @Test
    void processReturnsSuccessfulResultForValidSalesWorkbook() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            CellStyle dateStyle = dateStyle(workbook);
            Row header = workbook.createSheet("Hoja1").createRow(0);
            writeHeaders(header);
            writeValidSalesRow(header.getSheet().createRow(1), dateStyle, "100001");
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.findings()).isEmpty();
        assertThat(result.message()).isEqualTo("Demo regulatory report processed successfully");
    }

    @Test
    void processReturnsMissingRequiredSheetFinding() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            Row header = workbook.createSheet("OtherSheet").createRow(0);
            writeHeaders(header);
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .extracting("code")
                .contains("MISSING_REQUIRED_SHEET");
    }

    @Test
    void processReturnsInvalidColumnOrderFinding() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            CellStyle dateStyle = dateStyle(workbook);
            Row header = workbook.createSheet("Hoja1").createRow(0);
            writeHeaders(header);
            header.getCell(0).setCellValue("Cliente");
            writeValidSalesRow(header.getSheet().createRow(1), dateStyle, "100001");
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .extracting("code")
                .contains("INVALID_COLUMN_ORDER");
    }

    @Test
    void processReturnsInvalidNumericValueFinding() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            CellStyle dateStyle = dateStyle(workbook);
            Row header = workbook.createSheet("Hoja1").createRow(0);
            writeHeaders(header);
            Row dataRow = header.getSheet().createRow(1);
            writeValidSalesRow(dataRow, dateStyle, "100001");
            dataRow.getCell(columnIndex("Unidades")).setCellValue("N/A");
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .extracting("code")
                .contains("INVALID_NUMERIC_VALUE");
    }

    @Test
    void processReturnsInvalidShippingDateRangeFinding() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            CellStyle dateStyle = dateStyle(workbook);
            Row header = workbook.createSheet("Hoja1").createRow(0);
            writeHeaders(header);
            Row dataRow = header.getSheet().createRow(1);
            writeValidSalesRow(dataRow, dateStyle, "100001");
            writeDate(dataRow, columnIndex("Fecha envío"), LocalDate.of(2024, 1, 14), dateStyle);
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .extracting("code")
                .contains("INVALID_SHIPPING_DATE_RANGE");
    }

    @Test
    void processReturnsDuplicatedOrderIdFinding() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            CellStyle dateStyle = dateStyle(workbook);
            Row header = workbook.createSheet("Hoja1").createRow(0);
            writeHeaders(header);
            writeValidSalesRow(header.getSheet().createRow(1), dateStyle, "100001");
            writeValidSalesRow(header.getSheet().createRow(2), dateStyle, "100001");
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .extracting("code")
                .contains("DUPLICATED_ORDER_ID");
    }

    @Test
    void processReturnsAmountCalculationMismatchFinding() throws IOException {
        Path workbookPath = writeWorkbook(workbook -> {
            CellStyle dateStyle = dateStyle(workbook);
            Row header = workbook.createSheet("Hoja1").createRow(0);
            writeHeaders(header);
            Row dataRow = header.getSheet().createRow(1);
            writeValidSalesRow(dataRow, dateStyle, "100001");
            dataRow.getCell(columnIndex("Importe venta total")).setCellValue(999.99);
        });
        DemoRegulatoryReportProcessor processor = processor(workbookPath);

        ProcessingResult result = processor.process(processingJob());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.findings())
                .extracting("code")
                .contains("AMOUNT_CALCULATION_MISMATCH");
    }

    private DemoRegulatoryReportProcessor processor(Path workbookPath) {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        when(fileStorageService.resolvePath("analyst01/stored-report.xlsx")).thenReturn(workbookPath);

        return new DemoRegulatoryReportProcessor(fileStorageService);
    }

    private ProcessingJob processingJob() {
        return new ProcessingJob(uploadedFile(), "File uploaded");
    }

    private UploadedFile uploadedFile() {
        return new UploadedFile(
                "report.xlsx",
                "stored-report.xlsx",
                "analyst01/stored-report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                1024L,
                "checksum",
                UploadedFileStatus.STORED,
                analyst()
        );
    }

    private User analyst() {
        return new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                null,
                false,
                UserStatus.ACTIVE
        );
    }

    private Path writeWorkbook(WorkbookCustomizer customizer) throws IOException {
        Path workbookPath = Files.createTempFile(tempDirectory, "sales-report-", ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            customizer.customize(workbook);

            try (var outputStream = Files.newOutputStream(workbookPath)) {
                workbook.write(outputStream);
            }
        }

        return workbookPath;
    }

    private void writeHeaders(Row headerRow) {
        for (int index = 0; index < EXPECTED_HEADERS.size(); index++) {
            headerRow.createCell(index).setCellValue(EXPECTED_HEADERS.get(index));
        }
    }

    private void writeValidSalesRow(Row row, CellStyle dateStyle, String orderId) {
        row.createCell(columnIndex("ID Cliente")).setCellValue("C1000");
        row.createCell(columnIndex("Zona")).setCellValue("Europa");
        row.createCell(columnIndex("País")).setCellValue("Mexico");
        row.createCell(columnIndex("Tipo de producto")).setCellValue("Snacks");
        row.createCell(columnIndex("Canal de venta")).setCellValue("Online");
        row.createCell(columnIndex("Prioridad")).setCellValue("Alta");
        writeDate(row, columnIndex("Fecha pedido"), LocalDate.of(2024, 1, 15), dateStyle);
        row.createCell(columnIndex("ID Pedido")).setCellValue(orderId);
        writeDate(row, columnIndex("Fecha envío"), LocalDate.of(2024, 1, 20), dateStyle);
        row.createCell(columnIndex("Unidades")).setCellValue(10);
        row.createCell(columnIndex("Precio Unitario")).setCellValue(12.50);
        row.createCell(columnIndex("Coste unitario")).setCellValue(7.25);
        row.createCell(columnIndex("Importe venta total")).setCellValue(new BigDecimal("125.00").doubleValue());
        row.createCell(columnIndex("Importe Coste total")).setCellValue(new BigDecimal("72.50").doubleValue());
    }

    private void writeDate(Row row, int columnIndex, LocalDate value, CellStyle dateStyle) {
        Date date = Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
        row.createCell(columnIndex).setCellValue(date);
        row.getCell(columnIndex).setCellStyle(dateStyle);
    }

    private CellStyle dateStyle(Workbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private int columnIndex(String header) {
        return EXPECTED_HEADERS.indexOf(header);
    }

    @FunctionalInterface
    private interface WorkbookCustomizer {
        void customize(Workbook workbook);
    }
}
