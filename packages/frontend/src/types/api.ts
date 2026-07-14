export type Role = 'ADMIN' | 'APPROVER' | 'GENERAL';

export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';

export type ApplicationType = 'CLOCK_FIX' | 'LEAVE_REQUEST';

export type LeaveType = 'FULL_DAY' | 'HALF_AM' | 'HALF_PM';

export type EntryType = 'GO_OUT' | 'RETURN';

export type NotificationType =
  | 'OVERTIME_30'
  | 'OVERTIME_45'
  | 'OVERTIME_60'
  | 'APPLICATION_RECEIVED'
  | 'APPLICATION_APPROVED'
  | 'APPLICATION_REJECTED';

export interface User {
  id: string;
  email: string;
  name: string;
  role: Role;
  primaryDepartmentId: string;
  active: boolean;
}

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
}

export interface ValidationErrorResponse {
  status: number;
  message: string;
  errors: { field: string; message: string }[];
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
