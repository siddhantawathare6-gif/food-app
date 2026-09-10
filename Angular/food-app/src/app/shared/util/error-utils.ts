import { HttpErrorResponse } from '@angular/common/http';
import { ErrorDetails } from '../model/ErrorDetails';

export function extractErrorMessage(err: HttpErrorResponse, fallback: string): string {
  try {
    const errorDetails: ErrorDetails = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
    return errorDetails?.message || fallback;
  } catch {
    return fallback;
  }
}