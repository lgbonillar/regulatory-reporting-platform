package dev.lgbonillar.regreporting.upload.application.support;

import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;

public final class UploadedFileStatusMessages {

    private UploadedFileStatusMessages() {
    }

    public static String uploadHistoryMessage(UploadedFileStatus status) {
        return switch (status) {
            case STORED -> "File uploaded successfully";
            case PENDING_CORRECTION -> "File uploaded with validation issues";
            case FAILED -> "File upload validation failed";
            default -> "File uploaded";
        };
    }

    public static String uploadResponseMessage(UploadedFileStatus status) {
        return switch (status) {
            case PENDING_CORRECTION -> "File uploaded with validation issues";
            case FAILED -> "File upload validation failed";
            default -> "File uploaded";
        };
    }

    public static String updateHistoryMessage(UploadedFileStatus status) {
        return switch (status) {
            case STORED -> "File updated successfully";
            case PENDING_CORRECTION -> "File updated with validation issues";
            case FAILED -> "File update validation failed";
            default -> "File updated";
        };
    }

    public static String updateResponseMessage(UploadedFileStatus status) {
        return switch (status) {
            case PENDING_CORRECTION -> "File updated with validation issues";
            case FAILED -> "File update validation failed";
            default -> "File updated";
        };
    }
}