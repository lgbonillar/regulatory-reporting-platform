import { map, OperatorFunction } from 'rxjs'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  metadata: {
    timestamp: string
    count?: number
    requestId?: string
  }
}

export function unwrapApiResponse<T> (): OperatorFunction<ApiResponse<T>, T> {
  return map((response) => response.data)
}
