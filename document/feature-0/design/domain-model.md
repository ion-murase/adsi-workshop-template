# ドメインモデル設計

## ドメイン概要図

```
┌─────────────────────────────────────────────────────────────────────┐
│                        勤怠管理ドメイン                                │
├─────────────┬─────────────┬──────────────┬─────────────────────────┤
│  組織管理    │   打刻      │  申請・承認   │    通知                  │
│             │             │              │                         │
│ User        │ TimeRecord  │ Application  │ Notification            │
│ Department  │ TimeEntry   │  ├ ClockFix  │                         │
│ Site        │             │  └ LeaveReq  │                         │
│             │             │              │                         │
│ Calendar    │             │ PaidLeave    │                         │
│ Holiday     │             │              │                         │
└─────────────┴─────────────┴──────────────┴─────────────────────────┘
```

---

## Entity

### User（ユーザ）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| email | String | ログインID（一意） |
| passwordHash | String | BCrypt ハッシュ |
| name | String | 氏名 |
| role | Role (enum) | ADMIN / APPROVER / GENERAL |
| primaryDepartmentId | UUID | 主管部署 FK |
| requirePasswordChange | Boolean | 仮パスワード変更強制フラグ |
| active | Boolean | 有効/無効（論理削除） |
| hasAttendanceRecord | Boolean | 出退勤記録有無（物理削除可否判定） |
| failedLoginAttempts | Integer | 連続ログイン失敗回数 |
| lockedUntil | ZonedDateTime | アカウントロック解除時刻 |
| createdAt | ZonedDateTime | |
| updatedAt | ZonedDateTime | |

**ビジネスルール:**
- `hasAttendanceRecord = true` の場合、物理削除不可（無効化のみ）
- `active = false` の場合、ログイン不可
- `failedLoginAttempts >= 5` でロック。30分後に解除

### Department（部署）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| name | String | 部署名 |
| siteId | UUID | 拠点 FK |
| createdAt | ZonedDateTime | |
| updatedAt | ZonedDateTime | |

**ビジネスルール:**
- 所属ユーザがいる場合は削除不可

### UserDepartment（ユーザ所属部署 — 中間テーブル）

| 属性 | 型 | 説明 |
|------|-----|------|
| userId | UUID | FK |
| departmentId | UUID | FK |

### Site（拠点）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| name | String | 拠点名 |
| timezone | String | IANA タイムゾーン（例: Asia/Tokyo） |
| createdAt | ZonedDateTime | |
| updatedAt | ZonedDateTime | |

### TimeRecord（勤怠記録 — 日単位）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| userId | UUID | FK |
| workDate | LocalDate | 勤務日（拠点TZ基準） |
| clockIn | ZonedDateTime | 出勤打刻 |
| clockOut | ZonedDateTime | 退勤打刻（null=未退勤） |
| breakMinutes | Integer | 休憩時間（固定60分） |
| workMinutes | Integer | 実労働時間（分） |
| overtimeMinutes | Integer | 残業時間（分） |
| nightMinutes | Integer | 深夜勤務時間（分） |
| holidayWorkMinutes | Integer | 休日出勤時間（分） |
| isHoliday | Boolean | 休日フラグ |
| timezone | String | 記録時点のTZ（遡及変更防止） |
| createdAt | ZonedDateTime | |
| updatedAt | ZonedDateTime | |
| version | Long | 楽観ロック |

**ビジネスルール:**
- 1ユーザ1日1レコード
- workMinutes = (clockOut - clockIn) を分換算 - breakMinutes
- overtimeMinutes = max(0, workMinutes - 450) ※450分=7.5時間
- 半日休の日: overtimeMinutes = max(0, workMinutes - 225) ※225分=3.75時間
- 日またぎ勤務: 暦日ごとに分割して計算

### TimeEntry（中間打刻）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| timeRecordId | UUID | FK |
| type | EntryType (enum) | GO_OUT / RETURN |
| timestamp | ZonedDateTime | 打刻時刻 |
| createdAt | ZonedDateTime | |

**ビジネスルール:**
- 記録のみ。勤務時間計算には影響しない
- GO_OUT と RETURN は交互に記録される

### Application（申請 — 共通基盤）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| applicantId | UUID | 申請者 FK |
| approverId | UUID | 承認者 FK（承認/却下時に記録） |
| type | ApplicationType (enum) | CLOCK_FIX / LEAVE_REQUEST |
| status | ApplicationStatus (enum) | PENDING / APPROVED / REJECTED / WITHDRAWN |
| rejectionComment | String | 却下理由（却下時のみ） |
| appliedAt | ZonedDateTime | 申請日時 |
| decidedAt | ZonedDateTime | 承認/却下日時 |
| createdAt | ZonedDateTime | |
| updatedAt | ZonedDateTime | |
| version | Long | 楽観ロック |

**ビジネスルール:**
- PENDING のみ取り下げ可能
- 却下後の再申請は別レコードとして作成

### ClockFixDetail（打刻修正申請 詳細）

| 属性 | 型 | 説明 |
|------|-----|------|
| applicationId | UUID | FK (Application) |
| targetDate | LocalDate | 修正対象日 |
| correctedClockIn | ZonedDateTime | 修正後出勤時刻 |
| correctedClockOut | ZonedDateTime | 修正後退勤時刻 |
| reason | String | 修正理由（必須） |

**ビジネスルール:**
- 遡及期限: 翌月第3営業日まで
- 承認後に TimeRecord を更新する

### LeaveRequestDetail（有給休暇申請 詳細）

| 属性 | 型 | 説明 |
|------|-----|------|
| applicationId | UUID | FK (Application) |
| leaveDate | LocalDate | 休暇日 |
| leaveType | LeaveType (enum) | FULL_DAY / HALF_AM / HALF_PM |

**ビジネスルール:**
- 半日休: 0.5日消化
- 有給残日数がゼロの場合は申請不可
- 事後申請可能

### PaidLeaveBalance（有給残日数）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| userId | UUID | FK |
| fiscalYear | Integer | 対象年度 |
| grantedDays | BigDecimal | 付与日数 |
| usedDays | BigDecimal | 消化日数 |
| carriedOverDays | BigDecimal | 前年繰越日数 |
| expiresAt | LocalDate | 繰越有効期限 |
| createdAt | ZonedDateTime | |
| updatedAt | ZonedDateTime | |
| version | Long | 楽観ロック |

**ビジネスルール:**
- remainingDays = grantedDays + carriedOverDays - usedDays
- 入社日基準で自動付与（労基法最低基準）
- 前年度未消化分は1年間のみ繰越

### CompanyCalendar（会社カレンダー）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| date | LocalDate | 日付 |
| holidayName | String | 休日名称 |
| year | Integer | 年度（検索用） |
| createdAt | ZonedDateTime | |

**ビジネスルール:**
- 土日 + 日本の祝日 + ここで登録した日 = 休日
- 年度ごとに再設定

### Notification（通知）

| 属性 | 型 | 説明 |
|------|-----|------|
| id | UUID | PK |
| recipientId | UUID | 通知先ユーザ FK |
| type | NotificationType (enum) | OVERTIME_30 / OVERTIME_45 / OVERTIME_60 / APPLICATION_RECEIVED / APPLICATION_APPROVED / APPLICATION_REJECTED |
| title | String | 通知タイトル |
| message | String | 通知本文 |
| referenceId | UUID | 関連エンティティID（申請ID等） |
| isRead | Boolean | 既読フラグ |
| createdAt | ZonedDateTime | |

---

## Value Object

| VO名 | 属性 | 用途 |
|-------|------|------|
| WorkDuration | minutes: Integer | 勤務時間の表現（分単位） |
| DateRange | from: LocalDate, to: LocalDate | 期間指定（CSV出力等） |

---

## Enum

| Enum名 | 値 |
|--------|-----|
| Role | ADMIN, APPROVER, GENERAL |
| EntryType | GO_OUT, RETURN |
| ApplicationType | CLOCK_FIX, LEAVE_REQUEST |
| ApplicationStatus | PENDING, APPROVED, REJECTED, WITHDRAWN |
| LeaveType | FULL_DAY, HALF_AM, HALF_PM |
| NotificationType | OVERTIME_30, OVERTIME_45, OVERTIME_60, APPLICATION_RECEIVED, APPLICATION_APPROVED, APPLICATION_REJECTED |

---

## Service

| Service | 責務 |
|---------|------|
| AuthenticationService | ログイン認証、セッション管理、パスワード変更、ロック制御 |
| ClockService | 出勤/退勤/中間打刻の記録、勤務時間計算 |
| OvertimeAlertService | 月次残業集計、閾値判定、通知生成 |
| ApplicationService | 申請の提出/承認/却下/取り下げ、勤怠記録への反映 |
| PaidLeaveService | 有給付与・消化・残日数計算・繰越処理 |
| UserManagementService | ユーザCRUD、論理削除、仮パスワード発行 |
| CalendarService | 会社カレンダー管理、休日判定 |
| NotificationService | 通知生成・既読管理 |
| CsvExportService | 勤務履歴/月別集計/ユーザ一覧のCSV生成 |
| TimeZoneService | 拠点TZの解決、日付変換 |

---

## Repository

| Repository | 主な操作 |
|-----------|---------|
| UserRepository | findByEmail, findByDepartmentId, findActiveUsers |
| DepartmentRepository | findBySiteId, existsByIdWithUsers |
| SiteRepository | findAll |
| TimeRecordRepository | findByUserIdAndWorkDate, findByUserIdAndMonth |
| TimeEntryRepository | findByTimeRecordId |
| ApplicationRepository | findByApplicantId, findPendingByDepartmentId |
| PaidLeaveBalanceRepository | findByUserIdAndFiscalYear |
| CompanyCalendarRepository | findByYear, isHoliday |
| NotificationRepository | findByRecipientId, countUnread |

---

## ドメイン関連図

```
Site 1──* Department *──* User（UserDepartment中間テーブル）
                              │
              User.primaryDepartmentId ──→ Department
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
    TimeRecord           Application          PaidLeaveBalance
    1──* TimeEntry        │
                    ┌─────┴─────┐
              ClockFixDetail  LeaveRequestDetail
                              
    CompanyCalendar（独立）
    Notification（User に紐づく）
```
