package com.mrcrafterman.regreporting.upload.dto;

import java.util.UUID;

public record ReportFileUploadResponse(
        UUID fileId,
        UUID jobId,
        String originalFilename,
        String status,
        String message
) {
}
