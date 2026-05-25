package dev.lgbonillar.regreporting.processing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Processing job status transition audit entry.")
public record ProcessingJobStatusHistoryResponse(
        @Schema(description = "History entry identifier.")
        UUID id,

        @Schema(description = "Previous workflow status. Null for the initial entry.", example = "PENDING_EXECUTION")
        String previousStatus,

        @Schema(description = "New workflow status after the transition.", example = "PROCESSING")
        String newStatus,

        @Schema(description = "Transition origin.", example = "USER")
        String transitionSource,

        @Schema(description = "Username that caused the transition, when source is USER.", example = "admin01")
        String transitionedBy,

        @Schema(description = "Human-readable reason for the transition.")
        String reason,

        @Schema(description = "Timestamp when the transition was recorded.")
        LocalDateTime createdAt
) {
}
