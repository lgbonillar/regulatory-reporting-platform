package dev.lgbonillar.regreporting.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned after uploading or replacing a report file.")
public record ReportFileUploadResponse(
        @Schema(description = "Uploaded file identifier.")
        UUID fileId,

        @Schema(description = "Processing job created for the uploaded file.")
        UUID jobId,

        @Schema(description = "Original uploaded filename.", example = "capital-requirements-jan.xlsx")
        String originalFilename,

        @Schema(description = "Stored file status.", example = "STORED")
        String fileStatus,

        @Schema(description = "Initial processing job status.", example = "PENDING_EXECUTION")
        String jobStatus,

        @Schema(description = "Short user-facing upload result message.")
        String message
) {
}
