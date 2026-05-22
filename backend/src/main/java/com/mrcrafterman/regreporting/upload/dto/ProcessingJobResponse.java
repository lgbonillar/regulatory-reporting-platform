package com.mrcrafterman.regreporting.upload.dto;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
}
