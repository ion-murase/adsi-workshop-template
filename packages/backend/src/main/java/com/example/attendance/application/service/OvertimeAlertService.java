package com.example.attendance.application.service;

import java.time.YearMonth;
import java.util.UUID;

public interface OvertimeAlertService {

    void checkAndNotify(UUID userId, YearMonth yearMonth);
}
