package dev.lgbonillar.regreporting.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Validation finding detected in an uploaded file.")
public record UploadedFileFindingResponse(
        @Schema(description = "Finding identifier.")
        UUID findingId,

        @Schema(description = "Validation run identifier.")
        UUID validationRunId,

        @Schema(description = "Uploaded file identifier.")
        UUID fileId,

        @Schema(description = "Finding severity.", example = "ERROR")
        String severity,

        @Schema(description = "Finding scope.", example = "ROW_DATA")
        String scope,

        @Schema(description = "Machine-readable finding code.", example = "INVALID_NUMERIC_VALUE")
        String code,

        @Schema(description = "Human-readable finding message.")
        String message,

        @Schema(description = "Excel sheet name where the finding was detected.")
        String sheetName,

        @Schema(description = "One-based Excel row number.")
        Integer rowNumber,

        @Schema(description = "Excel column name.")
        String columnName,

        @Schema(description = "Business field name.")
        String fieldName,

        @Schema(description = "Rejected value read from the file.")
        String rejectedValue,

        @Schema(description = "Expected value or rule description.")
        String expectedValue,

        @Schema(description = "Actual value calculated by the system.")
        String actualValue,

        @Schema(description = "Timestamp when the finding was stored.")
        LocalDateTime createdAt
) {
}
