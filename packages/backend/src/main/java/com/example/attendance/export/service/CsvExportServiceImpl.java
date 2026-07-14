package com.example.attendance.export.service;

import com.example.attendance.clock.entity.TimeRecord;
import com.example.attendance.clock.repository.TimeRecordRepository;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CsvExportServiceImpl implements CsvExportService {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] WEEKDAYS = {"月", "火", "水", "木", "金", "土", "日"};

    private final TimeRecordRepository timeRecordRepository;
    private final UserRepository userRepository;

    public CsvExportServiceImpl(TimeRecordRepository timeRecordRepository,
                                UserRepository userRepository) {
        this.timeRecordRepository = timeRecordRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTimeRecords(UUID requesterId, UUID targetUserId, int year, int month) {
        var requester = getUser(requesterId);
        validateAccess(requester, targetUserId);

        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        var records = timeRecordRepository.findByUserIdAndYearMonth(targetUserId, startDate, endDate);

        var sb = new StringBuilder();
        sb.append("日付,曜日,出勤,退勤,休憩,実労働時間,残業時間,深夜時間,休日出勤,備考\n");

        for (var record : records) {
            sb.append(record.getWorkDate()).append(",");
            sb.append(getWeekdayName(record.getWorkDate())).append(",");
            sb.append(record.getClockIn() != null ? record.getClockIn().format(TIME_FORMAT) : "").append(",");
            sb.append(record.getClockOut() != null ? record.getClockOut().format(TIME_FORMAT) : "").append(",");
            sb.append(formatMinutes(record.getBreakMinutes())).append(",");
            sb.append(formatMinutes(record.getWorkMinutes())).append(",");
            sb.append(formatMinutes(record.getOvertimeMinutes())).append(",");
            sb.append(formatMinutes(record.getNightMinutes())).append(",");
            sb.append(formatMinutes(record.getHolidayWorkMinutes())).append(",");
            sb.append("\n");
        }

        return addBom(sb.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportMonthlySummary(UUID requesterId, int year, int month) {
        var requester = getUser(requesterId);
        validateAdminOrApprover(requester);

        var users = getAccessibleUsers(requester);
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        var sb = new StringBuilder();
        sb.append("社員番号,氏名,部署,勤務日数,勤務時間,残業時間,深夜時間,休日出勤時間,有給取得日数\n");

        for (var user : users) {
            var records = timeRecordRepository.findByUserIdAndYearMonth(user.getId(), startDate, endDate);
            int workDays = records.size();
            int totalWork = records.stream().mapToInt(r -> r.getWorkMinutes() != null ? r.getWorkMinutes() : 0).sum();
            int totalOvertime = records.stream().mapToInt(r -> r.getOvertimeMinutes() != null ? r.getOvertimeMinutes() : 0).sum();
            int totalNight = records.stream().mapToInt(r -> r.getNightMinutes() != null ? r.getNightMinutes() : 0).sum();
            int totalHoliday = records.stream().mapToInt(r -> r.getHolidayWorkMinutes() != null ? r.getHolidayWorkMinutes() : 0).sum();

            sb.append(user.getId().toString().substring(0, 8)).append(",");
            sb.append(escapeCsv(user.getName())).append(",");
            sb.append(user.getPrimaryDepartmentId() != null ? user.getPrimaryDepartmentId().toString().substring(0, 8) : "").append(",");
            sb.append(workDays).append(",");
            sb.append(formatMinutes(totalWork)).append(",");
            sb.append(formatMinutes(totalOvertime)).append(",");
            sb.append(formatMinutes(totalNight)).append(",");
            sb.append(formatMinutes(totalHoliday)).append(",");
            sb.append("0").append("\n");
        }

        return addBom(sb.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUsers(UUID requesterId) {
        var requester = getUser(requesterId);
        validateAdminOrApprover(requester);

        var users = getAccessibleUsers(requester);

        var sb = new StringBuilder();
        sb.append("社員番号,氏名,メールアドレス,ロール,主管部署,ステータス,登録日\n");

        for (var user : users) {
            sb.append(user.getId().toString().substring(0, 8)).append(",");
            sb.append(escapeCsv(user.getName())).append(",");
            sb.append(escapeCsv(user.getEmail())).append(",");
            sb.append(user.getRole()).append(",");
            sb.append(user.getPrimaryDepartmentId() != null ? user.getPrimaryDepartmentId().toString().substring(0, 8) : "").append(",");
            sb.append(user.getActive() ? "有効" : "無効").append(",");
            sb.append(user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate() : "").append("\n");
        }

        return addBom(sb.toString());
    }

    private void validateAccess(User requester, UUID targetUserId) {
        if (requester.getRole() == Role.ADMIN) {
            return;
        }
        if (requester.getRole() == Role.APPROVER) {
            return;
        }
        if (!requester.getId().equals(targetUserId)) {
            throw new BusinessException("他のユーザのデータをエクスポートする権限がありません", HttpStatus.FORBIDDEN);
        }
    }

    private void validateAdminOrApprover(User requester) {
        if (requester.getRole() != Role.ADMIN && requester.getRole() != Role.APPROVER) {
            throw new BusinessException("エクスポートする権限がありません", HttpStatus.FORBIDDEN);
        }
    }

    private List<User> getAccessibleUsers(User requester) {
        if (requester.getRole() == Role.ADMIN) {
            return userRepository.findByActiveOrderByNameAsc(true);
        }
        return userRepository.findByPrimaryDepartmentIdAndActiveOrderByNameAsc(
                requester.getPrimaryDepartmentId(), true);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("ユーザが見つかりません", HttpStatus.NOT_FOUND));
    }

    private String getWeekdayName(LocalDate date) {
        return WEEKDAYS[date.getDayOfWeek().getValue() - 1];
    }

    private String formatMinutes(Integer minutes) {
        if (minutes == null || minutes == 0) return "0:00";
        return (minutes / 60) + ":" + String.format("%02d", minutes % 60);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") ||
                value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private byte[] addBom(String content) {
        var contentBytes = content.getBytes(StandardCharsets.UTF_8);
        var result = new byte[BOM.length + contentBytes.length];
        System.arraycopy(BOM, 0, result, 0, BOM.length);
        System.arraycopy(contentBytes, 0, result, BOM.length, contentBytes.length);
        return result;
    }
}
