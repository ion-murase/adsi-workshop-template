export interface TimeRecordResponse {
  id: string;
  workDate: string;
  clockIn: string;
  clockOut: string | null;
  breakMinutes: number;
  workMinutes: number | null;
  overtimeMinutes: number | null;
  nightMinutes: number | null;
  holidayWorkMinutes: number | null;
  isHoliday: boolean;
}

export interface ClockStatusResponse {
  workDate: string;
  clockIn: string | null;
  clockOut: string | null;
  currentState: 'NOT_CLOCKED_IN' | 'WORKING' | 'OUT' | 'CLOCKED_OUT';
  entries: EntryRecord[];
}

export interface EntryRecord {
  entryType: 'GO_OUT' | 'RETURN';
  recordedAt: string;
}

export interface MonthlySummaryResponse {
  year: number;
  month: number;
  workDays: number;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  totalNightMinutes: number;
  totalHolidayWorkMinutes: number;
}

export interface HolidayResponse {
  id: string;
  holidayDate: string;
  holidayName: string;
  fiscalYear: number;
}

export interface HolidayRequest {
  holidayDate: string;
  holidayName: string;
}
