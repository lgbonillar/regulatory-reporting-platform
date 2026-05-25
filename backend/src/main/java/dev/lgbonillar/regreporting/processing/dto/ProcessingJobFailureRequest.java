package dev.lgbonillar.regreporting.processing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request used to mark a processing job as failed.")
public record ProcessingJobFailureRequest(
        @Schema(
                description = "Technical, validation or business reason for the processing failure.",
                example = "Required column tax_identifier was not found"
        )
        @NotBlank(message = "Failure reason is required")
        @Size(max = 1000, message = "Failure reason must not exceed 1000 characters")
        String reason
) {
}
