export type ProcessingJobStatus =
    | 'PENDING_EXECUTION'
    | 'PROCESSING'
    | 'PROCESSING_FAILED'
    | 'AWAITING_APPROVAL'
    | 'APPROVED'
    | 'REJECTED'
    | 'REVOKED'

export type FileStatus = 'STORED' | 'PENDING_CORRECTION' | 'MISSING' | 'FAILED' | 'DELETED'
