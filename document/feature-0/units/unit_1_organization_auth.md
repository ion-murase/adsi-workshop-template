# Unit 1: 組織管理・認証

## 目的

ユーザ・部署・拠点の CRUD と認証機能を実装する。
他の全 Unit が「誰が操作しているか」に依存するため、Phase B の最優先。

## Phase

**Phase B**（Unit 0 完了後、Unit 2/3 と並行可能）

## 依存

- Unit 0: 共通基盤（テーブル DDL, Entity 基盤, Security スケルトン）

## ユーザーストーリー

- US-8: ユーザ管理（登録/更新/無効化）
- US-9: 認証（ログイン/ログアウト/パスワード変更/アカウントロック）
- US-12: 拠点・タイムゾーン管理
- US-13: 部署管理

## テーブル

- sites
- departments
- users
- user_departments

## API エンドポイント

| メソッド | パス | 操作 |
|---------|------|------|
| POST | /auth/login | ログイン |
| POST | /auth/logout | ログアウト |
| PUT | /auth/password | パスワード変更 |
| GET | /users | ユーザ一覧 |
| POST | /users | ユーザ登録 |
| GET | /users/{id} | ユーザ詳細 |
| PUT | /users/{id} | ユーザ更新 |
| DELETE | /users/{id} | ユーザ削除/無効化 |
| POST | /users/{id}/reset-password | 仮パスワード発行 |
| GET | /users/me | 自分のプロフィール |
| GET | /departments | 部署一覧 |
| POST | /departments | 部署作成 |
| PUT | /departments/{id} | 部署更新 |
| DELETE | /departments/{id} | 部署削除 |
| GET | /sites | 拠点一覧 |
| POST | /sites | 拠点作成 |
| PUT | /sites/{id} | 拠点更新 |

## Backend 実装

### Entity
- User, Department, Site, UserDepartment

### Repository
- UserRepository: findByEmail, findByDepartmentId, findActiveUsers
- DepartmentRepository: findBySiteId, existsByIdWithUsers
- SiteRepository: findAll

### Service
- AuthenticationService
  - login(email, password): トークン発行、ロック判定
  - logout(token): トークン無効化
  - changePassword(userId, current, new): パスワード変更
- UserManagementService
  - createUser(request): ユーザ登録 + 仮パスワード生成
  - updateUser(id, request): 更新（ロール変更含む）
  - deleteUser(id): 物理削除 or 無効化
  - resetPassword(id): 仮パスワード再発行

### Security
- JwtTokenProvider: トークン生成/検証/リフレッシュ
- JwtAuthFilter: リクエストごとのトークン検証
- SecurityConfig: エンドポイントごとの認可ルール

## Frontend 実装

### 画面
- ログイン画面 (/login)
- パスワード変更画面 (/change-password)
- ユーザ管理一覧 (/admin/users)
- ユーザ登録/編集フォーム (/admin/users/new, /admin/users/:id)
- 部署管理 (/admin/departments)
- 拠点管理 (/admin/sites)

### コンポーネント
- LoginForm
- ChangePasswordForm
- UserTable, UserForm
- DepartmentTable, DepartmentForm
- SiteTable, SiteForm

## テスト

### Backend
- UserRepository: CRUD テスト (@DataJpaTest)
- AuthenticationService: ログイン成功/失敗/ロック (Unit test)
- UserManagementService: 登録/削除制約 (Unit test)
- AuthController: 認証フロー (@WebMvcTest)
- UserController: 権限チェック (@WebMvcTest)
- 統合テスト: ログイン → ユーザ操作 (@SpringBootTest)

### Frontend
- LoginForm: 入力バリデーション、送信
- UserTable: 一覧表示、ページネーション
- UserForm: バリデーション、部署選択

## 完了条件

- [ ] ログイン/ログアウト/パスワード変更が動作する
- [ ] アカウントロック（5回失敗→30分）が動作する
- [ ] 初回ログイン時にパスワード変更を強制する
- [ ] ユーザ CRUD（物理削除/無効化の制約含む）が動作する
- [ ] 部署・拠点の CRUD が動作する
- [ ] ロールベースの認可（ADMIN のみ管理操作）が動作する
- [ ] テストカバレッジ 80% 以上
