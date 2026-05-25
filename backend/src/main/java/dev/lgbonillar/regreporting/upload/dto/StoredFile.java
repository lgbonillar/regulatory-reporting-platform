package dev.lgbonillar.regreporting.upload.dto;

public record StoredFile(
        String storedFilename,
        String relativeStoragePath,
        String checksum
) {
}
