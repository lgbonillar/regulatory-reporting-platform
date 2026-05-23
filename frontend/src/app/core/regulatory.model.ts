export type ProcessingJobStatus =
    | 'PENDING_EXECUTION'
    | 'PROCESSING'
    | 'PROCESSING_FAILED'
    | 'AWAITING_APPROVAL'
    | 'APPROVED'
    | 'REJECTED'
    | 'REVOKED'

export type FileStatus = 'STORED' | 'MISSING' | 'FAILED' | 'DELETED'
