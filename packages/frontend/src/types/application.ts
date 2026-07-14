import { ApplicationStatus, ApplicationType } from './api';

export interface ClockFixDetailResponse {
  targetDate: string;
  correctedClockIn: string | null;
  correctedClockOut: string | null;
  reason: string;
}

export interface ApplicationResponse {
  id: string;
  applicantId: string;
  applicantName: string;
  approverId: string | null;
  type: ApplicationType;
  status: ApplicationStatus;
  rejectionComment: string | null;
  appliedAt: string;
  decidedAt: string | null;
  clockFixDetail: ClockFixDetailResponse | null;
}

export interface ApplicationPageResponse {
  content: ApplicationResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ClockFixRequest {
  targetDate: string;
  correctedClockIn: string | null;
  correctedClockOut: string | null;
  reason: string;
}

export interface RejectRequest {
  comment: string;
}
