package dev.lgbonillar.regreporting.processing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Validation or processing finding detected during regulatory report processing.")
public record ProcessingJobFindingResponse(

        @Schema(description = "Finding identifier.")
        UUID id,

        @Schema(description = "Finding severity.", example = "ERROR")
        String severity,

        @Schema(description = "Finding scope.", example = "ROW_DATA")
        String scope,

        @Schema(description = "Stable finding code.", example = "INVALID_DATA_TYPE")
        String code,

        @Schema(description = "Human-readable finding message.")
        String message,

        @Schema(description = "Excel sheet name related to this finding.")
        String sheetName,

        @Schema(description = "Excel row number related to this finding.")
        Integer rowNumber,

        @Schema(description = "Excel column name related to this finding.")
        String columnName,

        @Schema(description = "Logical business field related to this finding.")
        String fieldName,

        @Schema(description = "Rejected value found during validation.")
        String rejectedValue,

        @Schema(description = "Expected value or format.")
        String expectedValue,

        @Schema(description = "Actual value found during validation.")
        String actualValue,

        @Schema(description = "Timestamp when the finding was recorded.")
        LocalDateTime createdAt

) {
}
