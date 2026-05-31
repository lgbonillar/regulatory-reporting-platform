package dev.lgbonillar.regreporting.upload.application;

import dev.lgbonillar.regreporting.upload.domain.UploadedFile;
import dev.lgbonillar.regreporting.upload.domain.UploadedFileStatus;

public final class UploadedFileStatusApplier {

    private UploadedFileStatusApplier() {
    }

    public static void applyStatus(UploadedFile uploadedFile, UploadedFileStatus status) {
        switch (status) {
            case STORED -> uploadedFile.markStored();
            case PENDING_CORRECTION -> uploadedFile.markPendingCorrection();
            case FAILED -> uploadedFile.markFailed();
            default -> throw new IllegalArgumentException("Unsupported validation status: " + status);
        }
    }
}