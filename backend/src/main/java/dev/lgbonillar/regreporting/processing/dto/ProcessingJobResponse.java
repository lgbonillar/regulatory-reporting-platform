package dev.lgbonillar.regreporting.processing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Processing job workflow state and related file information.")
public record ProcessingJobResponse (
        @Schema(description = "Processing job identifier.")
        UUID jobId,

        @Schema(description = "Uploaded file identifier linked to this job.")
        UUID fileId,

        @Schema(description = "Original filename uploaded by the analyst.", example = "capital-requirements-jan.xlsx")
        String originalFilename,

        @Schema(description = "Current uploaded file status.", example = "STORED")
        String fileStatus,

        @Schema(description = "Current processing workflow status.", example = "PENDING_EXECUTION")
        String jobStatus,

        @Schema(description = "Short user-facing workflow message.", example = "Waiting for execution")
        String message,

        @Schema(description = "Username that uploaded the file.", example = "analyst01")
        String uploadedBy,

        @Schema(description = "Username that started processing, if any.", example = "analyst01")
        String triggeredBy,

        @Schema(description = "Timestamp when processing started.")
        LocalDateTime triggeredAt,

        @Schema(description = "Timestamp when processing completed or failed.")
        LocalDateTime processingCompletedAt,

        @Schema(description = "Failure reason when job status is PROCESSING_FAILED.")
        String failureReason,

        @Schema(description = "Administrator username that approved the result.")
        String approvedBy,

        @Schema(description = "Timestamp when the result was approved.")
        LocalDateTime approvedAt,

        @Schema(description = "Administrator username that rejected the result.")
        String rejectedBy,

        @Schema(description = "Timestamp when the result was rejected.")
        LocalDateTime rejectedAt,

        @Schema(description = "Administrative rejection reason.")
        String rejectionReason,

        @Schema(description = "Administrator username that revoked a previous approval.")
        String revokedBy,

        @Schema(description = "Timestamp when the previous approval was revoked.")
        LocalDateTime revokedAt,

        @Schema(description = "Administrative revocation reason.")
        String revocationReason,

        @Schema(description = "Timestamp when the processing job was created.")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp when the processing job was last updated.")
        LocalDateTime updatedAt
){
}
