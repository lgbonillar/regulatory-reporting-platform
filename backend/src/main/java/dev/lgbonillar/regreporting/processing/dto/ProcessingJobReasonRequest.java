package dev.lgbonillar.regreporting.processing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Administrative reason request for reject or revoke operations.")
public record ProcessingJobReasonRequest(
        @Schema(
                description = "Administrative decision reason.",
                example = "Totals do not match supporting documentation"
        )
        @NotBlank(message = "Reason is required")
        @Size(max = 1000, message = "Reason must not exceed 1000 characters")
        String reason
) {
}
