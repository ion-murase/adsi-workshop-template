# Unit 2: 打刻・勤務時間

## 目的

出退勤打刻、中間打刻、勤務時間計算、月次履歴表示、会社カレンダー管理を実装する。

## Phase

**Phase B**（Unit 0 完了後、Unit 1/3 と並行可能）

## 依存

- Unit 0: 共通基盤（テーブル DDL, Entity 基盤）
- Unit 1（実行時）: 認証済みユーザの取得。**ただし開発時は Mock で代替可能なため並行実装できる**

## ユーザーストーリー

- US-1: 打刻（出勤/退勤/外出/戻り）
- US-2: 勤務時間計算（実労働/残業/深夜/休日出勤）
- US-3: 勤務履歴（月次一覧・集計）
- US-10: 会社カレンダー管理

## テーブル

- time_records
- time_entries
- company_calendars

## API エンドポイント

| メソッド | パス | 操作 |
|---------|------|------|
| POST | /clock/in | 出勤打刻 |
| POST | /clock/out | 退勤打刻 |
| POST | /clock/go-out | 外出打刻 |
| POST | /clock/return | 戻り打刻 |
| GET | /clock/status | 本日の打刻状態 |
| GET | /time-records | 月次勤怠一覧 |
| GET | /time-records/summary | 月次集計 |
| GET | /calendar/holidays | 休日一覧 |
| POST | /calendar/holidays | 休日登録 |
| DELETE | /calendar/holidays/{id} | 休日削除 |

## Backend 実装

### Entity
- TimeRecord, TimeEntry, CompanyCalendar

### Repository
- TimeRecordRepository: findByUserIdAndWorkDate, findByUserIdAndYearMonth
- TimeEntryRepository: findByTimeRecordId
- CompanyCalendarRepository: findByYear, isHoliday

### Service
- ClockService
  - clockIn(userId): 出勤打刻。既に打刻済みの場合は409
  - clockOut(userId): 退勤打刻。勤務時間を計算して保存
  - goOut(userId): 外出打刻
  - returnFromOut(userId): 戻り打刻
  - getStatus(userId): 本日の打刻状態取得
  - calculateWorkTime(clockIn, clockOut, isHoliday): 勤務時間計算ロジック
- CalendarService
  - getHolidays(year): 年度の休日一覧
  - isHoliday(date): 休日判定（土日 + 祝日 + 会社カレンダー）
  - createHoliday(date, name): 休日登録
  - deleteHoliday(id): 休日削除

### 勤務時間計算ロジック（重要）

```
入力: clockIn(ZonedDateTime), clockOut(ZonedDateTime), isHoliday(boolean)
出力: workMinutes, overtimeMinutes, nightMinutes, holidayWorkMinutes

1. 打刻時刻を分単位に切り捨て
2. totalMinutes = clockOut - clockIn (分)
3. workMinutes = totalMinutes - 60 (休憩固定控除)
4. overtimeMinutes = max(0, workMinutes - 450)  ※7.5h = 450min
   - 半日休の日: max(0, workMinutes - 225)    ※3.75h = 225min
5. nightMinutes = 22:00〜05:00 に該当する時間（分）
6. holidayWorkMinutes = isHoliday ? workMinutes : 0
7. 日またぎ: 暦日ごとに分割して各日のレコードに分配
```

## Frontend 実装

### 画面
- ダッシュボード (/) — 打刻ボタン + 本日の状態 + 今月サマリ
- 勤務履歴 (/time-records) — 月別一覧テーブル
- 会社カレンダー (/admin/calendar) — 休日一覧 + 登録/削除

### コンポーネント
- ClockButtons（出勤/退勤/外出/戻り — 状態に応じて活性/非活性）
- TodayRecord（本日の打刻記録表示）
- MonthSummaryCard（月次集計カード）
- TimeRecordTable（勤務履歴テーブル）
- HolidayCalendarTable（休日一覧 + 追加/削除）

## テスト

### Backend
- ClockService: 出勤/退勤/重複打刻/打刻忘れ (Unit test)
- 勤務時間計算: 通常/残業/深夜/休日/日またぎ (Unit test — パラメタライズド)
- CalendarService: 休日判定（土日/祝日/会社カレンダー）(Unit test)
- ClockController: 打刻API (@WebMvcTest)
- TimeRecordRepository: 月次検索 (@DataJpaTest)
- 統合テスト: 出勤→退勤→履歴確認 (@SpringBootTest)

### Frontend
- ClockButtons: 状態遷移テスト
- TimeRecordTable: 月次データ表示
- 勤務時間のフォーマット表示

## 完了条件

- [ ] 出勤/退勤/外出/戻り打刻が動作する
- [ ] 勤務時間が正しく計算される（残業/深夜/休日区分）
- [ ] 日またぎ勤務が正しく分割計算される
- [ ] 月次勤務履歴一覧が表示される
- [ ] 月次集計（勤務日数/勤務時間/残業時間）が表示される
- [ ] 会社カレンダーの CRUD が動作する
- [ ] テストカバレッジ 80% 以上
