export interface ReportFileUploadResponse {
  fileId: string
  jobId: string
  originalFilename: string
  status: string
  message: string
}

export interface UploadedFileResponse {
  fileId: string
  originalFilename: string
  storedFilename: string
  contentType: string | null
  fileSize: number
  checksum: string
  status: string
  uploadedBy: string
  uploadedAt: string
  updatedAt: string | null
}
