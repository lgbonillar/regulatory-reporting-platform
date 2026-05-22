import { FileStatus, ProcessingJobStatus } from '../../../core/regulatory.model'

export interface ReportFileUploadResponse {
  fileId: string
  jobId: string
  originalFilename: string
  fileStatus: FileStatus
  jobStatus: ProcessingJobStatus
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
