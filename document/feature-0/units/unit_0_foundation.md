# Unit 0: 共通基盤

## 目的

全 Unit が依存するプロジェクト骨格・DB スキーマ・テスト基盤を構築する。
この Unit が完成すれば、Unit 1〜5 は独立してテストを書き始められる。

## Phase

**Phase A**（最初に2人共同で完成させる）

## スコープ

### Backend（Spring Boot）

- [ ] プロジェクト初期化（Spring Boot 3.x, Java 21）
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-validation
  - flyway-core
  - postgresql
  - lombok
  - jjwt（JWT）
  - spring-boot-starter-test
  - spring-security-test
- [ ] パッケージ構成
  ```
  com.example.attendance
  ├── config/          -- SecurityConfig, JwtConfig, WebConfig
  ├── common/
  │   ├── entity/      -- BaseEntity (id, createdAt, updatedAt)
  │   ├── exception/   -- BusinessException, GlobalExceptionHandler
  │   └── dto/         -- ErrorResponse, ValidationErrorResponse, PageResponse
  ├── domain/
  │   ├── user/        -- Unit 1
  │   ├── clock/       -- Unit 2
  │   ├── application/ -- Unit 4
  │   ├── leave/       -- Unit 5
  │   ├── notification/-- Unit 3
  │   └── calendar/    -- Unit 2
  └── infrastructure/
      └── security/    -- JwtTokenProvider, JwtAuthFilter
  ```
- [ ] Flyway マイグレーション（全テーブル DDL）
  - V1__create_sites.sql
  - V2__create_departments.sql
  - V3__create_users.sql
  - V4__create_user_departments.sql
  - V5__create_time_records.sql
  - V6__create_time_entries.sql
  - V7__create_applications.sql
  - V8__create_clock_fix_details.sql
  - V9__create_leave_request_details.sql
  - V10__create_paid_leave_balances.sql
  - V11__create_company_calendars.sql
  - V12__create_notifications.sql
- [ ] 共通 Entity 基盤
  - BaseEntity（id, createdAt, updatedAt）
  - Enum 定義（Role, ApplicationStatus, ApplicationType, LeaveType, EntryType, NotificationType）
- [ ] Security 基盤（JWT トークン生成/検証のスケルトン）
- [ ] GlobalExceptionHandler
- [ ] テスト基盤
  - application-test.yml（H2 or Testcontainers PostgreSQL）
  - テストユーティリティ（TestDataFactory）
- [ ] ArchUnit テスト（レイヤー依存ルール）

### Frontend（Next.js）

- [ ] プロジェクト初期化（Next.js 14+ App Router, TypeScript）
  - tailwindcss
  - shadcn/ui
  - @tanstack/react-query
  - react-hook-form + zod
  - vitest + @testing-library/react
- [ ] ディレクトリ構成
  ```
  src/
  ├── app/
  │   ├── (auth)/         -- ログイン、パスワード変更
  │   ├── (main)/         -- 認証済みレイアウト
  │   │   ├── page.tsx    -- ダッシュボード
  │   │   ├── time-records/
  │   │   ├── applications/
  │   │   ├── approvals/
  │   │   ├── notifications/
  │   │   ├── export/
  │   │   └── admin/
  │   └── layout.tsx
  ├── components/
  │   ├── ui/             -- shadcn コンポーネント
  │   └── common/         -- Header, Sidebar, DataTable etc.
  ├── lib/
  │   ├── api-client.ts   -- fetch wrapper (basePath対応)
  │   ├── auth.ts         -- トークン管理
  │   └── utils.ts
  └── types/
      └── api.ts          -- API レスポンス型定義
  ```
- [ ] API Client（fetch wrapper + basePath + エラーハンドリング）
- [ ] 認証ガード（AuthProvider + リダイレクト）
- [ ] 共通レイアウト（Header + Sidebar スケルトン）
- [ ] 共通コンポーネント
  - DataTable（ページネーション対応）
  - MonthPicker
  - ConfirmDialog
  - Toast
  - StatusBadge
  - LoadingSpinner
  - EmptyState
- [ ] API 型定義（types/api.ts）
- [ ] テスト設定（vitest.config.ts, test utils）

### インフラ

- [ ] docker-compose.yml（PostgreSQL, pgAdmin）
- [ ] .env.example
- [ ] Makefile or package.json scripts（dev, test, lint）

## 完了条件

- [ ] `mvn test` が通る（ArchUnit + マイグレーション成功）
- [ ] `npm run test` が通る（共通コンポーネントのスモークテスト）
- [ ] `docker-compose up` で PostgreSQL が起動し、Flyway マイグレーションが成功する
- [ ] 各 Unit が自分のテストを書き始められる状態

## テーブル

全テーブル（DDL のみ。データ操作は各 Unit で実装）:
- sites, departments, users, user_departments
- time_records, time_entries
- applications, clock_fix_details, leave_request_details
- paid_leave_balances
- company_calendars
- notifications

## API

なし（この Unit は API エンドポイントを持たない。基盤のみ）
