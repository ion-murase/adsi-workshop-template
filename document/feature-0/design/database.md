# DB設計

## 概要

- RDBMS: PostgreSQL
- マイグレーション: Flyway
- タイムスタンプ: UTC で保存、アプリ層で拠点 TZ に変換
- ID: UUID v7（時系列ソート可能）
- 楽観ロック: version カラム

---

## ER図

```
┌──────────┐      ┌───────────────┐      ┌──────────┐
│  sites   │1───*│ departments   │*───*│  users   │
│          │      │               │      │          │
│ id       │      │ id            │      │ id       │
│ name     │      │ name          │      │ email    │
│ timezone │      │ site_id (FK)  │      │ name     │
└──────────┘      └───────────────┘      │ role     │
                         │                │ primary_ │
                         │                │  dept_id │
                  ┌──────┴──────┐        │ active   │
                  │user_departments│      └────┬─────┘
                  │ user_id (FK)  │           │
                  │ dept_id (FK)  │           │
                  └───────────────┘           │
                                             │
         ┌───────────────────────────────────┼──────────────────┐
         │                                   │                  │
┌────────┴────────┐              ┌───────────┴──────┐   ┌──────┴──────────┐
│  time_records   │              │  applications    │   │paid_leave_      │
│                 │              │                  │   │  balances       │
│ id              │              │ id               │   │                 │
│ user_id (FK)    │              │ applicant_id(FK) │   │ id              │
│ work_date       │              │ approver_id (FK) │   │ user_id (FK)    │
│ clock_in        │              │ type             │   │ fiscal_year     │
│ clock_out       │              │ status           │   │ granted_days    │
│ break_minutes   │              │ rejection_comment│   │ used_days       │
│ work_minutes    │              │ applied_at       │   │ carried_over    │
│ overtime_minutes│              │ decided_at       │   │ expires_at      │
│ night_minutes   │              └────────┬─────────┘   └─────────────────┘
│ holiday_work_   │                       │
│   minutes       │          ┌────────────┴────────────┐
│ is_holiday      │          │                         │
│ timezone        │   ┌──────┴────────┐   ┌───────────┴───────┐
└────────┬────────┘   │clock_fix_     │   │leave_request_     │
         │            │  details      │   │  details          │
┌────────┴────────┐   │               │   │                   │
│  time_entries   │   │application_id │   │ application_id    │
│                 │   │target_date    │   │ leave_date        │
│ id              │   │corrected_     │   │ leave_type        │
│ time_record_id  │   │  clock_in     │   └───────────────────┘
│ type            │   │corrected_     │
│ timestamp       │   │  clock_out    │
└─────────────────┘   │reason         │
                      └───────────────┘

┌──────────────────┐   ┌──────────────────┐
│company_calendars │   │  notifications   │
│                  │   │                  │
│ id               │   │ id               │
│ date             │   │ recipient_id(FK) │
│ holiday_name     │   │ type             │
│ year             │   │ title            │
└──────────────────┘   │ message          │
                       │ reference_id     │
                       │ is_read          │
                       └──────────────────┘
```

---

## テーブル定義

### sites（拠点）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| name | VARCHAR(100) | NOT NULL | 拠点名 |
| timezone | VARCHAR(50) | NOT NULL | IANA TZ (例: Asia/Tokyo) |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |

### departments（部署）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| name | VARCHAR(100) | NOT NULL | 部署名 |
| site_id | UUID | FK → sites.id, NOT NULL | |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |

### users（ユーザ）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| email | VARCHAR(255) | UNIQUE, NOT NULL | |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt |
| name | VARCHAR(100) | NOT NULL | |
| role | VARCHAR(20) | NOT NULL | ADMIN/APPROVER/GENERAL |
| primary_department_id | UUID | FK → departments.id, NOT NULL | 主管部署 |
| require_password_change | BOOLEAN | NOT NULL DEFAULT true | |
| active | BOOLEAN | NOT NULL DEFAULT true | |
| has_attendance_record | BOOLEAN | NOT NULL DEFAULT false | |
| failed_login_attempts | INTEGER | NOT NULL DEFAULT 0 | |
| locked_until | TIMESTAMP WITH TIME ZONE | | null=ロックなし |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |

**インデックス:**
- `idx_users_email` UNIQUE (email)
- `idx_users_primary_department_id` (primary_department_id)
- `idx_users_active` (active)

### user_departments（ユーザ所属部署）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| user_id | UUID | FK → users.id, NOT NULL | |
| department_id | UUID | FK → departments.id, NOT NULL | |

**制約:**
- PK (user_id, department_id)

### time_records（勤怠記録）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| user_id | UUID | FK → users.id, NOT NULL | |
| work_date | DATE | NOT NULL | 勤務日（拠点TZ基準） |
| clock_in | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| clock_out | TIMESTAMP WITH TIME ZONE | | null=未退勤 |
| break_minutes | INTEGER | NOT NULL DEFAULT 60 | |
| work_minutes | INTEGER | | 実労働時間 |
| overtime_minutes | INTEGER | | 残業時間 |
| night_minutes | INTEGER | | 深夜時間 |
| holiday_work_minutes | INTEGER | | 休日出勤時間 |
| is_holiday | BOOLEAN | NOT NULL DEFAULT false | |
| timezone | VARCHAR(50) | NOT NULL | 記録時点TZ |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| version | BIGINT | NOT NULL DEFAULT 0 | |

**制約:**
- UNIQUE (user_id, work_date) — 1ユーザ1日1レコード

**インデックス:**
- `idx_time_records_user_date` UNIQUE (user_id, work_date)
- `idx_time_records_user_month` (user_id, work_date) — 月次検索用

### time_entries（中間打刻）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| time_record_id | UUID | FK → time_records.id, NOT NULL | |
| type | VARCHAR(10) | NOT NULL | GO_OUT / RETURN |
| timestamp | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |

**インデックス:**
- `idx_time_entries_record` (time_record_id)

### applications（申請）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| applicant_id | UUID | FK → users.id, NOT NULL | |
| approver_id | UUID | FK → users.id | 承認/却下した人 |
| type | VARCHAR(20) | NOT NULL | CLOCK_FIX / LEAVE_REQUEST |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | |
| rejection_comment | TEXT | | |
| applied_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| decided_at | TIMESTAMP WITH TIME ZONE | | |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| version | BIGINT | NOT NULL DEFAULT 0 | |

**インデックス:**
- `idx_applications_applicant` (applicant_id)
- `idx_applications_status` (status)
- `idx_applications_type_status` (type, status)

### clock_fix_details（打刻修正申請詳細）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| application_id | UUID | PK, FK → applications.id | |
| target_date | DATE | NOT NULL | |
| corrected_clock_in | TIMESTAMP WITH TIME ZONE | | |
| corrected_clock_out | TIMESTAMP WITH TIME ZONE | | |
| reason | TEXT | NOT NULL | |

### leave_request_details（有給休暇申請詳細）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| application_id | UUID | PK, FK → applications.id | |
| leave_date | DATE | NOT NULL | |
| leave_type | VARCHAR(10) | NOT NULL | FULL_DAY / HALF_AM / HALF_PM |

### paid_leave_balances（有給残日数）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| user_id | UUID | FK → users.id, NOT NULL | |
| fiscal_year | INTEGER | NOT NULL | |
| granted_days | DECIMAL(4,1) | NOT NULL | |
| used_days | DECIMAL(4,1) | NOT NULL DEFAULT 0 | |
| carried_over_days | DECIMAL(4,1) | NOT NULL DEFAULT 0 | |
| expires_at | DATE | NOT NULL | 繰越有効期限 |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| updated_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |
| version | BIGINT | NOT NULL DEFAULT 0 | |

**制約:**
- UNIQUE (user_id, fiscal_year)

### company_calendars（会社カレンダー）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| date | DATE | UNIQUE, NOT NULL | |
| holiday_name | VARCHAR(100) | NOT NULL | |
| year | INTEGER | NOT NULL | 年度（検索用） |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |

**インデックス:**
- `idx_company_calendars_year` (year)
- `idx_company_calendars_date` UNIQUE (date)

### notifications（通知）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | UUID | PK | |
| recipient_id | UUID | FK → users.id, NOT NULL | |
| type | VARCHAR(30) | NOT NULL | |
| title | VARCHAR(200) | NOT NULL | |
| message | TEXT | NOT NULL | |
| reference_id | UUID | | 関連エンティティID |
| is_read | BOOLEAN | NOT NULL DEFAULT false | |
| created_at | TIMESTAMP WITH TIME ZONE | NOT NULL | |

**インデックス:**
- `idx_notifications_recipient_read` (recipient_id, is_read)
- `idx_notifications_recipient_created` (recipient_id, created_at DESC)

---

## 監査ログ（別途検討）

勤怠データの変更履歴は以下のいずれかで実現:
- **案1**: Hibernate Envers（推奨。Entity 変更を自動追跡）
- **案2**: トリガーベースの監査テーブル

→ 実装フェーズで決定
