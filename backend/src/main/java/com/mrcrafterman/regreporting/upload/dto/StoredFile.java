package com.mrcrafterman.regreporting.upload.dto;

public record StoredFile(
        String storedFilename,
        String relativeStoragePath,
        String checksum
) {
}
