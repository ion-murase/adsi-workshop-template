# Unit 3: 通知基盤

## 目的

アプリ内通知の送信・一覧・既読管理を実装する。
他の Unit（申請・残業超過）から呼ばれる「通知送信」の共通基盤。

## Phase

**Phase B**（Unit 0 完了後、Unit 1/2 と並行可能。小規模のため短期で完了）

## 依存

- Unit 0: 共通基盤

## ユーザーストーリー

- US-7: 通知（一覧・未読/既読管理）

## テーブル

- notifications

## API エンドポイント

| メソッド | パス | 操作 |
|---------|------|------|
| GET | /notifications | 通知一覧 |
| GET | /notifications/unread-count | 未読件数 |
| POST | /notifications/{id}/read | 既読化 |
| POST | /notifications/read-all | 全既読化 |

## Backend 実装

### Entity
- Notification

### Repository
- NotificationRepository: findByRecipientId, countUnreadByRecipientId

### Service
- NotificationService
  - send(recipientId, type, title, message, referenceId): 通知作成
  - getNotifications(recipientId, unreadOnly, pageable): 一覧取得
  - getUnreadCount(recipientId): 未読件数
  - markAsRead(id, recipientId): 既読化
  - markAllAsRead(recipientId): 全既読化

### 他 Unit からの呼び出しインターフェース

```java
public interface NotificationService {
    void send(UUID recipientId, NotificationType type,
              String title, String message, UUID referenceId);
}
```

Unit 4（申請）や Unit 2 拡張（残業超過）から上記を呼び出す。

## Frontend 実装

### 画面
- 通知一覧 (/notifications)

### コンポーネント
- NotificationBadge（ヘッダーの未読件数バッジ — ポーリング or SSE）
- NotificationList（通知一覧 + 既読化）
- NotificationItem（個々の通知カード）

## テスト

### Backend
- NotificationService: 送信/既読/全既読 (Unit test)
- NotificationController: 一覧/件数/既読API (@WebMvcTest)
- NotificationRepository: ページネーション (@DataJpaTest)

### Frontend
- NotificationBadge: 未読件数表示
- NotificationList: 一覧表示・既読化操作

## 完了条件

- [ ] 通知の作成（Service 経由）が動作する
- [ ] 通知一覧が取得できる（未読フィルタ・ページネーション）
- [ ] 未読件数が取得できる
- [ ] 個別既読/全既読が動作する
- [ ] テストカバレッジ 80% 以上

## 規模感

Backend: Entity 1 + Repository 1 + Service 1 + Controller 1
Frontend: 画面 1 + コンポーネント 3

→ **1〜2日で完了可能**。Phase B の他 Unit と同一担当者が着手しても問題ない。
