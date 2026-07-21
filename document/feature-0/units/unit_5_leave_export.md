# Unit 5: 有給休暇・集計・CSV エクスポート

## 目的

有給休暇申請（残日数管理含む）と CSV エクスポート機能を実装する。

## Phase

**Phase C**（Unit 1, 2, 4 完了後。Unit 4 の申請基盤を利用）

## 依存

- Unit 1: ユーザ情報（入社日からの付与計算）
- Unit 2: 勤怠記録（CSV 出力対象）
- Unit 3: 通知基盤（申請到着・結果通知）
- Unit 4: 申請基盤（Application エンティティ・承認ワークフロー）

## ユーザーストーリー

- US-6: 有給休暇申請（全日休/半日休、残日数管理）
- US-11: CSV エクスポート

## テーブル

- leave_request_details
- paid_leave_balances
- （applications は Unit 4 で実装済み。共有利用）

## API エンドポイント

| メソッド | パス | 操作 |
|---------|------|------|
| POST | /applications/leave | 有給休暇申請 |
| GET | /paid-leave/balance | 有給残日数取得 |
| GET | /export/time-records | 勤務履歴CSV |
| GET | /export/monthly-summary | 月別集計CSV |
| GET | /export/users | ユーザ一覧CSV |

## Backend 実装

### Entity
- LeaveRequestDetail, PaidLeaveBalance

### Repository
- PaidLeaveBalanceRepository: findByUserIdAndFiscalYear, findCurrentBalance
- LeaveRequestDetailRepository: findByApplicationId

### Service
- PaidLeaveService
  - getBalance(userId): 現在の残日数取得
  - grantAnnualLeave(userId): 入社日基準の自動付与
    - 労基法最低基準に従う付与日数テーブル
    - 前年度未消化分の繰越（1年間のみ）
  - consume(userId, leaveType): 有給消化（FULL_DAY=1.0, HALF=0.5）
  - canApply(userId, leaveType): 残日数チェック

- ApplicationService（Unit 4 を拡張）
  - submitLeaveRequest(userId, request): 有給休暇申請の提出
    - 残日数チェック（ゼロなら申請不可）
    - 通知送信
  - 承認時: PaidLeaveService.consume() を呼び出し

- CsvExportService
  - exportTimeRecords(userId, year, month): 勤務履歴CSV
  - exportMonthlySummary(year, month, requesterId): 月別ユーザ別集計CSV
  - exportUsers(requesterId): ユーザ一覧CSV
  - 権限チェック:
    - 一般: 自分のみ
    - 承認者: 主管部署の部下 + 自分
    - 管理者: 全員

### 有給付与テーブル（労基法最低基準）

| 勤続年数 | 付与日数 |
|---------|---------|
| 0.5年 | 10日 |
| 1.5年 | 11日 |
| 2.5年 | 12日 |
| 3.5年 | 14日 |
| 4.5年 | 16日 |
| 5.5年 | 18日 |
| 6.5年以上 | 20日 |

### CSV 出力フォーマット

**勤務履歴CSV:**
```
日付,曜日,出勤,退勤,休憩,実労働時間,残業時間,深夜時間,休日出勤,備考
2026-07-01,火,09:00,18:30,1:00,8:30,1:00,0:00,0:00,
2026-07-02,水,09:15,17:45,1:00,7:30,0:00,0:00,0:00,
```

**月別集計CSV:**
```
社員番号,氏名,部署,勤務日数,勤務時間,残業時間,深夜時間,休日出勤時間,有給取得日数
```

**ユーザ一覧CSV:**
```
社員番号,氏名,メールアドレス,ロール,主管部署,ステータス,登録日
```

文字コード: UTF-8 BOM 付き

## Frontend 実装

### 画面
- 有給休暇申請フォーム (/applications/leave/new) — 日付・種別選択 + 残日数表示
- CSV エクスポート (/export) — 対象データ・期間選択 → ダウンロード

### コンポーネント
- LeaveRequestForm（休暇日・種別選択）
- PaidLeaveBalanceCard（残日数表示）
- ExportForm（データ種別・対象月選択・ダウンロードボタン）

## テスト

### Backend
- PaidLeaveService: 付与計算/消化/繰越/残日数不足 (Unit test — パラメタライズド)
- 有給申請 → 承認 → 残日数消化フロー (Unit test)
- CsvExportService: CSV 出力フォーマット/権限チェック (Unit test)
- 統合テスト: 有給申請→承認→残日数更新 (@SpringBootTest)

### Frontend
- LeaveRequestForm: 種別選択・残日数表示
- ExportForm: ダウンロードリンク生成

## 完了条件

- [ ] 有給休暇申請（全日休/半日休）が動作する
- [ ] 残日数チェック（ゼロで申請不可）が動作する
- [ ] 入社日基準の自動付与が動作する
- [ ] 前年度繰越（1年間のみ）が動作する
- [ ] 承認後に残日数が消化される
- [ ] 勤務履歴 CSV が正しくエクスポートされる
- [ ] 月別集計 CSV が正しくエクスポートされる
- [ ] ユーザ一覧 CSV が正しくエクスポートされる
- [ ] CSV の権限チェック（一般/承認者/管理者）が動作する
- [ ] テストカバレッジ 80% 以上
