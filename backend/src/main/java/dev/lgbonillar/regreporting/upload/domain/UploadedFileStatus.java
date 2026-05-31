package dev.lgbonillar.regreporting.upload.domain;

public enum UploadedFileStatus {
    PENDING_VALIDATION,
    STORED,
    PENDING_CORRECTION,
    DELETED,
    MISSING,
    FAILED
}
