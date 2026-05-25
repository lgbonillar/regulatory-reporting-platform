package dev.lgbonillar.regreporting.processing.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingJobResponse (
        UUID jobId,
        UUID fileId,
        String originalFilename,
        String fileStatus,
        String jobStatus,
        String message,
        String uploadedBy,
        String triggeredBy,
        LocalDateTime triggeredAt,
        LocalDateTime processingCompletedAt,
        String failureReason,
        String approvedBy,
        LocalDateTime approvedAt,
        String rejectedBy,
        LocalDateTime rejectedAt,
        String rejectionReason,
        String revokedBy,
        LocalDateTime revokedAt,
        String revocationReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
}
