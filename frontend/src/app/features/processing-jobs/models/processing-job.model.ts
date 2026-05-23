import { FileStatus, ProcessingJobStatus } from '../../../core/regulatory.model'

export interface ProcessingJobResponse {
  jobId: string
  fileId: string
  originalFilename: string
  fileStatus: FileStatus
  jobStatus: ProcessingJobStatus
  message: string | null
  uploadedBy: string
  createdAt: string
  updatedAt: string | null
}
