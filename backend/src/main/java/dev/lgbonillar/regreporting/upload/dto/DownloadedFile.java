package dev.lgbonillar.regreporting.upload.dto;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import org.springframework.core.io.Resource;

public record DownloadedFile(
        UploadedFile uploadedFile,
        Resource resource
) {
}