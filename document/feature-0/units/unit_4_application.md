# Unit 4: 申請・承認

## 目的

打刻修正申請・承認/却下/取り下げワークフローと、残業超過通知を実装する。

## Phase

**Phase C**（Unit 1, 2, 3 完了後）

## 依存

- Unit 1: 認証・ユーザ情報（承認者の特定に必要）
- Unit 2: 勤怠記録（打刻修正の反映先）
- Unit 3: 通知基盤（申請到着・結果通知・残業超過通知）

## ユーザーストーリー

- US-4: 残業超過通知（30h/45h/60h）
- US-5: 打刻修正申請

## テーブル

- applications
- clock_fix_details

## API エンドポイント

| メソッド | パス | 操作 |
|---------|------|------|
| POST | /applications/clock-fix | 打刻修正申請 |
| GET | /applications | 申請一覧（自分の） |
| GET | /applications/pending | 承認待ち一覧（承認者） |
| POST | /applications/{id}/approve | 承認 |
| POST | /applications/{id}/reject | 却下 |
| POST | /applications/{id}/withdraw | 取り下げ |

## Backend 実装

### Entity
- Application, ClockFixDetail

### Repository
- ApplicationRepository
  - findByApplicantId(userId, pageable)
  - findPendingByApproverDepartments(departmentIds, pageable)
  - findById(id)

### Service
- ApplicationService
  - submitClockFix(userId, request): 打刻修正申請の提出
    - 遡及期限チェック（翌月第3営業日）
    - 通知送信（主管部署の承認者全員へ）
  - approve(applicationId, approverId): 承認
    - 打刻修正: TimeRecord を更新し勤務時間を再計算
    - 通知送信（申請者へ）
  - reject(applicationId, approverId, comment): 却下
    - 通知送信（申請者へ）
  - withdraw(applicationId, userId): 取り下げ
    - PENDING のみ取り下げ可能
  - getMyApplications(userId, status, type, pageable): 自分の申請一覧
  - getPendingApplications(approverId, pageable): 承認待ち一覧

- OvertimeAlertService
  - checkAndNotify(userId, yearMonth): 月次残業チェック
    - 退勤打刻時 or 打刻修正承認時に呼び出す
    - 30h/45h/60h のいずれかを超過したら通知
    - 同月同閾値で重複通知しない
  - 通知先: 本人 + 主管部署の承認者

### 遡及期限の計算ロジック

```
対象月の翌月1日から起算して3営業日目の23:59:59まで
営業日 = 平日 かつ 会社カレンダーで休日でない日
```

## Frontend 実装

### 画面
- 申請一覧 (/applications) — ステータスフィルタ + 取り下げボタン
- 打刻修正申請フォーム (/applications/clock-fix/new)
- 承認待ち一覧 (/approvals) — 承認/却下アクション

### コンポーネント
- ApplicationTable（申請一覧テーブル）
- ClockFixForm（修正日・時刻・理由入力）
- ApprovalTable（承認待ち一覧 + 承認/却下ボタン）
- RejectDialog（却下理由入力モーダル）
- StatusBadge（PENDING/APPROVED/REJECTED/WITHDRAWN）

## テスト

### Backend
- ApplicationService: 提出/承認/却下/取り下げ/遡及期限 (Unit test)
- OvertimeAlertService: 閾値超過判定/重複通知防止 (Unit test)
- 承認後の TimeRecord 更新と勤務時間再計算 (Unit test)
- ApplicationController: 権限チェック（承認者のみ承認可能）(@WebMvcTest)
- 統合テスト: 申請→承認→勤怠反映→通知 (@SpringBootTest)

### Frontend
- ClockFixForm: バリデーション（理由必須、日付範囲）
- ApprovalTable: 承認/却下操作
- ApplicationTable: ステータスフィルタ

## 完了条件

- [ ] 打刻修正申請の提出が動作する
- [ ] 遡及期限チェックが動作する
- [ ] 承認/却下/取り下げが動作する
- [ ] 承認後に勤怠記録が更新される
- [ ] 申請到着・結果の通知が送信される
- [ ] 残業超過通知（30h/45h/60h）が動作する
- [ ] 承認者のみが承認操作できる（権限チェック）
- [ ] テストカバレッジ 80% 以上
