# Tasks：註冊登入管理

## 目錄

- [Phase 1 - Setup](#phase-1---setup) — 3 項
- [Phase 2 - Foundational](#phase-2---foundational) — 5 項
- [Phase 3 - User Story 1 & 2](#phase-3---user-story-1--2) — 5 項
- [Phase 4 - User Story 3](#phase-4---user-story-3) — 4 項
- [Phase 5 - User Story 4](#phase-5---user-story-4) — 3 項
- [Phase 6 - Verification](#phase-6---verification) — 6 項

## Phase 1 - Setup

- [x] T001 建立 `specs/008-registration-management/` 全套 speckit 文件
- [x] T002 盤點 `uiux/` 與既有 React 共用樣式
- [x] T003 建立整合測試驗收骨架 `backend/src/test/java/com/agentflow/base/RegistrationManagementIntegrationTest.java`

## Phase 2 - Foundational

- [x] T004 [P] 建立密碼政策 BO `backend/src/main/java/com/agentflow/base/model/bo/PasswordPolicy.java`
- [x] T005 [P] 建立註冊紀錄 BO `backend/src/main/java/com/agentflow/base/model/bo/RegistrationRecord.java`
- [x] T006 [P] 建立 JPA DAO `backend/src/main/java/com/agentflow/base/dao/PasswordPolicyDao.java`
- [x] T007 [P] 建立 JPA DAO `backend/src/main/java/com/agentflow/base/dao/RegistrationRecordDao.java`
- [x] T008 建立 DTO `backend/src/main/java/com/agentflow/base/model/dto/RegistrationManagementDtos.java`

## Phase 3 - User Story 1 & 2

- [x] T009 [US1] 實作政策讀寫業務邏輯 `backend/src/main/java/com/agentflow/base/service/RegistrationManagementService.java`
- [x] T010 [US2] 實作動態密碼驗證 `backend/src/main/java/com/agentflow/base/service/RegistrationManagementService.java`
- [x] T011 [US2] 接入信箱註冊與重設 `backend/src/main/java/com/agentflow/base/service/EmailRegistrationService.java`
- [x] T012 [US1] 建立政策 REST API `backend/src/main/java/com/agentflow/base/controller/RegistrationManagementController.java`
- [x] T013 [US1] 建立 React 政策表單 `frontend/src/pages/RegistrationManagementPage.tsx`

## Phase 4 - User Story 3

- [x] T014 [US3] 接入信箱首次註冊稽核 `backend/src/main/java/com/agentflow/base/service/EmailRegistrationService.java`
- [x] T015 [US3] 接入 LINE 首次註冊稽核 `backend/src/main/java/com/agentflow/base/service/LineOAuthService.java`
- [x] T016 [US3] 建立註冊紀錄 API `backend/src/main/java/com/agentflow/base/controller/RegistrationManagementController.java`
- [x] T017 [US3] 建立註冊紀錄表格 `frontend/src/pages/RegistrationManagementPage.tsx`

## Phase 5 - User Story 4

- [x] T018 [US4] 套用管理 API RBAC 與指定訊息 `backend/src/main/java/com/agentflow/base/config/SecurityConfig.java`
- [x] T019 [US4] 建立管理頁路由 Guard `frontend/src/App.tsx`
- [x] T020 [US4] 建立側欄入口與指定無權限頁訊息 `frontend/src/components/AppShell.tsx`

## Phase 6 - Verification

- [x] T021 完成並跑綠後端整合測試 `backend/src/test/java/com/agentflow/base/RegistrationManagementIntegrationTest.java`
- [x] T022 建立並執行 Postman collection `postman/registration-management.postman_collection.json`
- [x] T023 產生 Postman 報告 `report/test/result-20260729-registration-management-postman.md`
- [x] T024 建立並執行 Cypress `frontend/cypress/e2e/registration-management.cy.ts`
- [x] T025 產生 Cypress 報告 `report/test/result-20260729-registration-management-cypress.md`
- [x] T026 完成全專案建置、checklist 與 Sheet 第 18–22 列狀態回寫
