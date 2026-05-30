import { FileStatus, ProcessingJobStatus } from '../../../core/regulatory.model'

export interface ProcessingJobResponse {
  jobId: string
  fileId: string
  originalFilename: string
  fileStatus: FileStatus
  jobStatus: ProcessingJobStatus
  message: string | null
  uploadedBy: string
  triggeredBy: string | null
  triggeredAt: string | null
  processingCompletedAt: string | null
  failureReason: string | null
  approvedBy: string | null
  approvedAt: string | null
  rejectedBy: string | null
  rejectedAt: string | null
  rejectionReason: string | null
  revokedBy: string | null
  revokedAt: string | null
  revocationReason: string | null
  createdAt: string
  updatedAt: string | null
}

export interface ProcessingJobStatusHistoryResponse {
  id: string
  previousStatus: ProcessingJobStatus | null
  newStatus: ProcessingJobStatus
  transitionSource: 'USER' | 'SYSTEM'
  transitionedBy: string | null
  reason: string | null
  createdAt: string
}

export interface ProcessingJobFindingResponse {
  id: string
  severity: string
  scope: string
  code: string
  message: string
  sheetName: string | null
  rowNumber: number | null
  columnName: string | null
  fieldName: string | null
  rejectedValue: string | null
  expectedValue: string | null
  actualValue: string | null
  createdAt: string
}
