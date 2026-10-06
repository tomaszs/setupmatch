export interface ApiError {
  message: string;
  field_errors?: Record<string, string>;
}

export interface HttpErrorResponseBody {
  error?: ApiError;
  message?: string;
}
