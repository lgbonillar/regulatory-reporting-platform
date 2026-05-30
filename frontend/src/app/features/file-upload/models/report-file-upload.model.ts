import { FileStatus, ProcessingJobStatus } from '../../../core/regulatory.model'

export interface ReportFileUploadResponse {
  fileId: string
  jobId: string | null
  originalFilename: string
  fileStatus: FileStatus
  jobStatus: ProcessingJobStatus | null
  message: string
}

export interface UploadedFileResponse {
  fileId: string
  originalFilename: string
  storedFilename: string
  contentType: string | null
  fileSize: number
  checksum: string
  fileStatus: FileStatus
  uploadedBy: string
  uploadedAt: string
  updatedAt: string | null
}

export interface UploadedFileValidationRunResponse {
  validationRunId: string
  fileId: string
  status: string
  source: string
  summaryMessage: string
  createdBy: string
  createdAt: string
}

export interface UploadedFileFindingResponse {
  findingId: string
  validationRunId: string
  fileId: string
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
