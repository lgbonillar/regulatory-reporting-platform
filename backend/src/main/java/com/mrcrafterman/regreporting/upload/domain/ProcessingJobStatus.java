package com.mrcrafterman.regreporting.upload.domain;

public enum ProcessingJobStatus {
    PENDING_EXECUTION,
    PROCESSING,
    PROCESSING_FAILED,
    AWAITING_APPROVAL,
    APPROVED,
    REJECTED,
    REVOKED
}
