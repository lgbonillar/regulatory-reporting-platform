package com.mrcrafterman.regreporting.upload.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingJobStatusHistoryResponse(
        UUID id,
        String previousStatus,
        String newStatus,
        String transitionSource,
        String transitionedBy,
        String reason,
        LocalDateTime createdAt
) {
}
