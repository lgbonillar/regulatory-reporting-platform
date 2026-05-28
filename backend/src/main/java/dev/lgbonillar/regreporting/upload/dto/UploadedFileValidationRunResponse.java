package dev.lgbonillar.regreporting.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Validation run executed for an uploaded file.")
public record UploadedFileValidationRunResponse(
        @Schema(description = "Validation run identifier.")
        UUID validationRunId,

        @Schema(description = "Uploaded file identifier.")
        UUID fileId,

        @Schema(description = "Validation run status.", example = "FAILED")
        String status,

        @Schema(description = "Source that triggered validation.", example = "UPLOAD")
        String source,

        @Schema(description = "Short validation result summary.")
        String summaryMessage,

        @Schema(description = "Username that triggered validation.")
        String createdBy,

        @Schema(description = "Timestamp when validation was created.")
        LocalDateTime createdAt
) {
}
