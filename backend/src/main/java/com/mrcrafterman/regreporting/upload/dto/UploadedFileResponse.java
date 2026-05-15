package com.mrcrafterman.regreporting.upload.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadedFileResponse(
        UUID fileId,
        String originalFilename,
        String storedFilename,
        String contentType,
        long fileSize,
        String checksum,
        String status,
        String uploadedBy,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt
) {
}
