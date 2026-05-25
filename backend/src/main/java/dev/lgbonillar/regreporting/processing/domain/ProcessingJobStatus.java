package dev.lgbonillar.regreporting.processing.domain;

public enum ProcessingJobStatus {
    PENDING_EXECUTION,
    PROCESSING,
    PROCESSING_FAILED,
    AWAITING_APPROVAL,
    APPROVED,
    REJECTED,
    REVOKED
}
