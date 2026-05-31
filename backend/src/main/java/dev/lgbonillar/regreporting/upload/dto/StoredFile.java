package dev.lgbonillar.regreporting.upload.dto;

public record StoredFile(
        String originalFilename,
        String storedFilename,
        String relativeStoragePath,
        String checksum
) {
}
