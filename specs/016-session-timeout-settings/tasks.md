# Tasks：後台登出時間設定

## 目錄

- [Phase 1: Setup & Specification](#phase-1-setup--specification) — 4 項
- [Phase 2: Foundational Tests](#phase-2-foundational-tests) — 3 項
- [Phase 3: User Story 1 - 管理政策](#phase-3-user-story-1---管理政策) — 5 項
- [Phase 4: User Story 2 & 3 - JWT 動態效期](#phase-4-user-story-2--3---jwt-動態效期) — 3 項
- [Phase 5: User Story 4 - 後台 UI 與權限](#phase-5-user-story-4---後台-ui-與權限) — 4 項
- [Phase 6: Acceptance & Closeout](#phase-6-acceptance--closeout) — 5 項
- [Dependencies](#dependencies) — 執行順序
- [Parallel Opportunities](#parallel-opportunities) — 可平行項
- [Implementation Strategy](#implementation-strategy) — claims-first TDD

## Phase 1: Setup & Specification

- [x] T001 建立 `specs/016-session-timeout-settings/spec.md` 並定義 4 條 User Story 與 12 條 FR
- [x] T002 [P] 建立 `specs/016-session-timeout-settings/checklists/requirements.md` 並完成 22 項檢核
- [x] T003 [P] 建立 `plan.md`、`research.md`、`data-model.md` 與 `quickstart.md`
- [x] T004 [P] 建立 `contracts/openapi.yaml` 與 `analyze.md`

## Phase 2: Foundational Tests

- [x] T005 在 `backend/src/test/java/com/agentflow/base/RegistrationManagementIntegrationTest.java` 新增政策 CRUD、邊界、權限與 JWT claims 測試
- [x] T006 [P] 在 `frontend/cypress/e2e/session-timeout-settings.cy.ts` 新增管理表單 contract E2E
- [x] T007 確認 T005/T006 在 API、BO 與 UI 尚未實作時紅燈（後端 4 個 missing symbol 編譯錯誤；Cypress 0/1，未發出 session-timeout request）並記錄證據於本文件

## Phase 3: User Story 1 - 管理政策

- [x] T008 [P] [US1] 建立 `backend/src/main/java/com/agentflow/base/model/bo/SessionTimeoutPolicy.java`
- [x] T009 [P] [US1] 建立 `backend/src/main/java/com/agentflow/base/dao/SessionTimeoutPolicyDao.java`
- [x] T010 [US1] 在 `backend/src/main/java/com/agentflow/base/model/dto/RegistrationManagementDtos.java` 新增政策 DTO
- [x] T011 [US1] 建立 `backend/src/main/java/com/agentflow/base/service/SessionTimeoutPolicyService.java`
- [x] T012 [US1] 在 `backend/src/main/java/com/agentflow/base/controller/RegistrationManagementController.java` 新增 GET/PUT API

## Phase 4: User Story 2 & 3 - JWT 動態效期

- [x] T013 [US2] 在 `backend/src/main/java/com/agentflow/base/security/JwtService.java` 以目前政策產生 exp
- [x] T014 [US2] 驗證帳密、信箱與 LINE 共用 JwtService，無各流程分支
- [x] T015 [US3] 驗證更新前 token claims 不變、更新後 token 使用新分鐘數

## Phase 5: User Story 4 - 後台 UI 與權限

- [x] T016 [US4] 在 `frontend/src/pages/RegistrationManagementPage.tsx` 並行讀取及更新登出時間
- [x] T017 [US4] 在同頁新增登入工作階段卡片、生效說明與 5–1440 輸入邊界
- [x] T018 [US4] 在 `frontend/src/styles.css` 補登出時間表單 RWD 樣式
- [x] T019 [US4] 在 `frontend/package.json` 新增專屬 Cypress script

## Phase 6: Acceptance & Closeout

- [x] T020 執行 RegistrationManagementIntegrationTest 與後端完整測試至綠燈
- [x] T021 執行前端 production build、session timeout Cypress 1/1 與既有 login countdown Cypress 5/5 至綠燈
- [x] T022 更新 `skills/business-logic/registration-management/SKILL.md` 與 `login-session-countdown/SKILL.md`
- [x] T023 逐條重核 FR-001–012 與 22 項 checklist
- [x] T024 確認 `TASKS_PENDING=0`、文件格式與 git diff check 通過

## Dependencies

`T001–T004 → T005–T007 → T008–T012 → T013–T015 → T016–T019 → T020–T024`

- BO/DAO 可平行，Service 依賴兩者；Controller 依賴 DTO 與 Service。
- JwtService 動態效期需在 policy service 完成後接入。
- UI E2E 可先以 contract intercept 建立紅燈，再接 React 表單。

## Parallel Opportunities

- T002–T004、T005–T006、T008–T009 修改不同檔案，可平行準備。
- T016–T018 涉及同一 UI 流程，依序完成以降低樣式與狀態衝突。

## Implementation Strategy

先以 MockMvc 解析真實 JWT claims 定義安全核心，再補 UI contract Cypress。依 BO → DAO → Service → Controller → JwtService → React 的順序實作，最後回歸 feature 015 倒數，確保後台值仍以 JWT exp 傳遞到右上角。
