# Tasks：信箱註冊登入

## 目錄

- [Phase 1 - 規格與測試基線](#phase-1---規格與測試基線) — 3 項
- [Phase 2 - DB、BO 與 DAO](#phase-2---dbbo-與-dao) — 4 項
- [Phase 3 - Service 業務邏輯](#phase-3---service-業務邏輯) — 4 項
- [Phase 4 - Controller REST API](#phase-4---controller-rest-api) — 3 項
- [Phase 5 - React 與 Page](#phase-5---react-與-page) — 4 項
- [Phase 6 - Postman、Cypress 與報告](#phase-6---postmancypress-與報告) — 5 項
- [Phase 7 - 最終驗收與回寫](#phase-7---最終驗收與回寫) — 4 項
- [Dependencies](#dependencies) — 執行順序與平行規則
- [FR Coverage](#fr-coverage) — 18 條需求對應

## Phase 1 - 規格與測試基線

- [x] T001 [P] 將 Sheet 六條 MUST 編入 `specs/007-gmail-verification-mail/spec.md`、checklist 與本 tasks
- [x] T002 [P] 更新 `specs/007-gmail-verification-mail/plan.md`、`data-model.md` 與 `contracts/openapi.yaml`
- [x] T003 建立 `backend/src/test/java/com/agentflow/base/EmailRegistrationLoginIntegrationTest.java`，先涵蓋註冊、重複信箱、登入、忘記密碼、一次性票券與寄送紀錄（FR-001–FR-013、FR-016）

## Phase 2 - DB、BO 與 DAO

- [x] T004 [P] 建立 `backend/src/main/java/com/agentflow/base/model/bo/EmailVerification.java`（FR-004–FR-007、FR-013、FR-014）
- [x] T005 [P] 建立 `backend/src/main/java/com/agentflow/base/model/bo/EmailDeliveryLog.java`（FR-001–FR-002、FR-014）
- [x] T006 [P] 建立 `backend/src/main/java/com/agentflow/base/dao/EmailVerificationDao.java` 與查詢契約（FR-006–FR-007、FR-013–FR-014）
- [x] T007 [P] 建立 `backend/src/main/java/com/agentflow/base/dao/EmailDeliveryLogDao.java` 與最近紀錄查詢（FR-001–FR-002、FR-014）

## Phase 3 - Service 業務邏輯

- [x] T008 建立 `backend/src/main/java/com/agentflow/base/service/EmailDeliveryLogService.java`，封裝寄送紀錄讀寫（FR-001–FR-002、FR-014）
- [x] T009 擴充 `backend/src/main/java/com/agentflow/base/service/EmailVerificationService.java`，完成首次檢查、寄碼、雜湊、核銷、效期與重試（FR-003–FR-007、FR-012）
- [x] T010 建立 `backend/src/main/java/com/agentflow/base/service/EmailRegistrationService.java`，完成建帳、角色、自動登入與重設密碼（FR-008–FR-013）
- [x] T011 更新 `MailProperties.java`、`SmtpMailGateway.java` 與 H2/test 設定，提供正式預設關閉的 E2E mock SMTP（FR-004、FR-016–FR-017）

## Phase 4 - Controller REST API

- [x] T012 更新 `EmailVerificationDtos.java`，建立寄碼、核銷、註冊、重設與紀錄 DTO（FR-001–FR-013）
- [x] T013 建立 `backend/src/main/java/com/agentflow/base/controller/EmailAuthController.java` 公開驗證／註冊／重設 API（FR-003–FR-013）
- [x] T014 擴充 `EmailVerificationController.java` 管理員寄信與最近紀錄 API，保持原 API 相容（FR-001–FR-002、FR-014）

## Phase 5 - React 與 Page

- [x] T015 建立 `frontend/src/pages/EmailRegistrationPage.tsx` 三步註冊與自動登入流程（FR-003–FR-010、FR-015）
- [x] T016 建立 `frontend/src/pages/ForgotPasswordPage.tsx` 三步重設流程（FR-012–FR-013、FR-015）
- [x] T017 更新 `LoginPage.tsx`、`authSlice.ts` 與 `App.tsx`，支援信箱登入、註冊／忘記密碼入口及路由（FR-010–FR-012、FR-015）
- [x] T018 更新 `EmailVerificationPage.tsx` 與 `styles.css`，顯示最近寄送紀錄並對齊 uiux 共用樣式（FR-001–FR-002、FR-015）

## Phase 6 - Postman、Cypress 與報告

- [x] T019 建立 `postman/email-registration-login.postman_collection.json`，逐項驗證六條 MUST 與 response（FR-016、FR-018）
- [x] T020 建立 `frontend/cypress/e2e/email-registration-login.cy.ts`，逐項驗證註冊、重複、登入及重設（FR-017–FR-018）
- [x] T021 執行 Postman/Newman 到綠燈並產出 `report/test/result-20260729-email-registration-login-postman.md`（FR-018）
- [x] T022 執行 Cypress 到綠燈並產出 `report/test/result-20260729-email-registration-login-cypress.md`（FR-018）
- [x] T023 更新 `skills/business-logic/email-verification/SKILL.md` 與 `skills/SKILLS_INDEX.md` 的正式註冊契約

## Phase 7 - 最終驗收與回寫

- [x] T024 執行後端 test/build 與前端 production build，確認全數 exit code 0（FR-016、FR-018）
- [x] T025 逐條完成 `checklists/requirements.md` 並更新 `analyze.md` 為 100% FR↔task 覆蓋
- [x] T026 將 Google Sheet `註冊登入驗證!B25:B30` 從「預計開發」更新為「開發完成」
- [x] T027 建立新的 AI 對話承接完成摘要，符合每個子模組完成後切換對話的要求

## Dependencies

- T003 先於 T004–T014，建立紅燈合約。
- T004–T007 可平行，完成後才執行 T008–T010。
- T009–T011 完成後執行 T012–T014，固定 API 契約。
- T015–T018 在 API 契約固定後執行；不同頁面可平行但共享路由與樣式時循序合併。
- T019–T020 在前後端完成後建立；T021–T022 必須實際啟動應用程式驗收。
- T026 僅能在 T024–T025 全部成功後執行。

## FR Coverage

| FR | Tasks |
|---|---|
| FR-001–FR-002 | T005、T007–T008、T014、T018–T019 |
| FR-003–FR-007 | T003–T004、T006、T009、T012–T013、T015 |
| FR-008–FR-010 | T003、T010、T012–T013、T015、T017 |
| FR-011 | T003、T010、T017、T019–T020 |
| FR-012–FR-013 | T003–T004、T006、T009–T010、T012–T013、T016 |
| FR-014 | T004–T014 |
| FR-015 | T015–T018 |
| FR-016 | T003、T011、T019、T024 |
| FR-017 | T011、T020、T022 |
| FR-018 | T019–T027 |
