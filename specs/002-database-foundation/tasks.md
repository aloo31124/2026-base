# Tasks：資料庫基礎

## 目錄

- [Phase 1 - Scripts](#phase-1---scripts) — 3 項
- [Phase 2 - JPA Model](#phase-2---jpa-model) — 4 項
- [Phase 3 - CRUD Stack](#phase-3---crud-stack) — 4 項
- [Phase 4 - Validation](#phase-4---validation) — 2 項

## Phase 1 - Scripts

- [x] T001 [US1] 撰寫 Windows 整合驗證與冪等初始化於 scripts/init-database.ps1
- [x] T002 [P] [US1] 撰寫 macOS/Linux 初始化於 scripts/init-database.sh
- [x] T003 [US1] 驗證 DB/Login 名稱並以受控識別名稱執行 CREATE DATABASE/LOGIN/USER/db_owner

## Phase 2 - JPA Model

- [x] T004 [US3] 建立 UUID、created_at、updated_at BaseEntity 於 backend/src/main/java/com/agentflow/base/model/bo/BaseEntity.java
- [x] T005 [P] [US2] 建立單數 snake_case app_user、role、user_role BO 於 backend/src/main/java/com/agentflow/base/model/bo
- [x] T006 [P] [US2] 建立 test BO 及 `_status` 欄位於 backend/src/main/java/com/agentflow/base/model/bo/TestRecord.java
- [x] T007 [US2] 設定 MSSQL datasource 與 JPA 自動建表於 backend/src/main/resources/application.yml

## Phase 3 - CRUD Stack

- [x] T008 [P] [US2] 建立 TestRecordDao 於 backend/src/main/java/com/agentflow/base/dao/TestRecordDao.java
- [x] T009 [US2] 建立 TestRecordService 於 backend/src/main/java/com/agentflow/base/service/TestRecordService.java
- [x] T010 [US2] 建立 TestRecordController 於 backend/src/main/java/com/agentflow/base/controller/TestRecordController.java
- [x] T011 [US2] 建立 React test CRUD page 於 frontend/src/pages/TestPage.tsx

## Phase 4 - Validation

- [x] T012 [US3] 執行 DatabaseConventionTest 驗證 UUID、審計與命名規則
- [x] T013 [US1] 啟動 MSSQL 後端並由 Postman collection 完成 test CRUD

