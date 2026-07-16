# CLAUDE.md

## このリポジトリについて

このリポジトリは、Java/Spring Boot バックエンドと Next.js フロントエンドを含む勤怠管理アプリのワークショッププロジェクトです。

- `packages/backend/`: Spring Boot 3.x / Java 21 バックエンド
- `packages/frontend/`: Next.js 14+ App Router フロントエンド
- `docker-compose.yml`: PostgreSQL と pgAdmin の開発コンテナ構成
- `document/feature-0/`: 要求仕様・設計・Unit 0 基盤ドキュメント
- `.claude/`: ワークショップ用ルール・スキル・エージェント定義

## 主要スタック

### Backend

- Java 21
- Spring Boot 3.4
- Spring Data JPA
- Spring Security
- Bean Validation
- Flyway
- PostgreSQL / H2
- Lombok
- JJWT
- JUnit 5 + Spring Boot テスト
- ArchUnit

### Frontend

- Next.js 14 + App Router
- TypeScript
- Tailwind CSS
- React Hook Form
- Zod
- TanStack Query
- Vitest + Testing Library

## 重要ファイル

- `package.json`
  - `setup`: backend build + frontend install
  - `boot`, `boot:workshop`: backend 起動
  - `dev`, `dev:frontend`: frontend 起動
  - `check:backend`, `test:backend`, `lint:frontend`
- `docker-compose.yml`
  - `postgres` と `pgadmin` を起動
- `packages/backend/build.gradle`
  - Java toolchain、依存関係、テスト設定
- `packages/frontend/package.json`
  - Next.js / Vitest / Tailwind の設定
- `.claude/README.md`
  - ワークショップ用のルールとエージェント構成
- `.claude/settings.json`
  - AI モデル設定

## 開発の流れ

1. `npm run setup`: backend ビルドと frontend 依存インストール
2. `docker compose up`: 開発用 PostgreSQL / pgAdmin を起動
3. `npm run dev:frontend`: フロントエンド開発サーバを起動
4. `cd packages/backend && ./gradlew bootRun`: バックエンドを起動

## `.claude` の役割

`.claude/` はこのワークショップ向けの開発プロセスとレビュー支援をまとめています。

- `rules/`: コーディング規約、テスト、セキュリティ、SageMaker 制約
- `skills/`: 要求仕様、設計、作業分割、TDD 実装、検証、レビュー手順
- `agents/`: Java / TypeScript / セキュリティ / テストレビュー用 subagent

## 推奨ワークフロー

- まず `document/feature-0/` の要求仕様・設計を読む
- `.claude/rules/` を参照しながら実装とテストを進める
- 変更内容のレビューには `multi-agent-review` スキルを活用する

## 追加ノート

- `document/feature-0/units/unit_0_foundation.md` は基盤 Unit の完了条件を定義しています。
- このプロジェクトは `mvn test` ではなく `./gradlew test` でバックエンドテストを実行します。
- SageMaker での開発には `scripts/dev-sagemaker.sh` / `scripts/dev-sagemaker-stop.sh` が用意されています。
