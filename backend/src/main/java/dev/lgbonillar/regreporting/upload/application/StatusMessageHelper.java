package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;

public final class StatusMessageHelper {

    private StatusMessageHelper() {
    }

    public static String historyMessage(UploadedFileStatus status, String verb) {
        return switch (status) {
            case STORED -> "File " + verb + " successfully";
            case PENDING_CORRECTION -> "File " + verb + " with validation issues";
            case FAILED -> "File " + verb + " validation failed";
            default -> "File " + verb;
        };
    }

    public static String statusMessage(UploadedFileStatus status, String verb) {
        return switch (status) {
            case PENDING_CORRECTION -> "File " + verb + " with validation issues";
            case FAILED -> "File " + verb + " validation failed";
            default -> "File " + verb;
        };
    }
}