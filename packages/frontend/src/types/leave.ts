export interface PaidLeaveBalanceResponse {
  fiscalYear: number;
  grantedDays: number;
  usedDays: number;
  carriedOverDays: number;
  remainingDays: number;
}

export interface LeaveRequestPayload {
  leaveDate: string;
  leaveType: string;
}
